package connect4;

/**
 * Represents an illegal move.
 */
public class IllegalMoveException extends GameException {

	private static final long serialVersionUID = 1L;

	private final int column;
	private final Disc disc;

	public IllegalMoveException(final ErrorCode errorCode, final Disc disc, final int column, final String message) {
		super(errorCode, message);
		this.column = column;
		this.disc = disc;
	}

	/**
	 * @return the column that was attempted to be played in which created the illegal move
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * @return the {@link Disc} that created the illegal move
	 */
	public Disc getDisc() {
		return disc;
	}
}
