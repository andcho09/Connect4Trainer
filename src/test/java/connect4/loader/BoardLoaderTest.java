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
		Board board = BoardLoader.readBoard(inputBoardString);

		int index = inputBoardString.indexOf('\n');
		inputBoardString = inputBoardString.substring(index).trim();
		Assert.assertEquals(inputBoardString, board.toString().trim());
	}
}
