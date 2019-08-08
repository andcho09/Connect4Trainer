package connect4.trainer;

import java.util.LinkedList;
import java.util.List;

import connect4.Board;
import connect4.BoardHelper;
import connect4.Disc;
import connect4.IllegalMoveException;
import connect4.Move;

/**
 * A factory for {@link ColumnAnalyser}s which look at the playing in an individual column. This is very simple and does not evaluate beyond
 * one disc being played unlike the {@link AbstractForceBoardAnalyser} classes.
 */
public class ColumnAnalyserFactory {

	public static abstract class ColumnAnalyser {

		/**
		 * Flags the given column position with various facts (flags). For example, will playing in the column win the game? Or will playing
		 * in the column let opponent play ontop of my disc and win? The flags have no meaning, they're scored by the
		 * {@link ScoringAlgorithm} later.
		 * @param board the {@link Board} to analyse. This is the real board so if the
		 *        {@link ColumnAnalyser} needs to modify it, it should do so using
		 *        {@link Board#Board(Board)} (i.e. the clone constructor)
		 * @param currentPlayer the {@link Disc} of the current player
		 * @param column the column (0-based) we're analysing
		 * @param currentAnalysis the analysis conducted so far. Implementors are expected to modify
		 *        this instance
		 * @return <code>true</code> if we added a flag, otherwise <code>false</code>
		 */
		public abstract boolean flag(final Board board, final Disc currentPlayer, final int column, final ColumnAnalysis currentAnalysis);
	}

	/** Unplayable or win now */
	private static final ColumnAnalyser WIN_NOW = new ColumnAnalyser() {
		@Override
		public boolean flag(final Board board, final Disc currentPlayer, final int column, final ColumnAnalysis currentAnalysis) {
			int row = -1;
			final Board newBoard = new Board(board);
			try {
				row = newBoard.putDisc(column, currentPlayer);
			} catch (final IllegalMoveException e) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_UNPLAYABLE);
				return true;
			}

			final Disc winner = BoardHelper.hasWinner(newBoard, new Move(currentPlayer, column, row));
			if (winner == currentPlayer) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_WIN_1);
				return true;
			}
			return false;
		}
	};

	/** Playing here blocks opponent from winning in their next move */
	private static final ColumnAnalyser BLOCK_LOSS_1 = new ColumnAnalyser() {
		@Override
		public boolean flag(final Board board, final Disc currentPlayer, final int column, final ColumnAnalysis currentAnalysis) {
			final Disc opponentDisc = Disc.getOpposite(currentPlayer);
			final ColumnAnalysis opponentAnalysis = new ColumnAnalysis(currentAnalysis);
			if (WIN_NOW.flag(board, opponentDisc, column, opponentAnalysis)) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_BLOCK_LOSS_1);
				return true;
			}
			return false;
		}
	};

	/** Playing here allows the opponent to win by playing above us */
	private static final ColumnAnalyser ENABLE_OPPONENT_WIN = new ColumnAnalyser() {
		@Override
		public boolean flag(final Board board, final Disc currentPlayer, final int column, final ColumnAnalysis currentAnalysis) {
			final Board newBoard = new Board(board);
			try {
				newBoard.putDisc(column, currentPlayer);
			} catch (final IllegalMoveException e) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_UNPLAYABLE);
				return true;
			}

			final Disc oponentPlayer = Disc.getOpposite(currentPlayer);
			int row;
			try {
				row = newBoard.putDisc(column, oponentPlayer);
			} catch (final IllegalMoveException ignored) {
				return false;
			}

			if (oponentPlayer.equals(BoardHelper.hasWinner(newBoard, new Move(oponentPlayer, column, row)))) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_ENABLE_OPPONENT_WIN);
				return true;
			}
			return false;
		}
	};

	/** Playing here gives us more than one different column to win (i.e. execute a trap) */
	private static final ColumnAnalyser TRAP_MORE_THAN_ONE = new ColumnAnalyser() {
		@Override
		public boolean flag(final Board board, final Disc currentPlayer, final int column, final ColumnAnalysis currentAnalysis) {
			final Board newBoard = new Board(board);
			try {
				newBoard.putDisc(column, currentPlayer);
			} catch (final IllegalMoveException e) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_UNPLAYABLE);
				return true;
			}

			int winCounter = 0;
			final int startColumn = BoardHelper.getMinColumnSpan(board, column);
			final int endColumn = BoardHelper.getMaxColumnSpan(board, column);
			for (int i = startColumn; i <= endColumn; i++) {
				// A board two moves ahead
				final Board newBoard2Ahead = new Board(newBoard);
				int row;
				try {
					row = newBoard2Ahead.putDisc(i, currentPlayer);
				} catch (final IllegalMoveException e) {
					continue;
				}
				if (currentPlayer.equals(BoardHelper.hasWinner(newBoard2Ahead, new Move(currentPlayer, i, row)))) {
					winCounter++;
				}
			}

			if (winCounter > 1) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE);
				return true;
			}
			return false;
		}
	};

	/**
	 * Playing here blocks the opponent gaining more than one different column to win (i.e. execute a trap)
	 */
	private static final ColumnAnalyser BLOCK_TRAP_MORE_THAN_ONE = new ColumnAnalyser() {
		@Override
		public boolean flag(final Board board, final Disc currentPlayer, final int column, final ColumnAnalysis currentAnalysis) {
			final Disc opponentDisc = Disc.getOpposite(currentPlayer);
			final ColumnAnalysis opponentAnalysis = new ColumnAnalysis(currentAnalysis);
			if (TRAP_MORE_THAN_ONE.flag(board, opponentDisc, column, opponentAnalysis)) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE);
				return true;
			}
			return false;
		}
	};

	/**
	 * Playing here creates a three in a row setup where the opponent can't block as there's a gap below the spot that completes the
	 * 4-in-a-row. I.e. someone has to play below that spot first. This could set up a win later or at least shut down the column. Also
	 * flags double 3-in-a-row setups where playing in the column creates one 3-in-a-row setup ontop of another.
	 */
	private static final ColumnAnalyser MAKE_3_SETUP = new ColumnAnalyser() {
		@Override
		public boolean flag(final Board board, final Disc currentPlayer, final int column, final ColumnAnalysis currentAnalysis) {
			int row;
			final Board newBoard = new Board(board);
			try {
				row = newBoard.putDisc(column, currentPlayer);
			} catch (final IllegalMoveException e) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_UNPLAYABLE);
				return true;
			}

			List<int[]> spans;
			int[] leftMost;
			int[] rightMost;

			// Horizontal check
			if (row > 0) { // On the bottom row the opponent can block next move, that's forced play
				leftMost = new int[] { BoardHelper.getMinColumnSpan(newBoard, column), row };
				rightMost = new int[] { BoardHelper.getMaxColumnSpan(newBoard, column), row };
				if (flagSetups(currentAnalysis, newBoard, currentPlayer, leftMost[0], leftMost[1], rightMost[0], rightMost[1], 1, 0)) {
					return true;
				}
			}

			// Diagonal SW-NE check
			spans = BoardHelper.getDiagonalSwNeSpans(newBoard, column, row);
			leftMost = spans.get(0);
			rightMost = spans.get(1);
			if (flagSetups(currentAnalysis, newBoard, currentPlayer, leftMost[0], leftMost[1], rightMost[0], rightMost[1], 1, 1)) {
				return true;
			}

			// Diagonal SE-NW check
			spans = BoardHelper.getDiagonalSeNwSpans(newBoard, column, row);
			leftMost = spans.get(0);
			rightMost = spans.get(1);
			if (flagSetups(currentAnalysis, newBoard, currentPlayer, rightMost[0], rightMost[1], leftMost[0], leftMost[1], 1, -1)) {
				return true;
			}

			return false;
		}

		private boolean flagSetups(final ColumnAnalysis currentAnalysis, final Board board, final Disc currentPlayer, final int colStart,
				final int rowStart, final int colEnd, final int rowEnd, final int colMod, final int rowMod) {
			final int minCol = colStart;
			final int maxCol = colEnd;
			if (maxCol - minCol >= 3) { // has to be at least 4 columns
				spread: for (int c = minCol, r = rowStart; c <= maxCol - 3; c = c + colMod, r = r + rowMod) { // check each span
					int gapCol = -1;
					int gapRow = -1;
					for (int i = 0; i < 4; i++) { // progress the span 4 at at time
						final Disc disc = board.getDisc(c + i, r + i * rowMod);
						if (disc == null) {
							if (gapCol != -1) {
								continue spread; // Two gaps, can't make 4
							} else {
								gapCol = c + i;
								gapRow = r + i * rowMod;
							}
						} else if (!disc.equals(currentPlayer)) {
							continue spread; // Opponent disc, can't make 4
						}
					}
					if (gapRow > 0 && board.getDisc(gapCol, gapRow - 1) == null) {
						currentAnalysis.addCondition(ColumnAnalysis.FLAG_MAKE_3_SETUP);
						if (isDoubleSetup(board, currentPlayer, c, r, gapCol, colMod, rowMod)) {
							currentAnalysis.addCondition(ColumnAnalysis.FLAG_MAKE_3_DOUBLE_SETUP);
						}
						return true;
					}
				}
			}
			return false;
		}

		private boolean isDoubleSetup(final Board board, final Disc currentPlayer, final int colStart, final int rowStart, final int gapCol,
				final int colMod, final int rowMod) {
			if (rowStart == 0) {
				return false;
			}
			for (int c = colStart, r = rowStart - 1; c < colStart + 4; c = c + colMod, r = r + rowMod) {
				if (r < 0) {
					return false;
				}
				if (c != gapCol) {
					if (!board.getDisc(c, r).equals(currentPlayer)) {
						return false;
					}
				}
			}
			return true;
		}
	};

	/**
	 * Blocks the opponent from setting up a 3-in-a-row.
	 */
	private static final ColumnAnalyser BLOCK_MAKE_3_SETUP = new ColumnAnalyser() {

		@Override
		public boolean flag(final Board board, final Disc currentPlayer, final int column, final ColumnAnalysis currentAnalysis) {
			final Disc opponentDisc = Disc.getOpposite(currentPlayer);
			final ColumnAnalysis opponentAnalysis = new ColumnAnalysis(currentAnalysis);
			if (MAKE_3_SETUP.flag(board, opponentDisc, column, opponentAnalysis)) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_BLOCK_MAKE_3_SETUP);
				if (opponentAnalysis.hasCondition(ColumnAnalysis.FLAG_MAKE_3_DOUBLE_SETUP)) {
					currentAnalysis.addCondition(ColumnAnalysis.FLAG_BLOCK_MAKE_3_DOUBLE_SETUP);
				}
			}
			return false;
		}
	};

	/**
	 * Check if the bottom center column if free
	 */
	private static final ColumnAnalyser BOTTOM_CENTER = new ColumnAnalyser() {

		@Override
		public boolean flag(final Board board, final Disc currentPlayer, final int column, final ColumnAnalysis currentAnalysis) {
			final int centerColumn = board.getNumCols() / 2;
			if (column == centerColumn && board.getDisc(centerColumn, 0) == null) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_BOTTOM_CENTER_FREE);
				return true;
			}
			return false;
		}

	};

	private static final List<ColumnAnalyser> ANALYSERS = new LinkedList<>();
	static {
		ANALYSERS.add(WIN_NOW);
		ANALYSERS.add(TRAP_MORE_THAN_ONE);
		ANALYSERS.add(BLOCK_LOSS_1);
		ANALYSERS.add(BLOCK_TRAP_MORE_THAN_ONE);
		ANALYSERS.add(ENABLE_OPPONENT_WIN);
		ANALYSERS.add(MAKE_3_SETUP);
		ANALYSERS.add(BLOCK_MAKE_3_SETUP);
		ANALYSERS.add(BOTTOM_CENTER);
	}

	public static List<ColumnAnalyser> getAnalysers() {
		return ANALYSERS;
	}
}
