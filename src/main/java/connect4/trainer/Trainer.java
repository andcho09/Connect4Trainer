package connect4.trainer;

import java.util.ArrayList;
import java.util.List;

import connect4.Board;
import connect4.Disc;

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
public class Trainer extends Recommender {

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
}
