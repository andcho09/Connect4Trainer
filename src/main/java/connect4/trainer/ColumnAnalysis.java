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

	static interface ColumnAnalyser {

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
				final Disc opponentDisc = currentPlayer == Disc.RED ? Disc.YELLOW : Disc.RED;

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
	}
}
