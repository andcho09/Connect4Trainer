package connect4;

/**
 * Represents an illegal move.
 */
public class IllegalMoveException extends Exception {

	private static final long serialVersionUID = 1L;

	public IllegalMoveException(final String message) {
		super(message);
	}
}
