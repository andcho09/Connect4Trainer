package connect4;

/**
 * Represents an illegal move.
 */
public class IllegalMoveException extends Exception {

	private static final long serialVersionUID = 1L;

	private final Disc disc;

	public IllegalMoveException(final Disc disc, final String message) {
		super(message);
		this.disc = disc;
	}

	/**
	 * @return the {@link Disc} that created the illegal move
	 */
	public Disc getDisc() {
		return disc;
	}
}
