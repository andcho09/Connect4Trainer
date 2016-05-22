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
	 * Checks if the board is in a win condition (i.e. someone has four of their discs in a row).
	 * Searching is optimised because we know the last move played.
	 * @param board the {@link Board} to check
	 * @param lastMove the last played {@link Move}
	 * @return the {@link Disc} of the winner or <code>null</code> if there is no winner
	 */
	public static Disc hasWinner(final Board b, final Move lastMove) {
		final int verticalWinMask = Disc.RED == lastMove.getDisc() ? WIN_MASK_VERT_RED
				: WIN_MASK_VERT_YELLOW;
		if (hasWinnerVertical(b, verticalWinMask, lastMove.getCol(), lastMove.getRow())) {
			return lastMove.getDisc();
		}
		if (hasWinnerHorizontal(b, lastMove.getDisc(), lastMove.getCol(), lastMove.getRow())) {
			return lastMove.getDisc();
		}
		if (hasWinnerDiagonalSwNe(b, lastMove.getDisc(), lastMove.getCol(), lastMove.getRow())) {
			return lastMove.getDisc();
		}
		if (hasWinnerDiagonalSeNw(b, lastMove.getDisc(), lastMove.getCol(), lastMove.getRow())) {
			return lastMove.getDisc();
		}
		return null;
	}

	/**
	 * Checks if the board has been won by the player with the specified {@link Disc}
	 * @param board the {@link Board} to check
	 * @param disc the winner to check for
	 * @return <code>true</code> if the disc has one, else <code>false</code>
	 */
	private static boolean hasWinner(final Board b, final Disc disc) {
		// check vertical wins
		// this does bit shifting and masking to figure out if the column equals the magic win mask
		final int verticalWinMask = Disc.RED == disc ? WIN_MASK_VERT_RED : WIN_MASK_VERT_YELLOW;
		for (int c = 0; c < b.getNumCols(); c++) {
			if (hasWinnerVertical(b, verticalWinMask, c, -1)) {
				return true;
			}
		}

		// check horizontal wins
		for (int r = 0; r < b.getNumRows(); r++) {
			for (int c = 3; c < b.getNumCols() - 3; c++) {
				if (hasWinnerHorizontal(b, disc, c, r)) {
					return true;
				}
			}
		}

		// check diagonal wins in SW to NE direction
		final int cMax = b.getNumCols() - 1;
		final int rMax = b.getNumRows() - 1;
		int cAnchor = 0;
		int rAnchor = 0;
		for (; cAnchor < b.getNumCols() - 3; cAnchor++) {
			if (hasWinnerDiagonalSwNe(b, disc, cAnchor, rAnchor, cMax, rMax)) {
				return true;
			}
		}
		cAnchor = 0;
		rAnchor = 1;
		for (; rAnchor < b.getNumRows() - 3; rAnchor++) {
			if (hasWinnerDiagonalSwNe(b, disc, cAnchor, rAnchor, cMax, rMax)) {
				return true;
			}
		}

		// check diagonal wins in SE to NW direction
		cAnchor = 3;
		rAnchor = 0;
		for (; cAnchor < b.getNumCols(); cAnchor++) {
			if (hasWinnerDiagonalSeNw(b, disc, cAnchor, rAnchor, 0, rMax)) {
				return true;

			}
		}
		cAnchor = cMax;
		rAnchor = 1;
		for (; rAnchor < b.getNumRows() - 3; rAnchor++) {
			if (hasWinnerDiagonalSeNw(b, disc, cAnchor, rAnchor, 0, rMax)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if a {@link Disc} won at vertically at a specified position.
	 * @param b the board to check
	 * @param discWinMask the disc to check in bitmask form
	 * @param colPos the column position to check (0-based)
	 * @param rowPos the row position to check (0-based)
	 * @return <code>true</code> if disc won at column colPol, else <code>false</code>
	 */
	private static boolean hasWinnerVertical(final Board b, final int discWinMask, final int colPos,
			final int rowPos) {
		// this implementation doesn't use row-position, as it's likely faster to just bitmask
		int column = b.getDelegateBoard()[colPos];
		for (int r = 0; r < b.getNumRows() - 3; r++) {
			if (column == 0) {
				return false; // can't win if no discs are in the column
			}
			if (discWinMask == (column & 255)) { // mask considers only the bottom 8 bits
				return true;
			}
			column = column >>> 2;
		}
		return false;
	}

	/**
	 * Checks if the {@link Disc} won horizontally at a specified position.
	 * @param b the board to check
	 * @param disc the {@link Disc} to check
	 * @param colPos the row position to check (0-based)
	 * @param rowPos the row position to check (0-based)
	 * @return <code>true</code> if disc won at column colPol, else <code>false</code>
	 */
	private static boolean hasWinnerHorizontal(final Board b, final Disc disc, final int colPos,
			final int rowPos) {
		int c = Math.max(0, colPos - 3);
		final int maxColPos = Math.min(b.getNumCols(), colPos + 4);

		// this loop could check whether there's enough columns remaining to win
		int counter = 0;
		for (; c < maxColPos; c++) {
			if (b.getDiscByte(c, rowPos) == disc.getValue()) {
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
	 * Checks for wins in a diagonal SW to NE direction played at the specified position.
	 * @param b the board to check
	 * @param disc the disc to check
	 * @param cAnchor the starting column position (0-based)
	 * @param rAnchor the starting col position (0-based)
	 * @return <code>true</code> if the specified disc won, else <code>false</code>
	 */
	private static boolean hasWinnerDiagonalSwNe(final Board b, final Disc disc, final int cAnchor,
			final int rAnchor) {
		final int shiftSw = Math.min(3, Math.min(cAnchor, rAnchor));
		final int c = cAnchor - shiftSw;
		final int r = rAnchor - shiftSw;

		final int shiftNe = Math.min(3,
				Math.min(b.getNumCols() - cAnchor, b.getNumRows() - rAnchor));
		final int cMax = cAnchor + shiftNe;
		final int rMax = rAnchor + shiftNe;

		return hasWinnerDiagonalSwNe(b, disc, c, r, cMax, rMax);
	}

	/**
	 * Checks for wins in a diagonal SW to NE direction. It searches by starting in the specified
	 * start position (cMin, rMin) and progressing diagonally to the NE until position (cMax, rMax)
	 * is reached.
	 * It does not search backwards (i.e. in the SW direction).
	 * @param b the board to check
	 * @param disc the disc to check
	 * @param cMin the starting column position (0-based)
	 * @param rMin the starting col position (0-based)
	 * @param cMax the max column to search
	 * @param rMax the max row to search
	 * @return <code>true</code> if the specified disc won, else <code>false</code>
	 */
	private static boolean hasWinnerDiagonalSwNe(final Board b, final Disc disc, final int cMin,
			final int rMin, final int cMax, final int rMax) {
		if (rMax - rMin < 3) {
			return false; // can't win, not enough space
		}

		int counter = 0;

		int c = cMin;
		int r = rMin;
		// this optimisation checks the disc at 3 rows up and 3 right (i.e. the end of the
		// diagonal). You can't win diagonally without it
		if (b.getDiscByte(cMin + 3, rMin + 3) != disc.getValue()) {
			c += 4;
			r += 4;
		} else {
			counter = 1;
		}

		// early termination possible here, e.g. only 3 cols left but counter = 0
		for (; c <= cMax && r <= rMax; c++, r++) {
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
		final int shiftSe = Math.min(3, Math.min(b.getNumCols() - 1 - cAnchor, rAnchor));
		final int cMax = cAnchor + shiftSe;
		final int rMin = rAnchor - shiftSe;

		final int shiftNw = Math.min(3, Math.min(cAnchor, b.getNumRows() - 1 - rAnchor));
		final int cMin = cAnchor - shiftNw;
		final int rMax = rAnchor + shiftNw;

		return hasWinnerDiagonalSeNw(b, disc, cMax, rMin, cMin, rMax);
	}

	/**
	 * Checks for wins in a diagonal SE to NW direction. It searches by starting in the specified
	 * start position (cMax, rMin) and progressing diagonally to the NW until position (cMin, rMax)
	 * is reached. It does not search backwards (i.e. in the SE direction).
	 * @param b the board to check
	 * @param disc the disc to check
	 * @param cMax the starting column position (0-based)
	 * @param rMin the starting col position (0-based)
	 * @param cMax the max column to search
	 * @param rMax the max row to search
	 * @return <code>true</code> if the specified disc won, else <code>false</code>
	 */
	private static boolean hasWinnerDiagonalSeNw(final Board b, final Disc disc, final int cMax,
			final int rMin, final int cMin, final int rMax) {
		if (rMax - rMin < 3) {
			return false; // can't win, not enough space
		}

		int counter = 0;

		int c = cMax;
		int r = rMin;
		// this optimisation checks the disc at 3 rows up and 3 left (i.e. the end of the
		// diagonal). You can't win diagonally without it
		if (b.getDiscByte(cMax - 3, rMin + 3) != disc.getValue()) {
			c -= 4;
			r += 4;
		} else {
			counter = 1;
		}

		// early termination possible here, e.g. only 3 cols left but counter = 0
		for (; c >= cMin && r <= rMax; c--, r++) {
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
