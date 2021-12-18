package connect4.trainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.IllegalMoveException;
import connect4.api.analysis.BoardAnalysis;
import connect4.api.analysis.ColumnAnalysis;
import connect4.trainer.BoardAnalyserFactory.ForcedAnalysisResult;

public abstract class AbstractForceBoardAnalyser {

	static final Logger LOGGER = LogManager.getLogger();

	public abstract List<ForcedAnalysisResult> analyse(final BoardAnalysis boardAnalysis, final Board board, final Disc currentPlayer);

	/**
	 * Perform 'forced' analysis, i.e. recursively analyse if the opponent is forced into a move.
	 * @param board the {@link Board} to analyse
	 * @param currentPlayer the {@link Disc} of the current player
	 * @param boardAnalysis the freshly analysed board
	 * @param depth how far down the rabbit hole we've gone
	 * @return recommendations of where to play and why. If empty, no analysis (opponent not forced
	 *         before end condition). Could be wining columns (if we can win), or forced columns
	 *         (we're forced to play there).
	 */
	List<ForcedAnalysisResult> doForcedAnalysis(final Board board, final Disc currentPlayer, final BoardAnalysis boardAnalysis,
			final int depth) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Beginning forced analysis for player " + currentPlayer.toString() + " for board:\n" + board.toString()
					+ " with analysis: " + StringUtils.join(boardAnalysis.iterator(), ", "));
		}

		final List<ForcedAnalysisResult> resultInWins = new ArrayList<>();

		// Check exit conditions
		final BoardAnalysis winColumns = boardAnalysis.getColumnsWithConditions(ScoringAlgorithm.getWinColumnAnalysisFlags());
		final BoardAnalysis forcedColumns = boardAnalysis.getColumnsWithConditions(ScoringAlgorithm.getForcedColumnAnalysisFlags());
		int forcedColumn = -1;
		if (winColumns.size() > 0) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Detected win scenario, returning");
			}
			resultInWins.add(new ForcedAnalysisResult(depth, winColumns));
			return resultInWins;
		} else if (forcedColumns.size() > 1) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Detected we're forced scenario to play more than one column, we probably lost, returning");
			}
			return Collections.emptyList();
		} else if (forcedColumns.size() == 1) {
			forcedColumn = forcedColumns.get(0).getColumn();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Detected we're forced into playing column " + forcedColumn);
			}
		}

		// Begin 'forced' analysis
		for (final ColumnAnalysis analysis : boardAnalysis) {
			if (forcedColumn != -1 && forcedColumn != analysis.getColumn()) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Skipping column " + analysis.getColumn() + " because we're forced to play column " + forcedColumn
							+ " instead");
				}
				continue;
			}
			if (analysis.hasCondition(ColumnAnalysis.FLAG_UNPLAYABLE)) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Can't play " + analysis.getColumn() + ", skipping");
				}
				continue;
			}
			final Board newBoard = new Board(board);
			try {
				newBoard.putDisc(analysis.getColumn(), currentPlayer);
			} catch (final IllegalMoveException e) {
				throw new RuntimeException("Something went wrong with forced analysis. Illegal moves should've been removed already.", e);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("What happens when " + currentPlayer.toString() + " plays column " + analysis.getColumn()
						+ " to create board?:\n" + newBoard.toString());
			}

			final Disc opponentPlayer = Disc.getOpposite(currentPlayer);
			final BoardAnalysis opponentAnalyses = BoardAnalyserHelper.analyse(newBoard, opponentPlayer);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Opponent " + opponentPlayer.toString() + "'s analysis is:\n "
						+ StringUtils.join(opponentAnalyses.iterator(), ", "));
			}

			BoardAnalysis opponentForcedColumns = opponentAnalyses.getColumnsWithConditions(ColumnAnalysis.FLAG_BLOCK_LOSS_1);
			if (opponentForcedColumns.size() > 1) {
				// If the opponent is forced to play more than two columns, they've lost. Normally this would be detected in the exit
				// conditions checks above but some cases will slip through if we terminate analysis early, e.g. we're forced to block
				// losing in 1 move and coincidentally this move also sets up a trap
				final BoardAnalysis result = new BoardAnalysis();
				result.add(analysis);
				resultInWins.add(new ForcedAnalysisResult(depth, result));
			} else if (opponentForcedColumns.size() == 0) {
				opponentForcedColumns = boardAnalysis.getColumnsWithConditions(ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE);
				if (opponentForcedColumns.size() > 1) {
					// TODO if they're blocking two traps we won. We should've detected this earlier.
					throwMoreThanForcedMoveError("I think we missed something. The opponent is forced into blocking more than one trap.",
							board, currentPlayer, boardAnalysis, analysis.getColumn(), newBoard, opponentForcedColumns);
				}
			}

			if (opponentForcedColumns.size() == 1) {
				final int opponentForcedColumn = opponentForcedColumns.get(0).getColumn();
				final Board opponentBoard = new Board(newBoard);
				try {
					opponentBoard.putDisc(opponentForcedColumn, opponentPlayer);
				} catch (final IllegalMoveException e) {
					throw new RuntimeException(
							"Something went wrong with forced analysis. Opponent is forced to play in a column that's unplayable. This shouldn't happen.",
							e);
				}

				// TODO should we check that we didn't just lose right here? Should be eliminated by the 'are we forced check before'
				final BoardAnalysis forcedAnalyses = BoardAnalyserHelper.analyse(opponentBoard, currentPlayer);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Opponent " + opponentPlayer.toString() + " is forced to play column "
							+ opponentForcedColumns.get(0).getColumn() + " which creates board:\n" + opponentBoard.toString()
							+ "\nRecursively calling forced analysis again...");
				}
				final List<ForcedAnalysisResult> results = doForcedAnalysis(opponentBoard, currentPlayer, forcedAnalyses, depth + 1);
				for (final ForcedAnalysisResult result : results) {
					if (!result.isLoss()) {
						result.pushMove(analysis.getColumn());
						result.pushOpponentMove(opponentForcedColumn);
						resultInWins.add(result);
					}
				}
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Opponent " + opponentPlayer.toString() + " wasn't forced");
				}
				continue; // Opponent isn't forced, can't do any more 'forced' analysis
			}
		}

		return resultInWins;
	}

	/**
	 * Helper method to throw a {@link RuntimeException} when there's a win condition the analyser missed and was detected during 'forced'
	 * analysis. This is effectively an assertion.
	 * @param initialMessage the initial message
	 * @param board the original {@link Board} we're analysing
	 * @param currentPlayer the {@link Disc} of the current player
	 * @param currentAnalysis the most recent {@link BoardAnalysis}
	 * @param currentColumn the current column (0-based) being analysed
	 * @param newBoard the {@link Board} we created
	 * @param opponentForcedColumns {@link BoardAnalysis} showing where the opponent is forced to
	 *        make a move
	 */
	void throwMoreThanForcedMoveError(final String initialMessage, final Board board, final Disc currentPlayer,
			final BoardAnalysis currentAnalysis, final int currentColumn, final Board newBoard, final BoardAnalysis opponentForcedColumns) {
		final StringBuilder message = new StringBuilder(initialMessage + "\n");
		message.append(" We're playing as " + currentPlayer.toString() + " with board:\n" + board.toString());
		message.append(" Original analysis is:\n  " + StringUtils.join(currentAnalysis, ", ") + "\n");
		message.append(" Candidate column is " + currentColumn + " which creates board:\n" + newBoard.toString());
		message.append(" Opponent analysis is:\n  " + StringUtils.join(opponentForcedColumns, ", "));
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(message);
		}
		throw new RuntimeException(message.toString());
	}
}
