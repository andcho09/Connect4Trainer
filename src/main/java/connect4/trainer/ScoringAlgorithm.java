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

	private static final Integer[] FLAGS_WINS = new Integer[] { ColumnAnalysis.FLAG_WIN_1, ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE };
	private static final Integer[] FLAGS_FORCED = new Integer[] { ColumnAnalysis.FLAG_BLOCK_LOSS_1,
			ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE };

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
		if (analysis.hasCondition(ColumnAnalysis.FLAG_ENABLE_OPPONENT_WIN)) {
			return Integer.MIN_VALUE + 1;
		}
		if (analysis.hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE)) {
			return Integer.MAX_VALUE - 2;
		}
		if (analysis.hasCondition(ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE)) {
			return Integer.MAX_VALUE - 3;
		}
		if (analysis.hasCondition(ColumnAnalysis.FLAG_FORCED_WIN)) {
			return Integer.MAX_VALUE - 4;
		}
		if (analysis.hasCondition(ColumnAnalysis.FLAG_BLOCK_FORCED_WIN)) {
			return Integer.MAX_VALUE - 5;
		}
		if (analysis.hasCondition(ColumnAnalysis.FLAG_MAKE_3_SETUP)) {
			return 1000; // This is probably a good play
		}
		if (analysis.hasCondition(ColumnAnalysis.FLAG_MAKE_3_DOUBLE_SETUP)) {
			return 1000000; // This is very likely a good play
		}
		if (analysis.hasCondition(ColumnAnalysis.FLAG_BOTTOM_CENTER_FREE)) {
			return 1; // Possibly a good play
		}
		return 0; // No opinion
	}

	/**
	 * Checks whether there's any point doing any more analysis.
	 * @param analysis the current analysis
	 * @return <code>true</code> if analysis should continue, else <code>false</code> (we won, they
	 *         won, including traps, we're forced into a block)
	 */
	public static boolean isAnalysisDone(final ColumnAnalysis analysis) {
		if (analysis.hasCondition(ColumnAnalysis.FLAG_UNPLAYABLE) || analysis.hasCondition(ColumnAnalysis.FLAG_WIN_1)
				|| analysis.hasCondition(ColumnAnalysis.FLAG_BLOCK_LOSS_1)
				|| analysis.hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE)) {
			return true;
		} else {
			return false;
		}
	}

	public static Integer[] getWinColumnAnalysisFlags() {
		return FLAGS_WINS;
	}

	public static Integer[] getForcedColumnAnalysisFlags() {
		return FLAGS_FORCED;
	}
}
