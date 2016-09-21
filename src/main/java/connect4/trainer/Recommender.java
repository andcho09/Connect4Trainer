package connect4.trainer;

import java.util.List;
import java.util.Random;

import connect4.Board;
import connect4.Disc;

/**
 * Recommends where to play.
 */
public abstract class Recommender {

	final ScoringAlgorithm scoringAlgorithm;
	final Random random;

	private List<ColumnAnalysis> lastBestColumnAnalysis;
	private List<ColumnAnalysis> lastColumnAnalysis;

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
		lastBestColumnAnalysis = null;
		lastColumnAnalysis = null;
	}

	/**
	 * Sets the last analysis.
	 */
	void setLastAnalysis(final List<ColumnAnalysis> lastBestColumnAnalysis,
			final List<ColumnAnalysis> lastColumnAnalysis) {
		this.lastBestColumnAnalysis = lastBestColumnAnalysis;
		this.lastColumnAnalysis = lastColumnAnalysis;
	}

	/**
	 * @return the best column analysis for the recent analysis. Could be more than one best column.
	 */
	protected List<ColumnAnalysis> getLastBestColumnAnalysis() {
		return lastBestColumnAnalysis;
	}

	/**
	 * @return the column analysis for the most recent analysis. This is a list. The first item in
	 *         the list is for the first column, the second item for the second column, etc.
	 */
	protected List<ColumnAnalysis> getLastColumnAnalysis() {
		return lastColumnAnalysis;
	}
}
