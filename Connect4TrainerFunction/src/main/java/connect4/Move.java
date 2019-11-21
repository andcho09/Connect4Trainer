package connect4;

/**
 * Represents a move by a player.
 */
public class Move {

	private final int col;
	private final int row;
	private final Disc disc;

	/**
	 * A move.
	 * @param disc the {@link Disc} that was played during this move
	 * @param col the column position (0-based) of the move
	 * @param row the row position (0-based) of the move
	 */
	public Move(final Disc disc, final int col, final int row) {
		this.col = col;
		this.row = row;
		this.disc = disc;
	}

	public int getCol() {
		return col;
	}

	public int getRow() {
		return row;
	}

	public Disc getDisc() {
		return disc;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + col;
		result = prime * result + (disc == null ? 0 : disc.hashCode());
		result = prime * result + row;
		return result;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (getClass() != o.getClass()) {
			return false;
		}
		final Move other = (Move) o;
		if (col != other.col) {
			return false;
		}
		if (disc != other.disc) {
			return false;
		}
		if (row != other.row) {
			return false;
		}
		return true;
	}
}
