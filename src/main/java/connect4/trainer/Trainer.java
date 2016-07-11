package connect4.trainer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import connect4.Board;
import connect4.Disc;
import connect4.trainer.ColumnAnalysis.ColumnAnalyser;

/**
 * <p>
 * Gives hints for Connect4.
 * </p>
 * <p>
 * It advises along the following guidelines:
 * <ol>
 * <li>Play where you can win in one move
 * <li>Don't play where you lose in the next turn, i.e:
 * <ol>
 * <li>Block the opponent from getting four in a row in their next turn
 * </ol>
 * <li>Play where you have more than one winning moving in your next term (i.e. force a win in two
 * moves)
 * </ol>
 * </p>
 * <p>
 * Decisions are based on scoring each column.
 * <p>
 * It's possible multiple columns may score the same value in which case ties are broken by:
 * <ol>
 * <li>Middle column is worth {@link Board#getNumCols()}
 * <li>Each column away from the middle is worth {@link Board#getNumCols()} - n where n is the
 * number of columns away from the middle
 * <li>Otherwise pick randomly
 * </ol>
 * </p>
 */
public class Trainer {

	private final ScoringAlgorithm scoringAlgorithm;
	private final Random random;

	private List<ColumnAnalysis> lastBestColumnAnalysis;
	private List<ColumnAnalysis> lastColumnAnalysis;

	public Trainer() {
		scoringAlgorithm = new ScoringAlgorithm();
		this.random = new Random();
	}

	/**
	 * Analyses the board and recommends where to play
	 * @param board the {@link Board} to analyse.
	 * @param currentPlayer the {@link Disc} of the current player
	 * @return the column the trainer recommends to play (0-based)
	 */
	public int analyse(final Board board, final Disc currentPlayer) {
		resetLast();

		// Analysis phase
		final List<ColumnAnalysis> analysisList = new LinkedList<ColumnAnalysis>();
		for (int c = 0; c < board.getNumCols(); c++) {
			analysisList.add(analyse(board, currentPlayer, c));
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

		lastBestColumnAnalysis = bestColumnAnalysis;
		lastColumnAnalysis = analysisList;

		// Tie breaking phase
		if (bestColumnAnalysis.size() == 1) {
			return bestColumnAnalysis.get(0).getColumn();
		} else {
			final int randomInt = random.nextInt(bestColumnAnalysis.size());
			return bestColumnAnalysis.get(randomInt).getColumn();
		}
	}

	private ColumnAnalysis analyse(final Board board, final Disc currentPlayer, final int column) {
		final ColumnAnalysis analysis = new ColumnAnalysis(column);
		for (final ColumnAnalyser columnAnalyser : ColumnAnalysis.ANALYSERS) {
			final int flag = columnAnalyser.flag(board, currentPlayer, column);
			analysis.addCondition(flag);
			if (analysis.hasCondition(ColumnAnalysis.FLAG_UNPLAYABLE)) {
				continue;
			}
		}
		return analysis;
	}

	/**
	 * Reset the last analysis.
	 */
	protected void resetLast() {
		lastBestColumnAnalysis = null;
		lastColumnAnalysis = null;
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
