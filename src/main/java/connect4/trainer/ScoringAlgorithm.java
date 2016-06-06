package connect4.trainer;

/**
 * <p>
 * Scores a column based on analysis. The following scoring algorithm is used:
 * <ol>
 * <li>score is {@link Integer#MAX_VALUE} if the player can win in one move (force win now)
 * <li>score is {@link Integer#MIN_VALUE} if the player can't play in the column, i.e. it's full
 * <li>score is 0 if no opinion
 * <li>if opponent plays ontop of your move and wins, badness (lose in 1)
 * <li>block opponent from wining on their next move (worth a lot) (block losing in 1)
 * <li>set up a trap (more than two columns to win), i.e. can win in two moves. Have to exclude
 * setting up opponent though (win in 2)
 * <li>force maneuvers. Play (forces opponent to block losing in 1)
 * </ol>
 * </p>
 */
public class ScoringAlgorithm {

	public int score(final ColumnAnalysis analysis) {
		if (analysis.hasCondition(ColumnAnalysis.FLAG_WIN_1)) {
			return Integer.MAX_VALUE;
		}
		if (analysis.hasCondition(ColumnAnalysis.FLAG_UNPLAYABLE)) {
			return Integer.MIN_VALUE;
		}
		if (analysis.hasCondition(ColumnAnalysis.FLAG_BLOCK_LOSS_1)) {
			return Integer.MAX_VALUE - 1;
		}
		return 0; // No opinion
	}
}