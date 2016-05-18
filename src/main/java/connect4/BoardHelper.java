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
		final int verticalWinMask = Disc.RED == disc ? WIN_MASK_VERT_RED : WIN_MASK_VERT_YELLOW;

		cols: for (final int element : board) {
			int column = element;
			for (int r = 0; r < b.getNumRows() - 3; r++) {
				if (column == 0) {
					continue cols; // can't win if no discs are in the column
				}
				if (verticalWinMask == (column & 255)) { // mask considers only the bottom 4 bits
					return true;
				}
				column = column >>> 2;
			}
		}

		// check horizontal wins

		// check diagonal wins
		return false;
	}
}
