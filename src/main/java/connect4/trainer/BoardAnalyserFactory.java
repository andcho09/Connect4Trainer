package connect4.trainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import connect4.Board;
import connect4.Disc;
import connect4.IllegalMoveException;

/**
 * A factory for {@link BoardAnalyser}s which look at the playing the entire board. These analysers differ from the ColumnAnalyser in that:
 * <ul>
 * <li>BoardAnalysers run after ColumnAnalyers which allows them to leverage their analysis</b>
 * <li>BoardAnalysers are intended to leverage communicating ForcedAnalysisResult scenarios</b>
 * </ul>
 */
public class BoardAnalyserFactory {

	public static interface BoardAnalyser {

		public List<ForcedAnalysisResult> analyse(final BoardAnalysis boardAnalysis,
				final Board board, final Disc currentPlayer);
	}

	/**
	 * Records win scenarios during forced analysis.
	 */
	public static class ForcedAnalysisResult {
		private final int depth;
		private final BoardAnalysis boardAnalysis;
		private boolean isLoss;
		private final LinkedList<Integer> moves;
		private final LinkedList<Integer> opponentMoves;

		/**
		 * Records a win scenario.
		 * @param depth the depth of the win (shorter is more desirable)
		 * @param boardAnalysis the {@link BoardAnalysis} associated with this win at the win state
		 */
		public ForcedAnalysisResult(final int depth, final BoardAnalysis boardAnalysis) {
			this.depth = depth;
			this.boardAnalysis = boardAnalysis;
			this.moves = new LinkedList<Integer>();
			this.opponentMoves = new LinkedList<Integer>();
		}

		/**
		 * @return how many moves deep the win is
		 */
		int getDepth() {
			return depth;
		}

		BoardAnalysis getBoardAnalysis() {
			return boardAnalysis;
		}

		void setIsLoss(final boolean isLoss) {
			this.isLoss = isLoss;
		}

		/**
		 * Add a move onto the sequence of how we win.
		 * @param column the column of the move
		 */
		void pushMove(final int column) {
			moves.add(column);
		}

		/**
		 * Add an opponent's move to the sequence of how we win.
		 * @param column the column of the move
		 */
		void pushOpponentMove(final int column) {
			opponentMoves.add(column);
		}

		LinkedList<Integer> getMoves() {
			return moves;
		}

		LinkedList<Integer> getOpponentMoves() {
			return opponentMoves;
		}

		/**
		 * @return the earliest move in the win sequence. Could be <code>null</code> if we already
		 *         won without forcing the opponent
		 */
		Integer getEarliestMove() {
			if (moves.isEmpty()) {
				return null;
			}
			moves.getLast();
			return moves.get(moves.size() - 1);
		}

		@Override
		public String toString() {
			return "Sequence of moves: " + StringUtils.join(moves.descendingIterator(), ",");
		}

		/**
		 * Replay the following set of moves where the current player wins.
		 * @param board the board to start at
		 * @return a {@link String} representing the play
		 * @throws IllegalMoveException if the analysis is invalid and can't be played
		 */
		public String replay(final Board board, final Disc currentPlayer)
				throws IllegalMoveException {
			if (moves.isEmpty()) {
				return null; // Opponent not forced
			}

			final StringBuilder sb = new StringBuilder();
			final Board playBoard = new Board(board);
			final Disc opponent = Disc.getOpposite(currentPlayer);
			sb.append("As player " + currentPlayer.toString() + " with board:\n" + board);
			final int numOfMoves = moves.size();
			for (int i = 0; i < numOfMoves; i++) {
				// Current
				Integer column = moves.get(moves.size() - 1 - i);
				playBoard.putDisc(column, currentPlayer);
				sb.append(currentPlayer.toString() + " plays column " + (column + 1)
						+ " creating board:\n" + playBoard);
				// Opponent
				column = opponentMoves.get(opponentMoves.size() - 1 - i);
				playBoard.putDisc(column, opponent);
				sb.append(opponent.toString() + " plays column " + (column + 1)
						+ " creating board:\n" + playBoard);
			}
			// TODO show losses
			final BoardAnalysis winColumns = boardAnalysis.getColumnsWithConditions(
					ColumnAnalysis.FLAG_WIN_1, ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE);
			sb.append(currentPlayer.toString() + " wins with "
					+ StringUtils.join(winColumns.iterator(), ", "));
			return sb.toString();
		}
	}

	private static final BoardAnalyser FORCED_ANALYSER = new AbstractForceBoardAnalyser() {

		@Override
		public List<ForcedAnalysisResult> analyse(final BoardAnalysis boardAnalysis,
				final Board board, final Disc currentPlayer) {
			// Check 'forced'
			final List<ForcedAnalysisResult> forcedAnalysisWinResults = doForcedAnalysis(board,
					currentPlayer, boardAnalysis, 0);
			if (!forcedAnalysisWinResults.isEmpty()) {
				// Found some wins, now find the shortest depth
				int shortestDepth = Integer.MAX_VALUE;
				final List<ForcedAnalysisResult> shortestForcedAnalysisWinResults = new ArrayList<ForcedAnalysisResult>();
				for (final ForcedAnalysisResult forcedAnalysisWinResult : forcedAnalysisWinResults) {
					if (forcedAnalysisWinResult.getDepth() < shortestDepth) {
						shortestDepth = forcedAnalysisWinResult.getDepth();
						shortestForcedAnalysisWinResults.clear();
						shortestForcedAnalysisWinResults.add(forcedAnalysisWinResult);
					} else if (forcedAnalysisWinResult.getDepth() == shortestDepth) {
						shortestForcedAnalysisWinResults.add(forcedAnalysisWinResult);
					}
				}
				for (final ForcedAnalysisResult forcedAnalysisWinResult : shortestForcedAnalysisWinResults) {
					boardAnalysis.apply(forcedAnalysisWinResult.getEarliestMove(),
							ColumnAnalysis.FLAG_FORCED_WIN);
				}
				return shortestForcedAnalysisWinResults;
			} else {
				return Collections.emptyList();
			}
		}

	};

	private static final BoardAnalyser BLOCK_FORCED_ANALYSER = new AbstractForceBoardAnalyser() {
		@Override
		public List<ForcedAnalysisResult> analyse(final BoardAnalysis boardAnalysis,
				final Board board, final Disc currentPlayer) {
			final Disc opponent = Disc.getOpposite(currentPlayer);
			final BoardAnalysis opponentAnalysis = BoardAnalyserHelper.analyse(board, opponent);

			// Check 'forced'
			final List<ForcedAnalysisResult> forcedAnalysisOpponentWinResults = doForcedAnalysis(
					board, opponent, opponentAnalysis, 0);
			if (!forcedAnalysisOpponentWinResults.isEmpty()) {
				// Found some wins for the opponent, we can't let them play these columns
				for (final ForcedAnalysisResult forcedAnalysisOpponentWinResult : forcedAnalysisOpponentWinResults) {
					boardAnalysis.apply(forcedAnalysisOpponentWinResult.getEarliestMove(),
							ColumnAnalysis.FLAG_BLOCK_FORCED_WIN);
					forcedAnalysisOpponentWinResult.setIsLoss(true);
				}
				return forcedAnalysisOpponentWinResults;
			} else {
				return Collections.emptyList();
			}
		};
	};

	private static final List<BoardAnalyser> ANALYSERS = new LinkedList<BoardAnalyser>();
	static {
		ANALYSERS.add(FORCED_ANALYSER);
		ANALYSERS.add(BLOCK_FORCED_ANALYSER);
	}

	public static List<BoardAnalyser> getAnalysers() {
		return ANALYSERS;
	}
}
