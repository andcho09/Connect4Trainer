package connect4;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import connect4.loader.BoardLoader;

public class BoardHelperTest {

	public final static String RESOURCES_DIR = "src/test/resources/";

	@Test
	public void testHasWinner1() throws IOException {
		assertHasWinner(null, "BoardLoaderTest_1.txt");
	}

	@Test
	public void testHasWinner2() throws IOException {
		// Different types of vertical wins
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
	}

	private static void assertHasWinner(final Disc expectedWinner, final String inputFile)
			throws IOException {
		final String inputBoardString = FileUtils
				.readFileToString(new File(RESOURCES_DIR + inputFile));
		final Board board = BoardLoader.readBoard(inputBoardString);
		if (expectedWinner == null) {
			Assert.assertNull(BoardHelper.hasWinner(board));
		} else {
			Assert.assertEquals(expectedWinner, BoardHelper.hasWinner(board));
		}
	}
}
