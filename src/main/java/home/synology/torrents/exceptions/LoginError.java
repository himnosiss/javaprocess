package home.synology.torrents.exceptions;

public class LoginError extends Exception {
	private static final long serialVersionUID = 4070540937333407699L;

	public LoginError(String message) {
		super(message);
	}
}
