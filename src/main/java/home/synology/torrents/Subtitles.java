package home.synology.torrents;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wtekiela.opensub4j.api.OpenSubtitles;
import com.github.wtekiela.opensub4j.impl.OpenSubtitlesImpl;
import com.github.wtekiela.opensub4j.response.SubtitleInfo;

public class Subtitles {

	private static Set<Path> paths = new HashSet<Path>();

	private static Logger LOGGER = LoggerFactory.getLogger(Subtitles.class);

	private OpenSubtitles openSubtitlesApi = null;
	// private final String[] LANGUAGES=new String[]{"ger", "deu", "eng, "rum"};
	private final String[] LANGUAGES = new String[] { "rum" };

	public Subtitles() {
		URL openSubtitlesServer = null;
		try {
			openSubtitlesServer = new URL("http://api.opensubtitles.org/xml-rpc");
		} catch (MalformedURLException e) {
			LOGGER.error(e.getMessage());
		}
		openSubtitlesApi = new OpenSubtitlesImpl(openSubtitlesServer);
	}

	private boolean login() {
		boolean loggedin = false;
		try {
			openSubtitlesApi.login("himnosiss", "hpdeskjet690c", "en", "himnosiss 1");
			loggedin = true;
		} catch (XmlRpcException e) {
			LOGGER.error(e.getMessage());
		}
		return loggedin;
	}

	private void logout() {
		try {
			openSubtitlesApi.logout();
		} catch (XmlRpcException e) {
			LOGGER.error(e.getMessage());
		}
	}

	private String search(Path path) {
		if (path == null)
			return null;

		SubtitleInfo subtitleInfo = new SubtitleInfo();
		try {
			List<SubtitleInfo> subtitles = openSubtitlesApi.searchSubtitles(LANGUAGES[0], path.toFile());
			subtitles.sort(new Comparator<SubtitleInfo>() {
				public int compare(SubtitleInfo o1, SubtitleInfo o2) {
					return o2.getId() - o1.getId();
				}
			});
			if (!subtitles.isEmpty()) {
				subtitleInfo = subtitles.get(0);
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		} catch (XmlRpcException e) {
			LOGGER.error(e.getMessage());
		}

		return subtitleInfo.getDownloadLink();

	}

	/**
	 * @param path
	 *            should include the filename if this path is a folder then all
	 *            the files in the folder will be processed
	 */
	public static void downloadSubtitles() {
		Subtitles subtitles = new Subtitles();
		if (subtitles.login()) {
			for (Path path : paths) {
				// exclude some files
				if (subtitles.subExists(path)) {
					LOGGER.info("\"" + path.toString() + "\" has a subtitle, skip");
					continue;
				}
				String url = subtitles.search(path);
				try {
					subtitles.downloadFile(path, url);
				} catch (Exception e) {
					LOGGER.error(e.getMessage());
				}
			}
			subtitles.logout();
		}
	}

	private boolean subExists(Path path) {
		String filename = path.toString();
		String srt = filename.substring(0, filename.length() - 3) + "srt";
		String sub = filename.substring(0, filename.length() - 3) + "sub";
		Path srtPath = Paths.get(srt);
		Path subPath = Paths.get(sub);
		if (Files.exists(subPath, LinkOption.NOFOLLOW_LINKS)) {
			return true;
		}
		if (Files.exists(srtPath, LinkOption.NOFOLLOW_LINKS)) {
			return true;
		}
		return false;

	}

	private void downloadFile(Path path, String subtitleDownloadLink) throws IOException {
		if (subtitleDownloadLink == null) {
			return;
		}
		URL downloadUrl = new URL(subtitleDownloadLink);
		GZIPInputStream gzis = new GZIPInputStream(downloadUrl.openStream());

		// write the inputStream to a FileOutputStream
		String filename = path.toString();
		filename = filename.substring(0, filename.length() - 3) + "srt";
		FileOutputStream outputStream = new FileOutputStream(new File(filename));

		int read = 0;
		byte[] bytes = new byte[1024];

		while ((read = gzis.read(bytes)) != -1) {
			outputStream.write(bytes, 0, read);
		}
		outputStream.close();
	}

	public static void main(String[] args) {
		Path path = Paths
				.get("\\\\DISKSTATION\\video\\tvshow\\black.mirror\\02\\Black.Mirror.S02E01.Be.Right.Back.1080p.WEB-DL.AAC2.0.H.264-Coo7.mkv");
		Subtitles.addPath(path);
		downloadSubtitles();
	}

	public static void addPath(Path destination) {
		paths.add(destination);
	}
}
