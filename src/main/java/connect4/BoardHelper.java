package connect4;

/**
 * Utility/helper class.
 */
public class BoardHelper {

	private static final int WIN_MASK_VERT_RED = 85; // 01 01 01 01
	private static final int WIN_MASK_VERT_YELLOW = 170; // 10 10 10 10

	private BoardHelper() {
	}

	/**
	 * Checks if the board is in a win condition (i.e. someone has four of their discs in a row)
	 * @param board the {@link Board} to check
	 * @return the {@link Disc} of the winner or <code>null</code> if there is no winner
	 */
	public static Disc hasWinner(final Board b) {
		if (hasWinner(b, Disc.RED)) {
			return Disc.RED;
		} else if (hasWinner(b, Disc.YELLOW)) {
			return Disc.YELLOW;
		}
		return null;
	}

	/**
	 * Checks if the board has been won by the player with the specified {@link Disc}
	 * @param board the {@link Board} to check
	 * @param disc the winner to check for
	 * @return <code>true</code> if the disc has one, else <code>false</code>
	 */
	static boolean hasWinner(final Board b, final Disc disc) {
		final int[] board = b.getDelegateBoard();

		// check vertical wins
		// this does bit shifting and masking to figure out if the column equals the magic win mask
		final int verticalWinMask = Disc.RED == disc ? WIN_MASK_VERT_RED : WIN_MASK_VERT_YELLOW;
		cols: for (final int element : board) {
			int column = element;
			for (int r = 0; r < b.getNumRows() - 3; r++) {
				if (column == 0) {
					continue cols; // can't win if no discs are in the column
				}
				if (verticalWinMask == (column & 255)) { // mask considers only the bottom 8 bits
					return true;
				}
				column = column >>> 2;
			}
		}

		// check horizontal wins
		for (int r = 0; r < b.getNumRows(); r++) {
			int counter = 0;

			// this optimisation checks the fourth column's disc. In a 7 column game, you can't win
			// horizontally without it
			int c = 0;
			if (b.getDiscByte(3, r) != disc.getValue()) {
				c = 4;
			}

			// this loop could check whether there's enough columns remaining to win
			for (; c < b.getNumCols(); c++) {
				if (b.getDiscByte(c, r) == disc.getValue()) {
					counter++;
					if (counter == 4) {
						return true;
					}
				} else {
					counter = 0;
				}
			}
		}

		// check diagonal wins in SW to NE direction
		int cAnchor = 0;
		int rAnchor = 0;
		for (; cAnchor < b.getNumCols() - 3; cAnchor++) {
			if (hasWinnerDiagonalSwNe(b, disc, cAnchor, rAnchor)) {
				return true;
			}
		}
		cAnchor = 0;
		for (; rAnchor < b.getNumRows() - 3; rAnchor++) {
			if (hasWinnerDiagonalSwNe(b, disc, cAnchor, rAnchor)) {
				return true;
			}
		}

		// check diagonal wins in SE to NW direction
		cAnchor = 3;
		rAnchor = 0;
		for (; cAnchor < b.getNumCols(); cAnchor++) {
			if (hasWinnerDiagonalSeNw(b, disc, cAnchor, rAnchor)) {
				return true;

			}
		}
		cAnchor = b.getNumCols() - 1;
		for (; rAnchor < b.getNumRows() - 3; rAnchor++) {
			if (hasWinnerDiagonalSeNw(b, disc, cAnchor, rAnchor)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks for wins in a diagonal SW to NE direction. It searches by starting in the specified
	 * anchor position and progressing diagonally to the NE.
	 * @param b the board to check
	 * @param disc the disc to check
	 * @param cAnchor the starting column position (0-based)
	 * @param rAnchor the starting col position (0-based)
	 * @return <code>true</code> if the specified disc won, else <code>false</code>
	 */
	private static boolean hasWinnerDiagonalSwNe(final Board b, final Disc disc, final int cAnchor,
			final int rAnchor) {
		// this optimisation checks the disc at 3 rows up and 3 right (i.e. the end of the
		// diagonal). You can't win diagonally without it
		int c = cAnchor;
		int r = rAnchor;
		if (b.getDiscByte(cAnchor + 3, rAnchor + 3) != disc.getValue()) {
			c += 4;
			r += 4;
		}

		int counter = 0;
		// early termination possible here, e.g. only 3 cols left but counter = 0
		for (; c < b.getNumCols() && r < b.getNumRows(); c++, r++) {
			if (b.getDiscByte(c, r) == disc.getValue()) {
				counter++;
				if (counter == 4) {
					return true;
				}
			} else {
				counter = 0;
			}

		}
		return false;
	}

	/**
	 * Checks for wins in a diagonal SE to NW direction. It searches by starting in the specified
	 * anchor position and progressing diagonally to the NW.
	 * @param b the board to check
	 * @param disc the disc to check
	 * @param cAnchor the starting column position (0-based)
	 * @param rAnchor the starting col position (0-based)
	 * @return <code>true</code> if the specified disc won, else <code>false</code>
	 */
	private static boolean hasWinnerDiagonalSeNw(final Board b, final Disc disc, final int cAnchor,
			final int rAnchor) {
		// this optimisation checks the disc at 3 rows up and 3 left (i.e. the end of the
		// diagonal). You can't win diagonally without it
		int c = cAnchor;
		int r = rAnchor;
		if (b.getDiscByte(cAnchor - 3, rAnchor + 3) != disc.getValue()) {
			c -= 4;
			r += 4;
		}

		int counter = 0;
		// early termination possible here, e.g. only 3 cols left but counter = 0
		for (; c >= 0 && r < b.getNumRows(); c--, r++) {
			if (b.getDiscByte(c, r) == disc.getValue()) {
				counter++;
				if (counter == 4) {
					return true;
				}
			} else {
				counter = 0;
			}

		}
		return false;
	}

	/**
	 * Calculates the mask required to find discs at the specified row in a column
	 * @param columnValue
	 * @param row the row to mask for (0-based)
	 * @return the mask
	 */
	static int maskRow(final int row) {
		return 3 >>> row * 2;
	}
}
