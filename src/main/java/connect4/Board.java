package connect4;

import java.util.Arrays;

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
 * <li>A board is made up of int[] where each element is a column. A row within the column needs two bits, hence the 16 row max limitation.
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
	 * Copy constructor.
	 * @param board the board to copy
	 */
	public Board(final Board board) {
		this.nCols = board.nCols;
		this.nRows = board.nRows;
		this.board = Arrays.copyOf(board.board, board.board.length);
	}

	/**
	 * Get the disc at the specified position. (0,0) is bottom-left
	 * @param col the col index (0-based)
	 * @param row the row index (0-based)
	 * @return the {@link Disc} or <code>null</code> if no disc is present
	 */
	public Disc getDisc(final int col, final int row) {
		if (col < 0 || col >= nCols) {
			throw new IllegalArgumentException("Column position " + col + " is out of bounds");
		}
		if (row < 0 || row >= nRows) {
			throw new IllegalArgumentException("Row position " + row + " is out of bounds");
		}

		return Disc.getDisc((byte) getDiscByte(col, row));
	}

	/**
	 * Get the disc at the specified position. There is no validation like the public {@link #getDisc(int, int)} method
	 * @param col the col index (0-based)
	 * @param row the row index (0-based)
	 * @return the value of the disc or 0 if there is no disc
	 */
	int getDiscByte(final int col, final int row) {
		int column = board[col];
		column = column >>> row * 2;
		return column & 0x3;
	}

	/**
	 * Puts a disk at the specified column.
	 * @param col the column position (0-based)
	 * @param disc the disc
	 * @return the row number at which the disc was placed (0-based)
	 * @throws IllegalMoveException if the move is illegal
	 */
	public int putDisc(final int col, final Disc disc) throws IllegalMoveException {
		if (col < 0 || col >= nCols) {
			throw new IllegalMoveException(disc, col, "Column position " + col + " is out of bounds");
		}
		if (disc == null) {
			throw new IllegalMoveException(disc, -1, "Disc must not be null");
		}

		final int column = board[col];
		for (int r = 0; r < nRows; r++) {
			if (column >>> r * 2 == 0) {
				board[col] = column | disc.getValue() << r * 2;
				return r;
			}
		}

		throw new IllegalMoveException(disc, col, "Cannot place disc at column " + col + " because it is full");
	}

	/**
	 * @return <code>true</code> if there are no more moves to be played, else <code>false</code>
	 */
	public boolean isFull() {
		for (int c = 0; c < nCols; c++) {
			if (getDiscByte(c, nRows - 1) == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Prints the board
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder((nCols + 1) * nRows);
		for (int r = nRows - 1; r >= 0; r--) {
			for (int c = 0; c < nCols; c++) {
				final Disc disc = getDisc(c, r);
				sb.append(Disc.toSymbol(disc));
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	/**
	 * @return the number of columns this board has
	 */
	public int getNumCols() {
		return nCols;
	}

	/**
	 * @return the number of rows this board has
	 */
	public int getNumRows() {
		return nRows;
	}

	/**
	 * @return the int[] backing this board. Only meant to be called by helper classes
	 */
	int[] getDelegateBoard() {
		return board;
	}
}
