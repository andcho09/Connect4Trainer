package connect4.trainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import connect4.Board;
import connect4.Disc;
import connect4.IllegalMoveException;

/**
 * Trainer capable of predicting forced moves.
 */
public class ForcedTrainer extends Recommender {

	private static final Logger LOGGER = Logger.getLogger(ForcedTrainer.class);
	private static final BoardAnalyser BOARD_ANALYSER = new BoardAnalyser();

	/**
	 * Analyses the board and recommends where to play.
	 * @param board the {@link Board} to analyse.
	 * @param currentPlayer the {@link Disc} of the current player
	 * @return the column the trainer recommends to play (0-based)
	 */
	@Override
	public int recommend(final Board board, final Disc currentPlayer) {
		resetLast();

		// Analysis phase
		final BoardAnalysis boardAnalysis = BOARD_ANALYSER.analyse(board, currentPlayer);

		// Check 'forced'
		final List<ForcedAnalysisResult> forcedAnalysisWinResults = forcedAnalysis(board,
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
		}

		// Scoring phase
		final BoardAnalysis bestBoardAnalysis = new BoardAnalysis();
		int bestScore = Integer.MIN_VALUE;
		for (final ColumnAnalysis analysis : boardAnalysis) {
			final int score = scoringAlgorithm.score(analysis);
			if (score > bestScore) {
				bestScore = score;
				bestBoardAnalysis.clear();
				bestBoardAnalysis.add(analysis);
			} else if (score == bestScore) {
				bestBoardAnalysis.add(analysis);
			}
		}

		setLastAnalysis(bestBoardAnalysis, boardAnalysis);

		// Tie breaking phase
		if (bestBoardAnalysis.size() == 1) {
			return bestBoardAnalysis.get(0).getColumn();
		} else {
			final int randomInt = random.nextInt(bestBoardAnalysis.size());
			return bestBoardAnalysis.get(randomInt).getColumn();
		}
	}

	/**
	 * Perform 'forced' analysis, i.e. recursively analyse if the opponent is forced into a move.
	 * @param board the {@link Board} to analse
	 * @param currentPlayer the {@link Disc} of the current player
	 * @param boardAnalysis the freshly analysed board
	 * @param depth how far down the rabbit hole we've gone
	 * @return recommendations of where to play and why. If empty, no analysis (opponent not forced
	 *         before end condition). Could be wining columns (if we can win), or forced columns
	 *         (we're forced to play there).
	 */
	private List<ForcedAnalysisResult> forcedAnalysis(final Board board, final Disc currentPlayer,
			final BoardAnalysis boardAnalysis, final int depth) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Beginning forced analysis for player " + currentPlayer.toString()
					+ " for board:\n" + board.toString() + " with analysis: "
					+ StringUtils.join(boardAnalysis.iterator(), ", "));
		}

		final List<ForcedAnalysisResult> resultInWins = new ArrayList<ForcedAnalysisResult>();

		// Check exit conditions
		final BoardAnalysis winColumns = boardAnalysis.getColumnsWithConditions(
				ColumnAnalysis.FLAG_WIN_1, ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE);
		final BoardAnalysis forcedColumns = boardAnalysis.getColumnsWithConditions(
				ColumnAnalysis.FLAG_BLOCK_LOSS_1, ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE);
		if (winColumns.size() > 0) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Detected win scenario, returning");
			}
			resultInWins.add(new ForcedAnalysisResult(depth));
			return resultInWins;
		} else if (forcedColumns.size() > 0) {
			// TODO we shouldn't return here, just cut down on where we play, play that, and
			// continue analyising (it's still forced analysis)
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Detected we're forced scenario, returning");
			}
			return Collections.emptyList();
		}

		// Begin 'forced' analysis

		for (final ColumnAnalysis analysis : boardAnalysis) {
			if (analysis.hasCondition(ColumnAnalysis.FLAG_UNPLAYABLE)) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Can't play " + analysis.getColumn() + ", skipping");
				}
				continue;
			}
			final Board newBoard = new Board(board);
			try {
				newBoard.putDisc(analysis.getColumn(), currentPlayer);
			} catch (final IllegalMoveException swallow) {
				continue;
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("What happens when " + currentPlayer.toString() + " plays column "
						+ analysis.getColumn() + " to create board?:\n" + newBoard.toString());
			}

			final Disc opponentPlayer = Disc.getOpposite(currentPlayer);
			final BoardAnalysis opponentAnalyses = BOARD_ANALYSER.analyse(newBoard, opponentPlayer);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Opponent " + opponentPlayer.toString() + "'s analysis is:\n "
						+ StringUtils.join(opponentAnalyses.iterator(), ", "));
			}

			BoardAnalysis opponentForcedColumns = opponentAnalyses
					.getColumnsWithConditions(ColumnAnalysis.FLAG_BLOCK_LOSS_1);
			if (opponentForcedColumns.size() > 1) {
				// TODO if there's two 'must block now' situations then we should have won and
				// detected this before
				throwMoreThanForcedMoveError(
						"I think we missed something. The opponent is forced into blocking more than one column immediately.",
						board, currentPlayer, boardAnalysis.iterator(), analysis.getColumn(),
						newBoard, opponentForcedColumns.iterator());
			} else if (opponentForcedColumns.size() == 0) {
				opponentForcedColumns = boardAnalysis
						.getColumnsWithConditions(ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE);
				if (opponentForcedColumns.size() > 1) {
					// TODO if they're blocking two traps we won. We should've detected this
					// earlier.
					throwMoreThanForcedMoveError(
							"I think we missed something. The opponent is forced into blocking more than one trap.",
							board, currentPlayer, boardAnalysis.iterator(), analysis.getColumn(),
							newBoard, opponentForcedColumns.iterator());
				}
			}

			if (opponentForcedColumns.size() == 1) {
				final Board opponentBoard = new Board(newBoard);
				try {
					opponentBoard.putDisc(opponentForcedColumns.get(0).getColumn(), opponentPlayer);
				} catch (final IllegalMoveException e) {
					continue; // We're forced to play here but can't???
				}

				// TODO should we check that we didn't just lose right here? Should be eliminated by
				// the 'are we forced check before'

				final BoardAnalysis forcedAnalyses = BOARD_ANALYSER.analyse(opponentBoard,
						currentPlayer);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(
							"Opponent " + opponentPlayer.toString() + " is forced to play column "
									+ opponentForcedColumns.get(0).getColumn()
									+ " which creates board:\n" + opponentBoard.toString()
									+ "\nRecursively calling forced analysis again...");
				}
				final List<ForcedAnalysisResult> results = forcedAnalysis(opponentBoard,
						currentPlayer, forcedAnalyses, depth + 1);
				for (final ForcedAnalysisResult result : results) {
					result.pushMove(analysis.getColumn());
					resultInWins.add(result);
				}
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Opponent " + opponentPlayer.toString() + " wasn't forced");
				}
				continue; // Opponent isn't forced, can't do any more 'forced' analysis
			}
		}

		return resultInWins;
	}

	private void throwMoreThanForcedMoveError(final String initialMessage, final Board board,
			final Disc currentPlayer, final Iterator<ColumnAnalysis> currentAnalysis,
			final int currentColumn, final Board newBoard,
			final Iterator<ColumnAnalysis> opponentForcedColumns) {
		final StringBuilder message = new StringBuilder(initialMessage + "\n");
		message.append(" We're playing as " + currentPlayer.toString() + " with board:\n"
				+ board.toString());
		message.append(
				" Original analysis is:\n  " + StringUtils.join(currentAnalysis, ", ") + "\n");
		message.append(" Candidate column is " + currentColumn + " which creates board:\n"
				+ newBoard.toString());
		message.append(" Analysis is:\n  " + StringUtils.join(opponentForcedColumns, ", "));
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(message);
		}
		throw new RuntimeException(message.toString());
	}

	/**
	 * Records win scenarios during forced analysis.
	 */
	private static class ForcedAnalysisResult {
		private final int depth;
		private final LinkedList<Integer> moves;

		/**
		 * Records a win scenario.
		 * @param depth the depth of the win (shorter is more desirable)
		 */
		public ForcedAnalysisResult(final int depth) {
			this.depth = depth;
			this.moves = new LinkedList<Integer>();
		}

		/**
		 * @return how many moves deep the win is
		 */
		int getDepth() {
			return depth;
		}

		/**
		 * Add a move onto the sequence of how we win.
		 * @param column the column of the move
		 */
		void pushMove(final int column) {
			moves.add(column);
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
	}
}
