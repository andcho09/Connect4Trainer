package connect4.loader;

public class InvalidBoardFormatException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidBoardFormatException(final String message) {
		super(message);
	}

	public InvalidBoardFormatException(final String message, final Throwable t) {
		super(message, t);
	}
}
