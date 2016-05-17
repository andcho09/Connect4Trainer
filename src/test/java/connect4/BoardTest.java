package connect4;

import org.junit.Assert;
import org.junit.Test;

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
		Assert.assertEquals(".......\n.......\n.......\n.......\n.......\n.......\n",
				board.toString());
		Assert.assertEquals(Disc.EMPTY, board.getDisk(0, 0));
		Assert.assertEquals(Disc.EMPTY, board.getDisk(6, 0));
		Assert.assertEquals(Disc.EMPTY, board.getDisk(0, 5));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetIllegalDisc1() {
		new Board(7, 6).getDisk(-1, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetIllegalDisc2() {
		new Board(7, 6).getDisk(0, -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetIllegalDisc3() {
		new Board(7, 6).getDisk(7, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetIllegalDisc4() {
		new Board(7, 6).getDisk(0, 6);
	}
}
