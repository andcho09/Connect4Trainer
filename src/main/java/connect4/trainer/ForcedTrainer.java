package connect4.trainer;

import java.util.ArrayList;
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
		final List<ColumnAnalysis> analysisList = BOARD_ANALYSER.analyse(board, currentPlayer);

		// Check 'forced'
		final ForcedAnalysisResult forcedAnalysisResult = forcedAnalysis(board, currentPlayer,
				analysisList);
		if (ResultType.WIN.equals(forcedAnalysisResult.getReslutType())) {
			apply(analysisList, forcedAnalysisResult.getAnalysisList(),
					ColumnAnalysis.FLAG_FORCED_WIN);
		}

		// Scoring phase
		final List<ColumnAnalysis> bestColumnAnalysis = new ArrayList<ColumnAnalysis>(
				board.getNumCols());
		int bestScore = Integer.MIN_VALUE;
		for (final ColumnAnalysis analysis : analysisList) {
			final int score = scoringAlgorithm.score(analysis);
			if (score > bestScore) {
				bestScore = score;
				bestColumnAnalysis.clear();
				bestColumnAnalysis.add(analysis);
			} else if (score == bestScore) {
				bestColumnAnalysis.add(analysis);
			}
		}

		setLastAnalysis(bestColumnAnalysis, analysisList);

		// Tie breaking phase
		if (bestColumnAnalysis.size() == 1) {
			return bestColumnAnalysis.get(0).getColumn();
		} else {
			final int randomInt = random.nextInt(bestColumnAnalysis.size());
			return bestColumnAnalysis.get(randomInt).getColumn();
		}
	}

	/**
	 * Perform 'forced' analysis, i.e. recursively analyse if the opponent is forced into a move.
	 * @param board the {@link Board} to analse
	 * @param currentPlayer the {@link Disc} of the current player
	 * @param currentAnalysis the freshly analysed columns
	 * @return recommendations of where to play and why. If empty, no analysis (opponent not forced
	 *         before end condition). Could be wining columns (if we can win), or forced columns
	 *         (we're forced to play there).
	 */
	private ForcedAnalysisResult forcedAnalysis(final Board board, final Disc currentPlayer,
			final List<ColumnAnalysis> currentAnalysis) {
		// Check exit conditions
		final List<ColumnAnalysis> winColumns = BOARD_ANALYSER.getWinColumns(currentAnalysis);
		final List<ColumnAnalysis> forcedColumns = BOARD_ANALYSER.getForcedColumns(currentAnalysis);
		if (winColumns.size() > 0) {
			return new ForcedAnalysisResult(ResultType.WIN, winColumns);
		} else if (forcedColumns.size() > 0) {
			// TODO we shouldn't return here, just cut down on where we play
			return new ForcedAnalysisResult(ResultType.FORCED, forcedColumns);
		}

		// Begin 'forced' analysis
		for (final ColumnAnalysis analysis : currentAnalysis) {
			if (analysis.hasCondition(ColumnAnalysis.FLAG_UNPLAYABLE)) {
				continue;
			}
			final Board newBoard = new Board(board);
			try {
				newBoard.putDisc(analysis.getColumn(), currentPlayer);
			} catch (final IllegalMoveException swallow) {
				continue;
				// TODO should this flag unplayable?
			}

			final Disc opponentPlayer = Disc.getOpposite(currentPlayer);
			final List<ColumnAnalysis> opponentAnalyses = BOARD_ANALYSER.analyse(newBoard,
					opponentPlayer);
			final List<ColumnAnalysis> opponentForcedColumns = BOARD_ANALYSER
					.getForcedColumns(opponentAnalyses);
			if (opponentForcedColumns.size() == 0) {
				continue; // Opponent isn't forced, can't do any more 'forced' analysis
			} else if (opponentForcedColumns.size() == 1) {
				final Board opponentBoard = new Board(newBoard);
				try {
					opponentBoard.putDisc(opponentForcedColumns.get(0).getColumn(), opponentPlayer);
				} catch (final IllegalMoveException e) {
					continue; // We're forced to play here but can't???
				}

				// TODO should we check that we didn't just lose right here? Should be eliminated by
				// the 'are we forced check before'

				final List<ColumnAnalysis> forcedAnalyses = BOARD_ANALYSER.analyse(opponentBoard,
						currentPlayer);
				return forcedAnalysis(opponentBoard, currentPlayer, forcedAnalyses);
			} else {
				// TODO if there's two then we should have won
				final StringBuilder message = new StringBuilder(
						"I think we missed something. The opponent is forced into playing more than one column.\n");
				message.append(" We're playing as " + currentPlayer.toString() + " with board:\n"
						+ board.toString());
				message.append(" Original board is:\n" + board.toString());
				message.append(" Original analysis is:\n  "
						+ StringUtils.join(currentAnalysis.iterator(), ", ") + "\n");
				message.append(" Candiate column is " + analysis.getColumn()
						+ " which creates board:\n" + newBoard.toString());
				message.append(" Analysis is:\n  "
						+ StringUtils.join(opponentForcedColumns.iterator(), ", "));
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(message);
				}
				throw new RuntimeException(message.toString());
			}
		}

		return new ForcedAnalysisResult(ResultType.NO_IDEA, null); // no idea, not forced
	}

	/**
	 * Applies the flag to the originalAnalysis where there are shared columns with
	 * additionalAnalysis
	 * @param originalAnalysis the {@link ColumnAnalysis} list to modify
	 * @param additionalAnalysis only modify if columns are shared with these
	 * @param flag the flag to apply to the original list
	 */
	private void apply(final List<ColumnAnalysis> originalAnalysis,
			final List<ColumnAnalysis> additionalAnalysis, final int flag) {
		for (final ColumnAnalysis additional : additionalAnalysis) {
			for (final ColumnAnalysis original : originalAnalysis) {
				if (additional.getColumn() == original.getColumn()) {
					original.addCondition(flag);
					break;
				}
			}
		}
	}

	/**
	 * Represents the outcome after applying forced analysis.
	 */
	private static enum ResultType {
		/**
		 * We're going to win.
		 */
		WIN,
		/**
		 * We're forced to make a move to prevent the opponent from winning.
		 */
		FORCED,
		/**
		 * No idea what this move will lead to.
		 */
		NO_IDEA
	}

	/**
	 * Helper container class for a {@link ResultType} and it's associated {@link List} of
	 * {@link ColumnAnalysis} objects.
	 */
	private static class ForcedAnalysisResult {
		private final List<ColumnAnalysis> analysisList;
		private final ResultType reslutType;

		public ForcedAnalysisResult(final ResultType reslutType,
				final List<ColumnAnalysis> analysisList) {
			this.reslutType = reslutType;
			this.analysisList = analysisList;
		}

		public List<ColumnAnalysis> getAnalysisList() {
			return analysisList;
		}

		public ResultType getReslutType() {
			return reslutType;
		}
	}
}
