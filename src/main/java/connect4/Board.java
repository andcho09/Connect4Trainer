package connect4;

/**
 * <p>
 * Represents the board.
 * </p>
 * <p>
 * Limitations:
 * <ul>
 * <li>Only supports up board with a maximum of 16 rows
 * </ul>
 * </p>
 * <p>
 * Implementation details:
 * <ul>
 * <li>A board is made up of int[] where each element is a column. A row within the column needs two
 * bits, hence the 16 row max limitation.
 * </ul>
 * </p>
 */
public class Board {

	final int nCols;
	final int nRows;
	final int[] board;

	public Board(final int nCols, final int nRows) throws IllegalArgumentException {
		if (nRows > 16) {
			throw new IllegalArgumentException("Sorry, the maximum number of rows is 16");
		}
		if (nCols <= 0) {
			throw new IllegalArgumentException("The number of columns must be greater than 0");
		}
		if (nRows <= 0) {
			throw new IllegalArgumentException("The number of rows must be greater than 0");
		}
		this.nCols = nCols;
		this.nRows = nRows;
		this.board = new int[nCols];
	}

	/**
	 * Get the disk at the specified position.
	 * @param col the col index (0-based)
	 * @param row the row index (0-based)
	 * @return the {@link Disc}
	 */
	public byte getDisk(int col, int row) {
		if (col < 0 || col >= nCols) {
			throw new IllegalArgumentException("Column position " + col + " is out of bounds");
		}
		if (row < 0 || row >= nRows) {
			throw new IllegalArgumentException("Row position " + row + " is out of bounds");
		}

		int column = board[col];
		column = column >>> row;
		int mask = 0x3;
		return (byte) (column & mask);
	}

	/**
	 * Puts a disk at the specified column.
	 * @param col the column position (0-based)
	 * @param disc the disc
	 * @throws IllegalMoveException if the move is illegal
	 */
	public void putDisc(int col, byte disc) throws IllegalMoveException {

	}

	/**
	 * Prints the board
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(nCols * (nRows + 1));
		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				sb.append(Disc.toString(getDisk(j, i)));
			}
			sb.append('\n');
		}
		return sb.toString();
	}
}
