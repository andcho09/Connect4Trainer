package connect4.api;

/**
 * A disc that is placed onto the board. Discs are just bytes. This is really just a helper class.
 */
public enum Disc {

	RED((byte) 1, 'r'), YELLOW((byte) 2, 'y');

	private final byte value;
	private final char symbol;

	private Disc(final byte value, final char symbol) {
		this.value = value;
		this.symbol = symbol;
	}

	public byte getValue() {
		return this.value;
	}

	public char getSymbol() {
		return this.symbol;
	}

	/**
	 * Converts a disc byte to a human readable symbol.
	 * @param disc the disc
	 * @return 'r' for red, 'y' for yellow and '.' for everything else
	 */
	public static final char toSymbol(final Disc disc) {
		if (disc == null) {
			return '.';
		}
		return disc.symbol;
	}

	/**
	 * Gets the disc from the value.
	 * @param value the value
	 * @return a disc or <code>null</code> if the value couldn't be understood
	 */
	public static final Disc getDisc(final byte value) {
		switch (value) {
			case 1:
				return RED;
			case 2:
				return YELLOW;
			default:
				return null;
		}
	}

	/**
	 * Gets the disc from the value.
	 * @param value the value
	 * @return a disc or <code>null</code> if the value couldn't be understood
	 */
	public static final Disc getDisc(final char value) {
		switch (value) {
			case 'r':
				return RED;
			case 'y':
				return YELLOW;
			default:
				return null;
		}
	}

	/**
	 * @param disc the disc
	 * @return the opposite of the specified disc or null if disc is null
	 */
	public static final Disc getOpposite(final Disc disc) {
		if (null == disc) {
			return null;
		} else if (RED.equals(disc)) {
			return YELLOW;
		} else {
			return RED;
		}
	}
}
