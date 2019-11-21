package connect4;

import static connect4.BoardHelperTest.RESOURCES_DIR;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import connect4.loader.BoardLoader;

// Test normalising boards
public class BoardNormaliseTest {

	@Test
	public void testNormaliseAllCombos() throws Exception {
		final Board expected = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_reverse1a_input.txt"));
		Assert.assertEquals(expected, expected.normalise());
		Assert.assertEquals(expected, BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_reverse1b_input.txt")).normalise());
		Assert.assertEquals(expected, BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_reverse1c_input.txt")).normalise());
		Assert.assertEquals(expected, BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_reverse1d_input.txt")).normalise());
	}

	@Test
	public void testNormaliseSymmetrical() throws Exception {
		// Symmetrical
		final Board expected = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_reverse2_input.txt"));
		Assert.assertEquals(expected, expected.normalise());
		// Symmetrical but needs to swap
		Assert.assertEquals(expected, BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_reverse2a_input.txt")).normalise());
	}

	@Test
	public void testNormaliseReverseAndSwap() throws Exception {
		// Reverse and swap required
		final Board input = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_reverse3_input.txt"));
		final Board output = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_reverse3_output.txt"));
		Assert.assertEquals(output, input.normalise());
	}

	@Test
	public void testNormalise1a() throws Exception {
		// Already normalised
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardNormaliseTest_1a.txt"));
		Assert.assertEquals(board, board.normalise());
	}

	@Test
	public void testNormalise1b() throws Exception {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardNormaliseTest_1b.txt"));
		final Board expected = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardNormaliseTest_1a.txt"));
		Assert.assertEquals(expected, board.normalise());
	}

	@Test
	public void testNormalise1c() throws Exception {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardNormaliseTest_1c.txt"));
		final Board expected = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardNormaliseTest_1a.txt"));
		Assert.assertEquals(expected, board.normalise());
	}

	@Test
	public void testNormalise1d() throws Exception {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardNormaliseTest_1d.txt"));
		final Board expected = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardNormaliseTest_1a.txt"));
		Assert.assertEquals(expected, board.normalise());
	}
}
