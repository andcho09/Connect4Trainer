package connect4.trainer;

import java.util.LinkedList;
import java.util.List;

import connect4.Board;
import connect4.BoardHelper;
import connect4.Disc;
import connect4.IllegalMoveException;
import connect4.Move;

/**
 * An analyser which looks at the playing in an individual column.
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
		 * @param currentAnaylsi the analysis conducted so far. Implementors are expected to modify
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
				final Board newBoard2Ahead = new Board(newBoard); // A board two moves ahead
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
				final Board newBoard2Ahead = new Board(newBoard); // A board two moves ahead
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

	private static final List<ColumnAnalyser> ANALYSERS = new LinkedList<ColumnAnalyser>();
	static {
		ANALYSERS.add(WIN_NOW);
		ANALYSERS.add(BLOCK_LOSS_1);
		ANALYSERS.add(ENABLE_OPPONENT_WIN);
		ANALYSERS.add(TRAP_MORE_THAN_ONE);
		ANALYSERS.add(BLOCK_TRAP_MORE_THAN_ONE);
	}

	public static List<ColumnAnalyser> getAnalysers() {
		return ANALYSERS;
	}
}
