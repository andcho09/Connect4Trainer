package connect4.trainer;

import java.util.ArrayList;
import java.util.List;

import connect4.Board;
import connect4.BoardHelper;
import connect4.Disc;
import connect4.IllegalMoveException;
import connect4.Move;

/**
 * Flags a column with a bunch of facts/analysis. Doesn't do scoring. Just facts.
 */
public class ColumnAnalysis {

	/**
	 * No idea.
	 */
	public static final int FLAG_NO_OPINION = 0;
	/**
	 * Current player will win in next move
	 */
	public static final int FLAG_WIN_1 = 1 << 0;
	/**
	 * The column can't be played, i.e. it's full
	 */
	public static final int FLAG_UNPLAYABLE = 1 << 1;

	/**
	 * Playing in this column will block the opponent from winning in their next move
	 */
	public static final int FLAG_BLOCK_LOSS_1 = 1 << 2;

	/**
	 * Playing in this column allows the opponent to win by playing ontop
	 */
	public static final int FLAG_ENABLE_OPPONENT_WIN = 1 << 3;

	/**
	 * Playing in this column opens up two columns to win on the next move
	 */
	public static final int FLAG_TRAP_MORE_THAN_ONE = 1 << 4;

	/**
	 * Playing in this column opens up two columns to win on the next move
	 */
	public static final int FLAG_BLOCK_TRAP_MORE_THAN_ONE = 1 << 5;

	private final int column;
	private int flags;

	public ColumnAnalysis(final int column) {
		this.column = column;
	}

	/**
	 * @return the flags that have been set
	 */
	public int getFlags() {
		return flags;
	}

	public boolean hasCondition(final int flag) {
		return (flags & flag) == flag;
	}

	public void addCondition(final int flag) {
		flags = flags | flag;
	}

	public int getColumn() {
		return column;
	}

	/**
	 * An analyser which looks at the playing in an individual column.
	 */
	static interface ColumnAnalyser {

		/**
		 * Flags the given column position with various facts (flags). For example, will playing in
		 * the column win the game? Or will playing in the column let opponent play ontop of my disc
		 * and win? The flags have no meaning, they're scored by the {@link ScoringAlgorithm} later.
		 * @param board the {@link Board} to analyse. This is the real board so if the
		 *        {@link ColumnAnalyser} needs to modify it, it should do so using
		 *        {@link Board#Board(Board)} (i.e. the clone constructor)
		 * @param currentPlayer the {@link Disc} of the current player
		 * @param column the column (0-based) we're analysing
		 * @return what conditions have been found by playing at this position
		 */
		public int flag(final Board board, final Disc currentPlayer, final int column);
	}

	static List<ColumnAnalyser> ANALYSERS = new ArrayList<ColumnAnalyser>();
	static {
		ANALYSERS.add(new ColumnAnalyser() {
			// Unplayable or win now
			@Override
			public int flag(final Board board, final Disc currentPlayer, final int column) {
				int row = -1;
				final Board newBoard = new Board(board);
				try {
					row = newBoard.putDisc(column, currentPlayer);
				} catch (final IllegalMoveException e) {
					return FLAG_UNPLAYABLE;
				}

				final Disc winner = BoardHelper.hasWinner(newBoard,
						new Move(currentPlayer, column, row));
				if (winner == currentPlayer) {
					return ColumnAnalysis.FLAG_WIN_1;
				}
				return FLAG_NO_OPINION;
			}
		});

		ANALYSERS.add(new ColumnAnalyser() {
			// Playing here blocks opponent from winning in their next move
			@Override
			public int flag(final Board board, final Disc currentPlayer, final int column) {
				final Disc opponentDisc = Disc.getOpposite(currentPlayer);

				final Board newBoard = new Board(board);
				int row;
				try {
					row = newBoard.putDisc(column, opponentDisc);
				} catch (final IllegalMoveException e) {
					return FLAG_UNPLAYABLE;
				}

				final Disc winner = BoardHelper.hasWinner(newBoard,
						new Move(opponentDisc, column, row));
				if (winner == opponentDisc) {
					return ColumnAnalysis.FLAG_BLOCK_LOSS_1;
				}
				return FLAG_NO_OPINION;
			}
		});

		ANALYSERS.add(new ColumnAnalyser() {
			// Playing here allows the opponent to win by playing above us
			@Override
			public int flag(final Board board, final Disc currentPlayer, final int column) {
				final Board newBoard = new Board(board);
				try {
					newBoard.putDisc(column, currentPlayer);
				} catch (final IllegalMoveException e) {
					return FLAG_UNPLAYABLE;
				}

				final Disc oponentPlayer = Disc.getOpposite(currentPlayer);
				int row;
				try {
					row = newBoard.putDisc(column, oponentPlayer);
				} catch (final IllegalMoveException e) {
					return FLAG_NO_OPINION;
				}

				if (oponentPlayer.equals(
						BoardHelper.hasWinner(newBoard, new Move(oponentPlayer, column, row)))) {
					return FLAG_ENABLE_OPPONENT_WIN;
				}
				return FLAG_NO_OPINION;
			}
		});

		ANALYSERS.add(new ColumnAnalyser() {
			// Playing here gives us more than one different column to win (i.e. execute a trap)
			@Override
			public int flag(final Board board, final Disc currentPlayer, final int column) {
				final Board newBoard = new Board(board);
				try {
					newBoard.putDisc(column, currentPlayer);
				} catch (final IllegalMoveException e) {
					return FLAG_UNPLAYABLE;
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
					if (currentPlayer.equals(BoardHelper.hasWinner(newBoard2Ahead,
							new Move(currentPlayer, i, row)))) {
						winCounter++;
					}
				}

				if (winCounter > 1) {
					return FLAG_TRAP_MORE_THAN_ONE;
				}
				return FLAG_NO_OPINION;
			}
		});

		ANALYSERS.add(new ColumnAnalyser() {
			// Playing here blocks the opponent gaining more than one different column to win (i.e.
			// execute a trap)
			@Override
			public int flag(final Board board, final Disc currentPlayer, final int column) {
				final Disc opponentDisc = Disc.getOpposite(currentPlayer);

				final Board newBoard = new Board(board);
				try {
					newBoard.putDisc(column, opponentDisc);
				} catch (final IllegalMoveException e) {
					return FLAG_UNPLAYABLE;
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
					if (opponentDisc.equals(BoardHelper.hasWinner(newBoard2Ahead,
							new Move(opponentDisc, i, row)))) {
						winCounter++;
					}
				}

				if (winCounter > 1) {
					return FLAG_BLOCK_TRAP_MORE_THAN_ONE;
				}
				return FLAG_NO_OPINION;
			}
		});
	}
}
