package connect4.forwarder;

import static connect4.BoardHelperTest.RESOURCES_DIR;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.analysis.BoardAnalysis;
import connect4.api.analysis.ColumnAnalysis;
import connect4.loader.BoardLoader;

public class AbstractBoardForwarderTest {

	@Test
	public void testNormalise() throws IOException {
		final Board expectedBoard = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_reverse1a_input.txt"));
		assertNormalise(Disc.YELLOW, expectedBoard, createBoardAnalysis(0, 1, 2, 3, 4, 5, 6), Disc.YELLOW, expectedBoard,
				createBoardAnalysis(0, 1, 2, 3, 4, 5, 6));

		assertNormalise(Disc.YELLOW, expectedBoard, createBoardAnalysis(0, 1, 2, 3, 4, 5, 6), Disc.RED,
				BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_reverse1b_input.txt")), createBoardAnalysis(0, 1, 2, 3, 4, 5, 6));

		assertNormalise(Disc.YELLOW, expectedBoard, createBoardAnalysis(6, 5, 4, 3, 2, 1, 0), Disc.YELLOW,
				BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_reverse1c_input.txt")), createBoardAnalysis(0, 1, 2, 3, 4, 5, 6));

		assertNormalise(Disc.YELLOW, expectedBoard, createBoardAnalysis(6, 5, 4, 3, 2, 1, 0), Disc.RED,
				BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_reverse1d_input.txt")), createBoardAnalysis(0, 1, 2, 3, 4, 5, 6));
	}

	private void assertNormalise(final Disc expectedPlayer, final Board expectedBoard, final BoardAnalysis expectedBoardAnalysis,
			final Disc player, final Board board, final BoardAnalysis boardAnalysis) {
		final TestBoardForwarder forwarder = new TestBoardForwarder();
		forwarder.receive(new Board(board), player, boardAnalysis);
		Assert.assertEquals(expectedBoard, forwarder.getBoard());
		Assert.assertEquals(expectedBoardAnalysis, forwarder.getBoardAnalysis());
		Assert.assertEquals(expectedPlayer, forwarder.getCurrentPlayer());
	}

	private BoardAnalysis createBoardAnalysis(final int... colFlags) {
		final BoardAnalysis analysis = new BoardAnalysis();
		for (int i = 0; i < colFlags.length; i++) {
			final ColumnAnalysis columnAnalysis = new ColumnAnalysis(i);
			columnAnalysis.setFlags(colFlags[i]);
			analysis.add(columnAnalysis);
		}
		return analysis;
	}

	/**
	 * Exposes the {@link AbstractBoardForwarder#normalise(Disc, Board, BoardAnalysis)} method when
	 * {@link #receive(Board, Disc, BoardAnalysis)} is called.
	 */
	private static class TestBoardForwarder extends AbstractBoardForwarder {

		private Board board;
		private Disc currentPlayer;
		private BoardAnalysis boardAnalysis;

		@Override
		public boolean receive(final Board board, final Disc currentPlayer, final BoardAnalysis boardAnalysis) {
			final Object[] result = normalise(currentPlayer, board, boardAnalysis);
			this.currentPlayer = (Disc) result[0];
			this.board = (Board) result[1];
			this.boardAnalysis = (BoardAnalysis) result[2];
			return true;
		}

		@Override
		protected void forward(final Disc currentPlayer, final Board board, final BoardAnalysis boardAnalysis) {
		}

		public Board getBoard() {
			return this.board;
		}

		public Disc getCurrentPlayer() {
			return this.currentPlayer;
		}

		public BoardAnalysis getBoardAnalysis() {
			return this.boardAnalysis;
		}

	}
}
