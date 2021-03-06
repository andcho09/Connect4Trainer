package connect4;

import static connect4.BoardHelperTest.RESOURCES_DIR;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.IllegalMoveException;
import connect4.loader.BoardLoader;

public class BoardTest {

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalBoardSize1() {
		new Board(0, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalBoardSize2() {
		new Board(1, 17);
	}

	@Test
	public void testMaxBoard() {
		new Board(7, 16);
	}

	@Test
	public void testEmptyBoard() {
		final Board board = new Board(7, 6);
		Assert.assertEquals(".......\n.......\n.......\n.......\n.......\n.......\n", board.toString());
		Assert.assertNull(board.getDisc(0, 0));
		Assert.assertNull(board.getDisc(6, 0));
		Assert.assertNull(board.getDisc(0, 5));
		Assert.assertFalse(board.isFull());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetIllegalDisc1() {
		new Board(7, 6).getDisc(-1, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetIllegalDisc2() {
		new Board(7, 6).getDisc(0, -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetIllegalDisc3() {
		new Board(7, 6).getDisc(7, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetIllegalDisc4() {
		new Board(7, 6).getDisc(0, 6);
	}

	@Test
	public void testPutDisk1() throws IllegalMoveException {
		final Board board = new Board(7, 6);

		// Place red disk in col 0
		Assert.assertEquals(0, board.putDisc(0, Disc.RED));
		Assert.assertEquals(Disc.RED, board.getDisc(0, 0));
		Assert.assertEquals(null, board.getDisc(0, 1));
		Assert.assertEquals(null, board.getDisc(1, 0));
		Assert.assertEquals(".......\n.......\n.......\n.......\n.......\nr......\n", board.toString());

		// Place yellow disk in col 6
		Assert.assertEquals(0, board.putDisc(6, Disc.YELLOW));
		Assert.assertEquals(Disc.YELLOW, board.getDisc(6, 0));
		Assert.assertEquals(null, board.getDisc(6, 1));
		Assert.assertEquals(null, board.getDisc(5, 0));

		Assert.assertEquals(Disc.RED, board.getDisc(0, 0)); // Check red still there
		Assert.assertEquals(null, board.getDisc(0, 1));
		Assert.assertEquals(null, board.getDisc(1, 0));
		Assert.assertEquals(".......\n.......\n.......\n.......\n.......\nr.....y\n", board.toString());

		// Place red disk in col 6
		Assert.assertEquals(1, board.putDisc(6, Disc.RED));
		Assert.assertEquals(Disc.RED, board.getDisc(6, 1));
		Assert.assertEquals(null, board.getDisc(6, 2));
		Assert.assertEquals(null, board.getDisc(5, 1));

		Assert.assertEquals(Disc.YELLOW, board.getDisc(6, 0)); // Check yellow still there
		Assert.assertEquals(null, board.getDisc(5, 0));

		Assert.assertEquals(Disc.RED, board.getDisc(0, 0)); // Check first red still there
		Assert.assertEquals(null, board.getDisc(0, 1));
		Assert.assertEquals(null, board.getDisc(1, 0));
		Assert.assertEquals(".......\n.......\n.......\n.......\n......r\nr.....y\n", board.toString());

		// Place red disk in col 6
		Assert.assertEquals(2, board.putDisc(6, Disc.RED));
		Assert.assertEquals(Disc.RED, board.getDisc(6, 2));
		Assert.assertEquals(Disc.RED, board.getDisc(6, 1));
		Assert.assertEquals(Disc.YELLOW, board.getDisc(6, 0));
		Assert.assertEquals(null, board.getDisc(6, 3));
		Assert.assertEquals(".......\n.......\n.......\n......r\n......r\nr.....y\n", board.toString());
	}

	@Test(expected = IllegalMoveException.class)
	public void testPutBadDisk1() throws IllegalMoveException {
		final Board board = new Board(7, 3);
		Assert.assertEquals(0, board.putDisc(3, Disc.RED));
		Assert.assertEquals(1, board.putDisc(3, Disc.RED));
		Assert.assertEquals(2, board.putDisc(3, Disc.RED));
		Assert.assertEquals("...r...\n...r...\n...r...\n", board.toString());
		board.putDisc(3, Disc.RED);
	}

	@Test(expected = IllegalMoveException.class)
	public void testPutBadDisk2() throws IllegalMoveException {
		final Board board = new Board(7, 6);
		board.putDisc(7, Disc.RED);
	}

	@Test(expected = IllegalMoveException.class)
	public void testPutBadDisk3() throws IllegalMoveException {
		final Board board = new Board(7, 6);
		board.putDisc(-1, Disc.RED);
	}

	@Test
	public void testIsFull() throws IOException {
		Assert.assertFalse(BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_isFull1.txt")).isFull());
		Assert.assertTrue(BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_isFull2.txt")).isFull());
		Assert.assertFalse(BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_isFull3.txt")).isFull());
	}

	@Test
	public void testHashCodeNormalised() throws Exception {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_reverse3_input.txt"));
		Assert.assertEquals(board.hashCodeNormalised(), board.normalise().hashCode());
	}

	@Test
	public void testSwap() throws Exception {
		final Board input = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_swap_input.txt"));
		final Board output = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_swap_output.txt"));
		Assert.assertEquals(output, input.swap());
		Assert.assertEquals(input, input.swap().swap());
	}

	@Test
	public void testReverseToLeft() throws Exception {
		final Board expected = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_reverse1a_input.txt"));
		Board test = new Board(expected);
		Assert.assertEquals(false, test.reverseToLeft());
		Assert.assertEquals(expected, test);

		test = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_reverse1c_input.txt"));
		Assert.assertEquals(true, test.reverseToLeft());
		Assert.assertEquals(expected, test);
	}
}
