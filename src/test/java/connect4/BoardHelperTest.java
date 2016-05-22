package connect4;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import connect4.loader.BoardLoader;

public class BoardHelperTest {

	public final static String RESOURCES_DIR = "src/test/resources/";

	/**
	 * Test null winners.
	 */
	@Test
	public void testHasWinner1() throws IOException {
		assertHasWinner(null, "BoardLoaderTest_1.txt");
	}

	/**
	 * Test winners.
	 */
	@Test
	public void testHasWinner2() throws IOException {
		// Different types of vertical wins
		assertHasWinner(Disc.RED, "BoardHelperTest_hasWinner1.txt");
		assertHasWinner(Disc.RED, "BoardHelperTest_hasWinner1a.txt");
		assertHasWinner(Disc.RED, "BoardHelperTest_hasWinner1b.txt");
		assertHasWinner(Disc.YELLOW, "BoardHelperTest_hasWinner1c.txt");
		assertHasWinner(Disc.RED, "BoardHelperTest_hasWinner1d.txt");
		assertHasWinner(Disc.YELLOW, "BoardHelperTest_hasWinner1e.txt");

		// Different types of horizontal wins
		assertHasWinner(Disc.RED, "BoardHelperTest_hasWinner2a.txt");
		assertHasWinner(Disc.YELLOW, "BoardHelperTest_hasWinner2b.txt");
		assertHasWinner(Disc.RED, "BoardHelperTest_hasWinner2c.txt");
		assertHasWinner(Disc.YELLOW, "BoardHelperTest_hasWinner2d.txt");
		assertHasWinner(Disc.RED, "BoardHelperTest_hasWinner2e.txt");

		// Different types of diagonal wins (sw to ne)
		assertHasWinner(Disc.RED, "BoardHelperTest_hasWinner3a.txt");
		assertHasWinner(Disc.RED, "BoardHelperTest_hasWinner3b.txt");
		assertHasWinner(Disc.YELLOW, "BoardHelperTest_hasWinner3c.txt");
		assertHasWinner(Disc.YELLOW, "BoardHelperTest_hasWinner3d.txt");

		// Different types of diagonal wins (se to nw)
		assertHasWinner(Disc.RED, "BoardHelperTest_hasWinner4a.txt");
		assertHasWinner(null, "BoardHelperTest_hasWinner4b.txt");
	}

	/**
	 * Test winners with last move optimisation
	 */
	@Test
	public void testHasWinner3() throws IOException {
		// Different types of vertical wins
		assertHasWinner(Disc.RED, 6, 5, "BoardHelperTest_hasWinner1.txt");
		assertHasWinner(Disc.RED, 6, 4, "BoardHelperTest_hasWinner1a.txt");
		assertHasWinner(Disc.RED, 6, 3, "BoardHelperTest_hasWinner1b.txt");
		assertHasWinner(Disc.YELLOW, 6, 4, "BoardHelperTest_hasWinner1c.txt");
		assertHasWinner(Disc.RED, 6, 5, "BoardHelperTest_hasWinner1d.txt");
		assertHasWinner(Disc.YELLOW, 2, 5, "BoardHelperTest_hasWinner1e.txt");

		// Different types of horizontal wins
		assertHasWinner(Disc.RED, 0, 1, "BoardHelperTest_hasWinner2a.txt");
		assertHasWinner(Disc.YELLOW, 4, 0, "BoardHelperTest_hasWinner2b.txt");
		assertHasWinner(Disc.RED, 5, 1, "BoardHelperTest_hasWinner2c.txt");
		assertHasWinner(Disc.YELLOW, 5, 5, "BoardHelperTest_hasWinner2d.txt");
		assertHasWinner(Disc.YELLOW, 6, 5, "BoardHelperTest_hasWinner2d.txt");
		assertHasWinner(Disc.RED, 3, 4, "BoardHelperTest_hasWinner2e.txt");

		// Different types of diagonal wins (sw to ne)
		assertHasWinner(Disc.RED, 1, 1, "BoardHelperTest_hasWinner3a.txt");
		assertHasWinner(Disc.RED, 1, 1, "BoardHelperTest_hasWinner3b.txt");
		assertHasWinner(Disc.YELLOW, 6, 5, "BoardHelperTest_hasWinner3c.txt");
		assertHasWinner(Disc.YELLOW, 0, 2, "BoardHelperTest_hasWinner3d.txt");
		assertHasWinner(Disc.YELLOW, 0, 2, "BoardHelperTest_hasWinner3d.txt");
		assertHasWinner(Disc.YELLOW, 1, 3, "BoardHelperTest_hasWinner3d.txt");
		assertHasWinner(Disc.YELLOW, 3, 5, "BoardHelperTest_hasWinner3d.txt");

		// Different types of diagonal wins (se to nw)
		assertHasWinner(Disc.RED, 3, 3, "BoardHelperTest_hasWinner4a.txt");
		assertNoWinner(Disc.RED, 2, 0, "BoardHelperTest_hasWinner4b.txt");
	}

	private static void assertHasWinner(final Disc expectedWinner, final String inputFile)
			throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + inputFile));
		if (expectedWinner == null) {
			Assert.assertNull(BoardHelper.hasWinner(board));
		} else {
			Assert.assertEquals(expectedWinner, BoardHelper.hasWinner(board));
		}
	}

	private static void assertHasWinner(final Disc expectedWinner, final int lastMoveCol,
			final int lastMoveRow, final String inputFile) throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + inputFile));
		Assert.assertEquals(expectedWinner,
				BoardHelper.hasWinner(board, new Move(expectedWinner, lastMoveCol, lastMoveRow)));
	}

	private static void assertNoWinner(final Disc lastMoveDisc, final int lastMoveCol,
			final int lastMoveRow, final String inputFile) throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + inputFile));
		Assert.assertNull(
				BoardHelper.hasWinner(board, new Move(lastMoveDisc, lastMoveCol, lastMoveRow)));
	}
}
