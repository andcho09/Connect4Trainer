package connect4.trainer;

import java.util.ArrayList;
import java.util.List;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.analysis.BoardAnalysis;
import connect4.api.analysis.ColumnAnalysis;
import connect4.forwarder.AbstractBoardForwarder;
import connect4.forwarder.SinkBoardForwader;
import connect4.trainer.BoardAnalyserFactory.ForcedAnalysisResult;

/**
 * Trainer capable of predicting forced moves. Note, trainers are almost stateless, they remember the last {@link ForcedAnalysisResult}. You
 * can construct a new {@link Trainer} each time, or reuse one (like the JUnit tests do).
 */
public class Trainer extends Recommender {

	private final AbstractBoardForwarder boardForwarder;
	private List<ForcedAnalysisResult> lastForcedAnalysisResults = new ArrayList<>();

	public Trainer() {
		this(SinkBoardForwader.INSTANCE);
	}

	public Trainer(final AbstractBoardForwarder boardForwarder) {
		this.boardForwarder = boardForwarder;
	}

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
		final List<ForcedAnalysisResult> forcedAnalysisResults = new ArrayList<>();
		final List<AbstractForceBoardAnalyser> analysers = BoardAnalyserFactory.getForcedAnalysers();
		for (final AbstractForceBoardAnalyser forcedBoardAnalyser : analysers) {
			forcedAnalysisResults.addAll(forcedBoardAnalyser.analyse(boardAnalysis, board, currentPlayer));
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

		boardForwarder.receive(board, currentPlayer, bestBoardAnalysis);
		setLastAnalysis(bestBoardAnalysis, boardAnalysis);
		// TODO this could be a sequence of how we lose if it's for the opponent
		this.lastForcedAnalysisResults = forcedAnalysisResults;

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
