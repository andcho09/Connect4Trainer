package connect4.trainer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import connect4.Board;
import connect4.Disc;
import connect4.trainer.ColumnAnalyserFactory.ColumnAnalyser;

/**
 * Uses {@link ColumnAnalyser} to analyse a {@link Board}. This class is stateless.
 */
public class BoardAnalyser {

	/**
	 * Run analyses against the board.
	 * @param board the {@link Board}
	 * @param currentPlayer the {@link Disc} of the player we're analysing for
	 * @return
	 */
	public List<ColumnAnalysis> analyse(final Board board, final Disc currentPlayer) {
		final List<ColumnAnalysis> analysisList = new LinkedList<ColumnAnalysis>();
		for (int c = 0; c < board.getNumCols(); c++) {
			analysisList.add(analyse(board, currentPlayer, c));
		}
		return analysisList;
	}

	private ColumnAnalysis analyse(final Board board, final Disc currentPlayer, final int column) {
		final ColumnAnalysis analysis = new ColumnAnalysis(column);
		for (final ColumnAnalyser columnAnalyser : ColumnAnalyserFactory.getAnalysers()) {
			columnAnalyser.flag(board, currentPlayer, column, analysis);
			if (isAnalysisDone(analysis)) {
				break;
			}
		}
		return analysis;
	}

	/**
	 * Checks whether there's any point doing any more analysis.
	 * @param analysis the current analysis
	 * @return <code>true</code> if analysis should continue, else <code>false</code> (we won, they
	 *         won, including traps, we're forced into a block)
	 */
	public boolean isAnalysisDone(final ColumnAnalysis analysis) {
		// TODO this is a good candidate for a column analysis(s) class
		if (analysis.hasCondition(ColumnAnalysis.FLAG_UNPLAYABLE)
				|| analysis.hasCondition(ColumnAnalysis.FLAG_WIN_1)
				|| analysis.hasCondition(ColumnAnalysis.FLAG_BLOCK_LOSS_1)
				|| analysis.hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE)
				|| analysis.hasCondition(ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get columns where we won (or where we can execute a trap to win).
	 * @param currentAnalysis the analysis
	 * @return a {@link List} of {@link ColumnAnalysis} where we won or can win. Could be empty.
	 */
	public List<ColumnAnalysis> getWinColumns(final List<ColumnAnalysis> currentAnalysis) {
		// TODO this is a good candidate for a column analysis(s) class
		final List<ColumnAnalysis> result = new ArrayList<ColumnAnalysis>();
		for (final ColumnAnalysis columnAnalysis : currentAnalysis) {
			if (columnAnalysis.hasCondition(ColumnAnalysis.FLAG_WIN_1)
					|| columnAnalysis.hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE)) {
				result.add(columnAnalysis);
			}
		}
		return result;
	}

	/**
	 * Get columns where we're forced to play.
	 * @param currentAnalysis the analysis
	 * @return a {@link List} of {@link ColumnAnalysis} where we are forced to play. Could be empty
	 */
	public List<ColumnAnalysis> getForcedColumns(final List<ColumnAnalysis> currentAnalysis) {
		// TODO this is a good candidate for a column analysis(s) class
		final List<ColumnAnalysis> result = new ArrayList<ColumnAnalysis>();
		for (final ColumnAnalysis columnAnalysis : currentAnalysis) {
			if (columnAnalysis.hasCondition(ColumnAnalysis.FLAG_BLOCK_LOSS_1)
					|| columnAnalysis.hasCondition(ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE)) {
				result.add(columnAnalysis);
			}
		}
		return result;
	}

	/**
	 * Get columns with the following condition
	 * @param currentAnalysis the analysis
	 * @param flag the condition to return for
	 * @return a {@link List} of {@link ColumnAnalysis} which have the flag. Could be empty
	 */
	public List<ColumnAnalysis> getColumnsWithCondition(final List<ColumnAnalysis> currentAnalysis,
			final int flag) {
		// TODO this is a good candidate for a column analysis(s) class
		final List<ColumnAnalysis> result = new ArrayList<ColumnAnalysis>();
		for (final ColumnAnalysis columnAnalysis : currentAnalysis) {
			if (columnAnalysis.hasCondition(flag)) {
				result.add(columnAnalysis);
			}
		}
		return result;
	}
}
