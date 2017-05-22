package connect4;

/**
 * Denotes a generic Connnect 4 Trainer exception.
 */
public class GameException extends Throwable {

	private static final long serialVersionUID = 1L;

	public GameException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public GameException(final String message) {
		super(message);
	}
}
