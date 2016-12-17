package home.synology.torrents;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IgnoreCrapFilter implements Filter<Path> {

	static Logger LOGGER = LoggerFactory.getLogger(IgnoreCrapFilter.class);

	List<String> crapExtensions = new ArrayList<String>();

	public IgnoreCrapFilter(String pathToFileWithCrapExtensions) {
		try {
			crapExtensions = Files.readAllLines(Paths.get(pathToFileWithCrapExtensions));
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
	}

	@Override
	public boolean accept(Path entry) throws IOException {
		for (String extension : crapExtensions) {
			if (extension.trim().length() > 0 && (entry.toString().endsWith(extension) || entry.toString().startsWith(extension))) {
				LOGGER.info(entry.toString() + " rejected!");
				return false;
			}
		}
		return true;
	}
}
