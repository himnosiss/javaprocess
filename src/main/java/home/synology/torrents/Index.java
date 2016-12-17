package home.synology.torrents;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Index {

	private static Logger LOGGER = LoggerFactory.getLogger(Index.class);
	private static Set<Path> pathsToIndex = new HashSet<>();

	public static void index() {
		for (Path path : pathsToIndex) {

			String option = "-A";
			if (!path.toString().contains(Move.MOVIES)) {
				option = "-a";
			}

			ProcessBuilder pb = new ProcessBuilder("/usr/syno/bin/synoindex", option, path.toString());
			LOGGER.info("trigger indexing: ", pb.command().toString());

			try {
				java.lang.Process p = pb.start();

				InputStream errorStream = p.getErrorStream();
				InputStream inputStream = p.getInputStream();
				StringBuilder errorMessage = new StringBuilder();
				while (p.isAlive()) {
					int character = -1;
					while ((character = errorStream.read()) != -1) {
						errorMessage.append((char) character);
					}
					errorMessage.append("\n");
					character = -1;
					while ((character = inputStream.read()) != -1) {
						errorMessage.append((char) character);
					}
				}
				LOGGER.info("indexing exited with the code: ", p.exitValue());
				LOGGER.error("error stream: ", errorMessage.toString());

			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
		}
	}

	/**
	 * add to the list only series, for movies the index is triggered
	 * automatically.
	 * 
	 * @param path
	 *            the path to the series episode
	 */
	public static void add(Path path) {
		LOGGER.debug("adding "+ path +" to the indexing list");
		pathsToIndex.add(path.getParent());
	}
}
