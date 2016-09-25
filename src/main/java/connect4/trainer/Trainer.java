package connect4.trainer;

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
		final BoardAnalysis analyses = BOARD_ANALYSER.analyse(board, currentPlayer);

		// Scoring phase
		final BoardAnalysis bestBoardAnalysis = new BoardAnalysis();
		int bestScore = Integer.MIN_VALUE;
		for (final ColumnAnalysis analysis : analyses) {
			final int score = scoringAlgorithm.score(analysis);
			if (score > bestScore) {
				bestScore = score;
				bestBoardAnalysis.clear();
				bestBoardAnalysis.add(analysis);
			} else if (score == bestScore) {
				bestBoardAnalysis.add(analysis);
			}
		}

		setLastAnalysis(bestBoardAnalysis, analyses);

		// Tie breaking phase
		if (bestBoardAnalysis.size() == 1) {
			return bestBoardAnalysis.get(0).getColumn();
		} else {
			final int randomInt = random.nextInt(bestBoardAnalysis.size());
			return bestBoardAnalysis.get(randomInt).getColumn();
		}
	}
}
