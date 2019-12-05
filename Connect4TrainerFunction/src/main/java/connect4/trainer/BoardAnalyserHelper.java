package connect4.trainer;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.analysis.BoardAnalysis;
import connect4.api.analysis.ColumnAnalysis;
import connect4.trainer.ColumnAnalyserFactory.ColumnAnalyser;

/**
 * Uses {@link ColumnAnalyser} to analyse a {@link Board}. This class is stateless.
 */
public class BoardAnalyserHelper {

	private BoardAnalyserHelper() {
	}

	/**
	 * Runs basic {@link ColumnAnalyser}s against the board.
	 * @param board the {@link Board}
	 * @param currentPlayer the {@link Disc} of the player we're analysing for
	 * @return the {@link BoardAnalysis} containing the results of the analysis
	 */
	public static BoardAnalysis analyse(final Board board, final Disc currentPlayer) {
		final BoardAnalysis boardAnalysis = new BoardAnalysis();
		for (int c = 0; c < board.getNumCols(); c++) {
			boardAnalysis.add(analyse(board, currentPlayer, c));
		}
		return boardAnalysis;
	}

	private static ColumnAnalysis analyse(final Board board, final Disc currentPlayer, final int column) {
		final ColumnAnalysis analysis = new ColumnAnalysis(column);
		for (final ColumnAnalyser columnAnalyser : ColumnAnalyserFactory.getAnalysers()) {
			columnAnalyser.flag(board, currentPlayer, column, analysis);
			if (ScoringAlgorithm.isAnalysisDone(analysis)) {
				break;
			}
		}
		return analysis;
	}
}
