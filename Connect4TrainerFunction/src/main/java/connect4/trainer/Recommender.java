package connect4.trainer;

import java.util.Random;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.analysis.BoardAnalysis;

/**
 * Recommends where to play.
 */
public abstract class Recommender {

	final ScoringAlgorithm scoringAlgorithm;
	final Random random;

	private BoardAnalysis lastBestBoardAnalysis;
	private BoardAnalysis lastBoardAnalysis;

	public Recommender() {
		scoringAlgorithm = new ScoringAlgorithm();
		this.random = new Random();
	}

	/**
	 * Analyses the board and recommends where to play.
	 * @param board the {@link Board} to analyse.
	 * @param currentPlayer the {@link Disc} of the current player
	 * @return the column the trainer recommends to play (0-based)
	 */
	public abstract int recommend(final Board board, final Disc currentPlayer);

	/**
	 * Reset the last analysis.
	 */
	protected void resetLast() {
		lastBestBoardAnalysis = null;
		lastBoardAnalysis = null;
	}

	/**
	 * Sets the last analysis.
	 */
	void setLastAnalysis(final BoardAnalysis lastBestBoardAnalysis, final BoardAnalysis lastBoardAnalysis) {
		this.lastBestBoardAnalysis = lastBestBoardAnalysis;
		this.lastBoardAnalysis = lastBoardAnalysis;
	}

	/**
	 * @return the best {@link BoardAnalysis} for the recent analysis. Could be more than one best
	 *         column.
	 */
	public BoardAnalysis getLastBestBoardAnalysis() {
		return lastBestBoardAnalysis;
	}

	/**
	 * @return the {@link BoardAnalysis} for the most recent analysis. This is a list. The first
	 *         item in the list is for the first column, the second item for the second column, etc.
	 */
	protected BoardAnalysis getLastBoardAnalysis() {
		return lastBoardAnalysis;
	}
}
