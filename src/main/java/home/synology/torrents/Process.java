package home.synology.torrents;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Process {

	private static Logger LOGGER = LoggerFactory.getLogger(Process.class);
	private static final String pathToFileWithCrapExtensions = "/volume1/misc/ignoreExtensions";

	private static List<Path> unableToProcess = new ArrayList<Path>();

	public static void main(String[] args) {
		LOGGER.info("Start processing: ", args[0]);
		Process.processPath(Paths.get(args[0]));

		LOGGER.info("Start indexing: ", args[0]);
		Index.index();
		LOGGER.info("End indexing: ", args[0]);
	}

	public static void processPath(Path thePath) {
		// recursive process the directories tree (could use
		// Files.walkFileTree...)
		LOGGER.debug("Processing path: " + thePath);
		if (whatIsThePathType(thePath).equals(PathType.FOLDER)) {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(thePath, new IgnoreCrapFilter(pathToFileWithCrapExtensions))) {
				for (Path entry : stream) {
					if("sample".equalsIgnoreCase(entry.getFileName().toString())){
						continue;
					}
					processPath(entry);
				}
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}

			// delete the path once we finished processing it
			if (thePath.getFileName() != null && !thePath.getFileName().toString().equals("downloaded")) {
				try {
					if (!unableToProcess.contains(thePath)) {
						removeRecursive(thePath);
					} else {
						LOGGER.warn("The path: " + thePath + " could not be deleted because of a process error");
					}
				} catch (IOException e) {
					LOGGER.error(e.getMessage());
				}
			}
		} else {
			try {
				Move.move(thePath);
			} catch (Exception e) {
				unableToProcess.add(thePath);
			}
		}
	}

	private static void removeRecursive(Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				// try to delete the file anyway, even if its attributes
				// could not be read, since delete-only access is
				// theoretically possible
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if (exc == null) {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				} else {
					// directory iteration failed; propagate exception
					throw exc;
				}
			}
		});
	}

	private static PathType whatIsThePathType(Path thePath) {
		if (Files.isRegularFile(thePath, LinkOption.NOFOLLOW_LINKS)) {
			return PathType.FILE;
		} else {
			return PathType.FOLDER;
		}
	}
}
