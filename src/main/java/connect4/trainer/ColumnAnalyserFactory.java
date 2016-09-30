package connect4.trainer;

import java.util.LinkedList;
import java.util.List;

import connect4.Board;
import connect4.BoardHelper;
import connect4.Disc;
import connect4.IllegalMoveException;
import connect4.Move;

/**
 * A factory for {@link ColumnAnalyser}s which look at the playing in an individual column.
 */
public class ColumnAnalyserFactory {

	public static abstract class ColumnAnalyser {

		/**
		 * Flags the given column position with various facts (flags). For example, will playing in
		 * the column win the game? Or will playing in the column let opponent play ontop of my disc
		 * and win? The flags have no meaning, they're scored by the {@link ScoringAlgorithm} later.
		 * @param board the {@link Board} to analyse. This is the real board so if the
		 *        {@link ColumnAnalyser} needs to modify it, it should do so using
		 *        {@link Board#Board(Board)} (i.e. the clone constructor)
		 * @param currentPlayer the {@link Disc} of the current player
		 * @param column the column (0-based) we're analysing
		 * @param currentAnalysis the analysis conducted so far. Implementors are expected to modify
		 *        this instance
		 * @return the new analysis
		 */
		public abstract ColumnAnalysis flag(final Board board, final Disc currentPlayer,
				final int column, final ColumnAnalysis currentAnalysis);
	}

	/** Unplayable or win now */
	private static final ColumnAnalyser WIN_NOW = new ColumnAnalyser() {
		@Override
		public ColumnAnalysis flag(final Board board, final Disc currentPlayer, final int column,
				final ColumnAnalysis currentAnalysis) {
			int row = -1;
			final Board newBoard = new Board(board);
			try {
				row = newBoard.putDisc(column, currentPlayer);
			} catch (final IllegalMoveException e) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_UNPLAYABLE);
				return currentAnalysis;
			}

			final Disc winner = BoardHelper.hasWinner(newBoard,
					new Move(currentPlayer, column, row));
			if (winner == currentPlayer) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_WIN_1);
				return currentAnalysis;
			}
			return currentAnalysis;
		}
	};

	/** Playing here blocks opponent from winning in their next move */
	private static final ColumnAnalyser BLOCK_LOSS_1 = new ColumnAnalyser() {
		@Override
		public ColumnAnalysis flag(final Board board, final Disc currentPlayer, final int column,
				final ColumnAnalysis currentAnalysis) {
			final Disc opponentDisc = Disc.getOpposite(currentPlayer);

			final Board newBoard = new Board(board);
			int row;
			try {
				row = newBoard.putDisc(column, opponentDisc);
			} catch (final IllegalMoveException e) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_UNPLAYABLE);
				return currentAnalysis;
			}

			final Disc winner = BoardHelper.hasWinner(newBoard,
					new Move(opponentDisc, column, row));
			if (winner == opponentDisc) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_BLOCK_LOSS_1);
				return currentAnalysis;
			}
			return currentAnalysis;
		}
	};

	/** Playing here allows the opponent to win by playing above us */
	private static final ColumnAnalyser ENABLE_OPPONENT_WIN = new ColumnAnalyser() {
		@Override
		public ColumnAnalysis flag(final Board board, final Disc currentPlayer, final int column,
				final ColumnAnalysis currentAnalysis) {
			final Board newBoard = new Board(board);
			try {
				newBoard.putDisc(column, currentPlayer);
			} catch (final IllegalMoveException e) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_UNPLAYABLE);
				return currentAnalysis;
			}

			final Disc oponentPlayer = Disc.getOpposite(currentPlayer);
			int row;
			try {
				row = newBoard.putDisc(column, oponentPlayer);
			} catch (final IllegalMoveException e) {
				return currentAnalysis;
			}

			if (oponentPlayer.equals(
					BoardHelper.hasWinner(newBoard, new Move(oponentPlayer, column, row)))) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_ENABLE_OPPONENT_WIN);
				return currentAnalysis;
			}
			return currentAnalysis;
		}
	};

	/** Playing here gives us more than one different column to win (i.e. execute a trap) */
	private static final ColumnAnalyser TRAP_MORE_THAN_ONE = new ColumnAnalyser() {
		@Override
		public ColumnAnalysis flag(final Board board, final Disc currentPlayer, final int column,
				final ColumnAnalysis currentAnalysis) {
			final Board newBoard = new Board(board);
			try {
				newBoard.putDisc(column, currentPlayer);
			} catch (final IllegalMoveException e) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_UNPLAYABLE);
				return currentAnalysis;
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
				if (currentPlayer.equals(
						BoardHelper.hasWinner(newBoard2Ahead, new Move(currentPlayer, i, row)))) {
					winCounter++;
				}
			}

			if (winCounter > 1) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE);
				return currentAnalysis;
			}
			return currentAnalysis;
		}
	};

	/**
	 * Playing here blocks the opponent gaining more than one different column to win (i.e. execute
	 * a trap)
	 */
	private static final ColumnAnalyser BLOCK_TRAP_MORE_THAN_ONE = new ColumnAnalyser() {
		@Override
		public ColumnAnalysis flag(final Board board, final Disc currentPlayer, final int column,
				final ColumnAnalysis currentAnalysis) {
			final Disc opponentDisc = Disc.getOpposite(currentPlayer);

			final Board newBoard = new Board(board);
			try {
				newBoard.putDisc(column, opponentDisc);
			} catch (final IllegalMoveException e) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_UNPLAYABLE);
				return currentAnalysis;
			}

			int winCounter = 0;
			final int startColumn = BoardHelper.getMinColumnSpan(board, column);
			final int endColumn = BoardHelper.getMaxColumnSpan(board, column);
			for (int i = startColumn; i <= endColumn; i++) {
				// A board two moves ahead
				final Board newBoard2Ahead = new Board(newBoard);
				int row;
				try {
					row = newBoard2Ahead.putDisc(i, opponentDisc);
				} catch (final IllegalMoveException e) {
					continue;
				}
				if (opponentDisc.equals(
						BoardHelper.hasWinner(newBoard2Ahead, new Move(opponentDisc, i, row)))) {
					winCounter++;
				}
			}

			if (winCounter > 1) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE);
				return currentAnalysis;
			}
			return currentAnalysis;
		}
	};

	/**
	 * Playing here creates a four in a row setup where the opponent can't block as there's a gap
	 * below the spot that completes the 4-in-a-row. I.e. someone has to play below that spot first.
	 * This could set up a win later or at least shut down the column.
	 */
	private static final ColumnAnalyser MAKE_3_SETUP = new ColumnAnalyser() {
		@Override
		public ColumnAnalysis flag(final Board board, final Disc currentPlayer, final int column,
				final ColumnAnalysis currentAnalysis) {
			int row;
			final Board newBoard = new Board(board);
			try {
				row = newBoard.putDisc(column, currentPlayer);
			} catch (final IllegalMoveException e) {
				currentAnalysis.addCondition(ColumnAnalysis.FLAG_UNPLAYABLE);
				return currentAnalysis;
			}

			{// Horizontal check
				if (row > 0) {// if we're on the bottom row, there can't be gap below us
					final int minCol = BoardHelper.getMinColumnSpan(newBoard, column);
					final int maxCol = BoardHelper.getMaxColumnSpan(newBoard, column);
					if (maxCol - minCol >= 3) { // has to be at least 4 columns
						spread: for (int c = minCol; c <= maxCol - 3; c++) {
							int gapCol = -1;
							for (int i = 0; i < 4; i++) {
								final Disc disc = newBoard.getDisc(c + i, row);
								if (disc == null) {
									if (gapCol != -1) {
										continue spread; // Two gaps, can't make 4
									} else {
										gapCol = c + i;

									}
								} else if (!disc.equals(currentPlayer)) {
									continue spread; // Opponent disc, can't make 4
								}
							}
							currentAnalysis.addCondition(ColumnAnalysis.FLAG_MAKE_3_SETUP);
							return currentAnalysis;
						}
					}
				}
			}

			{// Diagonal SW-NE check
				final List<int[]> spans = BoardHelper.getDiagonalSwNeSpans(newBoard, column, row);
				final int[] sw = spans.get(0);
				final int[] ne = spans.get(1);
				final int minCol = sw[0];
				final int maxCol = ne[0];
				if (maxCol - minCol >= 3) { // has to be at least 4 columns
					spread: for (int c = minCol, r = sw[1]; c <= maxCol - 3; c++, r++) {
						int gapCol = -1;
						int gapRow = -1;
						for (int i = 0; i < 4; i++) {
							final Disc disc = newBoard.getDisc(c + i, r + i);
							if (disc == null) {
								if (gapCol != -1) {
									continue spread; // Two gaps, can't make 4
								} else {
									gapCol = c + i;
									gapRow = r + i;
								}
							} else if (!disc.equals(currentPlayer)) {
								continue spread; // Opponent disc, can't make 4
							}
						}
						if (gapRow > 0 && newBoard.getDisc(gapCol, gapRow - 1) == null) {
							currentAnalysis.addCondition(ColumnAnalysis.FLAG_MAKE_3_SETUP);
							return currentAnalysis;
						}
					}
				}
			}

			{// Diagonal SW-NE check
				final List<int[]> spans = BoardHelper.getDiagonalSeNwSpans(newBoard, column, row);
				final int[] se = spans.get(0);
				final int[] nw = spans.get(1);
				final int minCol = nw[0];
				final int maxCol = se[0];
				if (maxCol - minCol >= 3) { // has to be at least 4 columns
					spread: for (int c = minCol, r = nw[1]; c <= maxCol - 3; c++, r--) {
						int gapCol = -1;
						int gapRow = -1;
						for (int i = 0; i < 4; i++) {
							final Disc disc = newBoard.getDisc(c + i, r - i);
							if (disc == null) {
								if (gapCol != -1) {
									continue spread; // Two gaps, can't make 4
								} else {
									gapCol = c + i;
									gapRow = r - i;
								}
							} else if (!disc.equals(currentPlayer)) {
								continue spread; // Opponent disc, can't make 4
							}
						}
						if (gapRow > 0 && newBoard.getDisc(gapCol, gapRow - 1) == null) {
							currentAnalysis.addCondition(ColumnAnalysis.FLAG_MAKE_3_SETUP);
							return currentAnalysis;
						}
					}
				}
			}

			return currentAnalysis;
		}
	};

	private static final List<ColumnAnalyser> ANALYSERS = new LinkedList<ColumnAnalyser>();
	static {
		ANALYSERS.add(WIN_NOW);
		ANALYSERS.add(BLOCK_LOSS_1);
		ANALYSERS.add(ENABLE_OPPONENT_WIN);
		ANALYSERS.add(TRAP_MORE_THAN_ONE);
		ANALYSERS.add(BLOCK_TRAP_MORE_THAN_ONE);
		ANALYSERS.add(MAKE_3_SETUP);
	}

	public static List<ColumnAnalyser> getAnalysers() {
		return ANALYSERS;
	}
}
