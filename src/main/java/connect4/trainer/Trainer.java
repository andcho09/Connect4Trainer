package connect4.trainer;

import java.util.ArrayList;
import java.util.List;

import connect4.Board;
import connect4.Disc;
import connect4.trainer.BoardAnalyserFactory.BoardAnalyser;
import connect4.trainer.BoardAnalyserFactory.ForcedAnalysisResult;

/**
 * Trainer capable of predicting forced moves.
 */
public class Trainer extends Recommender {

	private List<ForcedAnalysisResult> lastForcedAnalysisResults = new ArrayList<ForcedAnalysisResult>();

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
		final BoardAnalysis boardAnalysis = BoardAnalyserHelper.analyse(board, currentPlayer);

		// Check 'forced'
		final List<ForcedAnalysisResult> forcedAnalysisResults = new ArrayList<ForcedAnalysisResult>();
		final List<BoardAnalyser> analysers = BoardAnalyserFactory.getAnalysers();
		for (final BoardAnalyser boardAnalyser : analysers) {
			forcedAnalysisResults
					.addAll(boardAnalyser.analyse(boardAnalysis, board, currentPlayer));
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
		this.lastForcedAnalysisResults = forcedAnalysisResults; // TODO this could be a sequence of
																// how we lose

		// Tie breaking phase
		if (bestBoardAnalysis.size() == 1) {
			return bestBoardAnalysis.get(0).getColumn();
		} else {
			final int randomInt = random.nextInt(bestBoardAnalysis.size());
			return bestBoardAnalysis.get(randomInt).getColumn();
		}
	}

	@Override
	protected void resetLast() {
		super.resetLast();
		this.lastForcedAnalysisResults = null;
	}

	List<ForcedAnalysisResult> getLastForcedAnalysisResults() {
		return lastForcedAnalysisResults;
	}
}
