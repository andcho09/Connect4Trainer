package connect4.forwarder;

import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.analysis.BoardAnalysis;
import connect4.api.analysis.ColumnAnalysis;

/**
 * Forwards interesting boards to a topic for future storage/analysis.
 */
public abstract class AbstractBoardForwarder {

	static final int INTERESTING_FLAGS = ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE | ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE
			| ColumnAnalysis.FLAG_FORCED_WIN | ColumnAnalysis.FLAG_BLOCK_FORCED_WIN;

	private static final Logger LOGGER = Logger.getLogger(AbstractBoardForwarder.class);

	/**
	 * Receive a board and consider forwarding it.
	 * @param board the board
	 * @param currentPlayer whose turn it is
	 * @param boardAnalysis analysis for the current player
	 * @return <code>true</code> if the board is received (not necessarily forwarded) or <code>false</code> if the board is dropped
	 */
	public boolean receive(final Board board, final Disc currentPlayer, final BoardAnalysis boardAnalysis) {
		if (!isInteresting(boardAnalysis)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("Board is not interesting, dropping. Board=%s, CurrentPlayer=%s, Analysis=%s", board,
						currentPlayer.getSymbol(), boardAnalysis.stream().filter(n -> n.getFlags() != ColumnAnalysis.FLAG_NO_OPINION)
								.map(n -> "col=" + n.getColumn() + " flag=" + n.getFlags()).collect(Collectors.joining(", "))));
			}
			return false;
		}
		final Object[] normaliseResult = normalise(currentPlayer, board, boardAnalysis);
		forward((Disc) normaliseResult[0], (Board) normaliseResult[1], (BoardAnalysis) normaliseResult[2]);
		return true;
	}

	protected boolean isInteresting(final BoardAnalysis boardAnalysis) {
		for (final ColumnAnalysis columnAnalysis : boardAnalysis) {
			if (columnAnalysis.hasConditions(INTERESTING_FLAGS)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Normalise the board request which is useful for creating hashes or persistent storage. This will:
	 * <ol>
	 * <li>Orientates the board so that most of the discs are on the left. This allows
	 * mirror-image games to be considered the same for analysis.
	 * <li>Swaps the disc so that there are more yellow than red discs. Doesn't swap if the number of discs are equal.
	 * </ol>
	 *
	 * @return array of {currentPlayer Disc, Board, BoardAnalysis}
	 */
	protected Object[] normalise(final Disc currentPlayer, final Board board, final BoardAnalysis boardAnalysis) {
		Disc normalisedCurrentPlayer = currentPlayer;
		Board normalisedBoard = board;
		final BoardAnalysis normalisedBoardAnalysis = boardAnalysis;
		if (Disc.YELLOW != currentPlayer) {
			normalisedCurrentPlayer = Disc.YELLOW;
			normalisedBoard = board.swap();
		}
		if (normalisedBoard.reverseToLeft()) {
			normalisedBoardAnalysis.reverse();
		}
		return new Object[] { normalisedCurrentPlayer, normalisedBoard, normalisedBoardAnalysis };
	}

	/**
	 * Forward the board to the destination.
	 */
	protected abstract void forward(final Disc currentPlayer, final Board board, final BoardAnalysis boardAnalysis);

	/**
	 * Warm up this forwarder to help with cold starts.
	 */
	public void warmUp() {
	}
}
