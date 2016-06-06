package connect4.trainer;

import static connect4.BoardHelperTest.RESOURCES_DIR;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import connect4.Board;
import connect4.Disc;
import connect4.loader.BoardLoader;

public class TrainerTest {

	@Test
	public void testEmpty() {
		final Board board = new Board(7, 6);
		final Trainer trainer = new Trainer();
		final int recommendedColumn = trainer.analyse(board, Disc.RED);
		Assert.assertTrue(recommendedColumn >= 0);
		Assert.assertTrue(recommendedColumn < board.getNumCols());
		// If center was preferred, we'd get deterministic result
	}

	@Test
	public void testWin1Move() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_1.txt"));
		final Trainer trainer = new Trainer();
		Assert.assertEquals(1, trainer.analyse(board, Disc.RED));
		Assert.assertEquals(1, trainer.analyse(board, Disc.YELLOW));
	}
}
