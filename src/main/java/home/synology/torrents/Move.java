package home.synology.torrents;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Move {

	private static Logger LOGGER = LoggerFactory.getLogger(Move.class);

	public static final String SERIES = "/volume1/video/tvshow";
	public static final String MOVIES = "/volume1/video/movie";
	// private static final String SERIES = "\\\\DISKSTATION/video/tvshow";
	// private static final String MOVIES = "\\\\DISKSTATION/video/movie";

	private static final Pattern SERIE = Pattern.compile("\\Ws\\d{1,2}");
	private static final Pattern MOVIE = Pattern.compile("avi|mkv|mp4");
	private static final Pattern SUBTITLE = Pattern.compile("srt|sub");

	/**
	 * moves a path representing a file.
	 * 
	 * @param path
	 */
	public static boolean move(Path path) throws Exception {
		Path destination = getDestinationForThisSource(path);
		if (destination != null) {
			LOGGER.info("\""+path.toString()+"\" moving to: \""+destination.toString()+"\"");
			Files.move(path, destination, StandardCopyOption.REPLACE_EXISTING);

			// index the current folder
			Index.add(destination);

			// DOWNLOAD SUBTITLE DISABLED DOESN'T WORK :)
			// download subtitle
			// if (isMovie(destination)) {
			// Subtitles.addPath(destination);
			// }
		}
		return destination != null;
	}

	private static Path getDestinationForThisSource(Path path) {
		String filename = path.getFileName().toString().toLowerCase().replace(" ", ".");

		String parentFilename = path.getParent().getFileName().toString().toLowerCase().replace(" ", ".");

		Path destination = null;
		if (isSeries(filename) || isSeries(parentFilename)) {
			destination = buildDestination(filename);
		} else if (isMovie(filename)) {
			destination = Paths.get(MOVIES).resolve(filename);
		} else if (isSubtitle(filename)) {
			destination = Paths.get(MOVIES).resolve(filename);
		}
		return destination;
	}

	private static Path buildDestination(final String filename) {
		Path destination = Paths.get(SERIES);
		String[] fileNameParts = filename.replaceAll("\\s", "\\.").split("\\.s\\d{1,2}");
		destination = destination.resolve(fileNameParts[0]);
		Matcher serie = SERIE.matcher(filename);
		serie.find();
		destination = destination.resolve(serie.group().substring(1));
		try {
			Files.createDirectories(destination);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
		destination = destination.resolve(filename);
		return destination;
	}

	public static boolean isMovie(String filename) {
		Matcher matcher = MOVIE.matcher(filename);
		if (matcher.find()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isSubtitle(String filename) {
		Matcher matcher = SUBTITLE.matcher(filename);
		if (matcher.find()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isSeries(String filename) {
		Matcher matcher = SERIE.matcher(filename);
		if (matcher.find()) {
			return true;
		} else {
			return false;
		}
	}
}
