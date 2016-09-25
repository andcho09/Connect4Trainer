package connect4.trainer;

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
	public BoardAnalysis analyse(final Board board, final Disc currentPlayer) {
		final BoardAnalysis boardAnalysis = new BoardAnalysis();
		for (int c = 0; c < board.getNumCols(); c++) {
			boardAnalysis.add(analyse(board, currentPlayer, c));
		}
		return boardAnalysis;
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
	private boolean isAnalysisDone(final ColumnAnalysis analysis) {
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
}
