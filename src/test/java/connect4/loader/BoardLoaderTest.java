package connect4.loader;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import connect4.Board;
import junit.framework.Assert;

public class BoardLoaderTest {

	@Test
	public void testBoardLoader1() throws IOException {
		String inputBoardString = FileUtils
				.readFileToString(new File("src/test/resources/BoardLoaderTest_1.txt"));
		final Board board = BoardLoader.readBoard(inputBoardString);
		inputBoardString = inputBoardString.substring(inputBoardString.indexOf('\n')).trim();
		Assert.assertEquals(inputBoardString, board.toString().trim());
	}

	@Test(expected = InvalidBoardFormatException.class)
	public void testBadBoardLoader1() throws IOException {
		// No board
		BoardLoader.readBoard(FileUtils
				.readFileToString(new File("src/test/resources/BoardLoaderTest_Bad1.txt")));

	}

	@Test(expected = InvalidBoardFormatException.class)
	public void testBadBoardLoader2() throws IOException {
		// nRows in first line bad
		BoardLoader.readBoard(FileUtils
				.readFileToString(new File("src/test/resources/BoardLoaderTest_Bad2.txt")));

	}

	@Test(expected = InvalidBoardFormatException.class)
	public void testBadBoardLoader3() throws IOException {
		// Missing a row
		BoardLoader.readBoard(FileUtils
				.readFileToString(new File("src/test/resources/BoardLoaderTest_Bad3.txt")));

	}

	@Test(expected = InvalidBoardFormatException.class)
	public void testBadBoardLoader4() throws IOException {
		// Unplayable
		BoardLoader.readBoard(FileUtils
				.readFileToString(new File("src/test/resources/BoardLoaderTest_Bad4.txt")));

	}
}
