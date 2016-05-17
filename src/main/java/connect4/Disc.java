package connect4;

/**
 * A disc that is placed onto the board. Discs are just bytes. This is really just a helper class.
 */
public class Disc {

	public static final byte EMPTY = 0;
	public static final byte RED = 1;
	public static final byte YELLOW = 2;

	private Disc() {
	}

	/**
	 * Converts a disc byte to a human readable symbol.
	 * @param disc the disc
	 * @return '.' for empty, 'r' for red, 'y' for yellow and '?' for everything else
	 */
	public static final char toString(byte disc) {
		switch (disc) {
			case EMPTY:
				return '.';
			case RED:
				return 'r';
			case YELLOW:
				return 'y';
			default:
				return '?';
		}
	}
}
