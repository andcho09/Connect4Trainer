package connect4;

/**
 * Denotes a generic Connect 4 Trainer exception.
 */
public class GameException extends Throwable {

	public static enum ErrorCode {
		ALREADY_WON, BOARD_FULL, COLUMN_FULL, OUT_OF_BOUNDS, UNKNOWN;
	}

	private static final long serialVersionUID = 1L;
	private final ErrorCode errorCode;

	public GameException(final ErrorCode errorCode, final String message, final Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public GameException(final ErrorCode errorCode, final String message) {
		this(errorCode, message, null);
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
