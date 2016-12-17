package home.synology.torrents.exceptions;

public class IncompleteDownloadItemException extends Exception {
	private static final long serialVersionUID = 1L;

	public IncompleteDownloadItemException(String message) {
		super(message);
	}

}
