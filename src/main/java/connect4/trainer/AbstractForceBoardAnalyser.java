package connect4.trainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import connect4.Board;
import connect4.Disc;
import connect4.IllegalMoveException;
import connect4.trainer.BoardAnalyserFactory.ForcedAnalysisResult;

public abstract class AbstractForceBoardAnalyser {

	private final Logger LOGGER = Logger.getLogger(getClass());

	public abstract List<ForcedAnalysisResult> analyse(final BoardAnalysis boardAnalysis,
			final Board board, final Disc currentPlayer);

	/**
	 * Perform 'forced' analysis, i.e. recursively analyse if the opponent is forced into a move.
	 * @param board the {@link Board} to analse
	 * @param currentPlayer the {@link Disc} of the current player
	 * @param boardAnalysis the freshly analysed board
	 * @param depth how far down the rabbit hole we've gone
	 * @return recommendations of where to play and why. If empty, no analysis (opponent not forced
	 *         before end condition). Could be wining columns (if we can win), or forced columns
	 *         (we're forced to play there).
	 */
	List<ForcedAnalysisResult> doForcedAnalysis(final Board board, final Disc currentPlayer,
			final BoardAnalysis boardAnalysis, final int depth) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Beginning forced analysis for player " + currentPlayer.toString()
					+ " for board:\n" + board.toString() + " with analysis: "
					+ StringUtils.join(boardAnalysis.iterator(), ", "));
		}

		final List<ForcedAnalysisResult> resultInWins = new ArrayList<ForcedAnalysisResult>();

		// Check exit conditions
		// TODO scoring algorithm
		final BoardAnalysis winColumns = boardAnalysis.getColumnsWithConditions(
				ColumnAnalysis.FLAG_WIN_1, ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE);
		final BoardAnalysis forcedColumns = boardAnalysis.getColumnsWithConditions(
				ColumnAnalysis.FLAG_BLOCK_LOSS_1, ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE);
		if (winColumns.size() > 0) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Detected win scenario, returning");
			}
			resultInWins.add(new ForcedAnalysisResult(depth, winColumns));
			return resultInWins;
		} else if (forcedColumns.size() > 0) {
			// TODO we shouldn't return here, just cut down on where we play, play that, and
			// continue analyising (it's still forced analysis)
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Detected we're forced scenario, returning");
			}
			return Collections.emptyList();
		}

		// Begin 'forced' analysis
		for (final ColumnAnalysis analysis : boardAnalysis) {
			if (analysis.hasCondition(ColumnAnalysis.FLAG_UNPLAYABLE)) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Can't play " + analysis.getColumn() + ", skipping");
				}
				continue;
			}
			final Board newBoard = new Board(board);
			try {
				newBoard.putDisc(analysis.getColumn(), currentPlayer);
			} catch (final IllegalMoveException swallow) {
				continue;
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("What happens when " + currentPlayer.toString() + " plays column "
						+ analysis.getColumn() + " to create board?:\n" + newBoard.toString());
			}

			final Disc opponentPlayer = Disc.getOpposite(currentPlayer);
			final BoardAnalysis opponentAnalyses = BoardAnalyserHelper.analyse(newBoard,
					opponentPlayer);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Opponent " + opponentPlayer.toString() + "'s analysis is:\n "
						+ StringUtils.join(opponentAnalyses.iterator(), ", "));
			}

			BoardAnalysis opponentForcedColumns = opponentAnalyses
					.getColumnsWithConditions(ColumnAnalysis.FLAG_BLOCK_LOSS_1);
			if (opponentForcedColumns.size() > 1) {
				// TODO if there's two 'must block now' situations then we should have won and
				// detected this before
				throwMoreThanForcedMoveError(
						"I think we missed something. The opponent is forced into blocking more than one column immediately.",
						board, currentPlayer, boardAnalysis, analysis.getColumn(), newBoard,
						opponentForcedColumns);
			} else if (opponentForcedColumns.size() == 0) {
				opponentForcedColumns = boardAnalysis
						.getColumnsWithConditions(ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE);
				if (opponentForcedColumns.size() > 1) {
					// TODO if they're blocking two traps we won. We should've detected this
					// earlier.
					throwMoreThanForcedMoveError(
							"I think we missed something. The opponent is forced into blocking more than one trap.",
							board, currentPlayer, boardAnalysis, analysis.getColumn(), newBoard,
							opponentForcedColumns);
				}
			}

			if (opponentForcedColumns.size() == 1) {
				final int opponentForcedColumn = opponentForcedColumns.get(0).getColumn();
				final Board opponentBoard = new Board(newBoard);
				try {
					opponentBoard.putDisc(opponentForcedColumn, opponentPlayer);
				} catch (final IllegalMoveException e) {
					continue; // We're forced to play here but can't???
				}

				// TODO should we check that we didn't just lose right here? Should be
				// eliminated by the 'are we forced check before'
				final BoardAnalysis forcedAnalyses = BoardAnalyserHelper.analyse(opponentBoard,
						currentPlayer);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(
							"Opponent " + opponentPlayer.toString() + " is forced to play column "
									+ opponentForcedColumns.get(0).getColumn()
									+ " which creates board:\n" + opponentBoard.toString()
									+ "\nRecursively calling forced analysis again...");
				}
				final List<ForcedAnalysisResult> results = doForcedAnalysis(opponentBoard,
						currentPlayer, forcedAnalyses, depth + 1);
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
	 * Helper method to throw a {@link RuntimeException} when there's a win condition the analyser
	 * missed and was detected during 'forced' analysis. This is effectively an assertion.
	 * @param initialMessage the initial message
	 * @param board the original {@link Board} we're analysing
	 * @param currentPlayer the {@link Disc} of the current player
	 * @param currentAnalysis the most recent {@link BoardAnalysis}
	 * @param currentColumn the current column (0-based) being analysed
	 * @param newBoard the {@link Board} we created
	 * @param opponentForcedColumns {@link BoardAnalysis} showing where the opponent is forced to
	 *        make a move
	 */
	void throwMoreThanForcedMoveError(final String initialMessage, final Board board,
			final Disc currentPlayer, final BoardAnalysis currentAnalysis, final int currentColumn,
			final Board newBoard, final BoardAnalysis opponentForcedColumns) {
		final StringBuilder message = new StringBuilder(initialMessage + "\n");
		message.append(" We're playing as " + currentPlayer.toString() + " with board:\n"
				+ board.toString());
		message.append(
				" Original analysis is:\n  " + StringUtils.join(currentAnalysis, ", ") + "\n");
		message.append(" Candidate column is " + currentColumn + " which creates board:\n"
				+ newBoard.toString());
		message.append(
				" Opponent analysis is:\n  " + StringUtils.join(opponentForcedColumns, ", "));
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(message);
		}
		throw new RuntimeException(message.toString());
	}
}
