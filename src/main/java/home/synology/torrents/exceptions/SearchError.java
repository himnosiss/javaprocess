package home.synology.torrents.exceptions;

public class SearchError extends Exception {
	private static final long serialVersionUID = 4070540937333407699L;

	public SearchError(String message) {
		super(message);
	}
}
