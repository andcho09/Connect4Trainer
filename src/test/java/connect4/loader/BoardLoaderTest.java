package connect4.loader;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import connect4.Board;
import connect4.Disc;

public class BoardLoaderTest {

	@Test
	public void testBoardLoader1() throws IOException {
		String inputBoardString = FileUtils.readFileToString(new File("src/test/resources/BoardLoaderTest_1.txt"), "UTF-8");
		final Board board = BoardLoader.readBoard(inputBoardString);
		inputBoardString = inputBoardString.substring(inputBoardString.indexOf('\n')).trim().replace("\r\n", "\n");
		Assert.assertEquals(inputBoardString, board.toString().trim());
	}

	@Test(expected = InvalidBoardFormatException.class)
	public void testBadBoardLoader1() throws IOException {
		// No board
		BoardLoader.readBoard(FileUtils.readFileToString(new File("src/test/resources/BoardLoaderTest_Bad1.txt"), "UTF-8"));

	}

	@Test(expected = InvalidBoardFormatException.class)
	public void testBadBoardLoader2() throws IOException {
		// nRows in first line bad
		BoardLoader.readBoard(FileUtils.readFileToString(new File("src/test/resources/BoardLoaderTest_Bad2.txt"), "UTF-8"));

	}

	@Test(expected = InvalidBoardFormatException.class)
	public void testBadBoardLoader3() throws IOException {
		// Missing a row
		BoardLoader.readBoard(FileUtils.readFileToString(new File("src/test/resources/BoardLoaderTest_Bad3.txt"), "UTF-8"));

	}

	@Test(expected = InvalidBoardFormatException.class)
	public void testBadBoardLoader4() throws IOException {
		// Unplayable
		BoardLoader.readBoard(FileUtils.readFileToString(new File("src/test/resources/BoardLoaderTest_Bad4.txt"), "UTF-8"));
	}

	@Test
	public void testBoardLoaderJson1() throws IOException {
		String inputBoardString = FileUtils.readFileToString(new File("src/test/resources/BoardLoaderTest_Json_1.txt"), "UTF-8");
		final Board board = BoardLoader.readBoard(inputBoardString);
		Assert.assertEquals(7, board.getNumCols());
		Assert.assertEquals(6, board.getNumRows());
		Assert.assertEquals(Disc.RED, board.getDisc(0, 1));
		Assert.assertEquals(Disc.RED, board.getDisc(1, 4));
		Assert.assertEquals(Disc.YELLOW, board.getDisc(2, 3));
		Assert.assertEquals(Disc.YELLOW, board.getDisc(3, 4));
		Assert.assertEquals(Disc.RED, board.getDisc(4, 1));
		Assert.assertEquals(Disc.RED, board.getDisc(5, 3));
		Assert.assertEquals(Disc.YELLOW, board.getDisc(6, 1));
	}
}
