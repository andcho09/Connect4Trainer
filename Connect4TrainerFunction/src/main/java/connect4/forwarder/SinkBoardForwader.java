package connect4.forwarder;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.analysis.BoardAnalysis;

/**
 * Just sinks boards.
 */
public class SinkBoardForwader extends AbstractBoardForwarder {

	public static final SinkBoardForwader INSTANCE = new SinkBoardForwader();

	private SinkBoardForwader() {
		// Private constructor so no one can instantiate
	}

	@Override
	public boolean receive(final Board board, final Disc currentPlayer, final BoardAnalysis boardAnalysis) {
		return false;
	}

	@Override
	protected void forward(final Disc currentPlayer, final Board board, final BoardAnalysis boardAnalysis) {
	}
}
