package connect4.trainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.IllegalMoveException;
import connect4.api.analysis.BoardAnalysis;
import connect4.api.analysis.ColumnAnalysis;

/**
 * A factory for {@link BoardAnalyser}s which look at the playing the entire board. These analysers differ from the ColumnAnalyser in that:
 * <ul>
 * <li>BoardAnalysers run after ColumnAnalyers which allows them to leverage their analysis</b>
 * <li>BoardAnalysers are intended to leverage communicating ForcedAnalysisResult scenarios</b>
 * </ul>
 */
public class BoardAnalyserFactory {

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
			this.moves = new LinkedList<>();
			this.opponentMoves = new LinkedList<>();
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

		boolean isLoss() {
			return isLoss;
		}

		/**
		 * @param isLoss <code>true</code> if the forced analysis ends in us losing
		 */
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
			return moves.getLast();
		}

		@Override
		public String toString() {
			return "Forced Moves:" + StringUtils.join(moves.descendingIterator(), ",");
		}

		/**
		 * Replay the following set of moves where the current player wins.
		 * @param board the board to start at
		 * @return a {@link String} representing the play
		 * @throws IllegalMoveException if the analysis is invalid and can't be played
		 */
		public String replay(final Board board, final Disc currentPlayer) throws IllegalMoveException {
			// TODO currently unused
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
				sb.append(currentPlayer.toString() + " plays column " + (column + 1) + " creating board:\n" + playBoard);
				// Opponent
				column = opponentMoves.get(opponentMoves.size() - 1 - i);
				playBoard.putDisc(column, opponent);
				sb.append(opponent.toString() + " plays column " + (column + 1) + " creating board:\n" + playBoard);
			}
			// TODO show losses
			// TODO scoring algorithm
			final BoardAnalysis winColumns = boardAnalysis.getColumnsWithConditions(ColumnAnalysis.FLAG_WIN_1,
					ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE);
			sb.append(currentPlayer.toString() + " wins with " + StringUtils.join(winColumns.iterator(), ", "));
			return sb.toString();
		}
	}

	private static final AbstractForceBoardAnalyser FORCED_ANALYSER = new AbstractForceBoardAnalyser() {

		@Override
		public List<ForcedAnalysisResult> analyse(final BoardAnalysis boardAnalysis, final Board board, final Disc currentPlayer) {
			// Check 'forced'
			final List<ForcedAnalysisResult> forcedAnalysisWinResults = doForcedAnalysis(board, currentPlayer, boardAnalysis, 0);
			if (!forcedAnalysisWinResults.isEmpty()) {
				// Found some wins, now find the shortest depth
				int shortestDepth = Integer.MAX_VALUE;
				final List<ForcedAnalysisResult> shortestForcedAnalysisWinResults = new ArrayList<>();
				for (final ForcedAnalysisResult result : forcedAnalysisWinResults) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Forced win sequence: " + result);
					}
					if (result.getDepth() < shortestDepth) {
						shortestDepth = result.getDepth();
						shortestForcedAnalysisWinResults.clear();
						shortestForcedAnalysisWinResults.add(result);
					} else if (result.getDepth() == shortestDepth) {
						shortestForcedAnalysisWinResults.add(result);
					}
				}
				for (final ForcedAnalysisResult result : shortestForcedAnalysisWinResults) {
					boardAnalysis.apply(result.getEarliestMove(), ColumnAnalysis.FLAG_FORCED_WIN);
				}
				return shortestForcedAnalysisWinResults;
			} else {
				return Collections.emptyList();
			}
		}

	};

	private static final AbstractForceBoardAnalyser BLOCK_FORCED_ANALYSER = new AbstractForceBoardAnalyser() {
		@Override
		public List<ForcedAnalysisResult> analyse(final BoardAnalysis boardAnalysis, final Board board, final Disc currentPlayer) {
			final Disc opponent = Disc.getOpposite(currentPlayer);
			final BoardAnalysis opponentAnalysis = BoardAnalyserHelper.analyse(board, opponent);

			// Check 'forced'
			final List<ForcedAnalysisResult> forcedAnalysisOpponentWinResults = doForcedAnalysis(board, opponent, opponentAnalysis, 0);
			if (!forcedAnalysisOpponentWinResults.isEmpty()) {
				// Found some wins for the opponent, we can't let them play these columns
				for (final ForcedAnalysisResult result : forcedAnalysisOpponentWinResults) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Opponent's forced win sequence: " + result);
					}
					boardAnalysis.apply(result.getEarliestMove(), ColumnAnalysis.FLAG_BLOCK_FORCED_WIN);
					result.setIsLoss(true);
				}
				return forcedAnalysisOpponentWinResults;
			} else {
				return Collections.emptyList();
			}
		};
	};

	private static final List<AbstractForceBoardAnalyser> ANALYSERS = new LinkedList<>();
	static {
		ANALYSERS.add(FORCED_ANALYSER);
		ANALYSERS.add(BLOCK_FORCED_ANALYSER);
	}

	public static List<AbstractForceBoardAnalyser> getForcedAnalysers() {
		return ANALYSERS;
	}
}
