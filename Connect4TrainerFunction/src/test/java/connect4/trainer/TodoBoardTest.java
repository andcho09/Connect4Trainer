package connect4.trainer;

import static connect4.BoardHelperTest.RESOURCES_DIR;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import connect4.api.Board;
import connect4.api.BoardHelper;
import connect4.api.Disc;
import connect4.api.analysis.ColumnAnalysis;
import connect4.loader.BoardLoader;

/**
 * JUnit tests for boards under investigation.
 * https://bitbucket.org/andcho09/connect4trainer/issues/9/weird-ai-cases-that-need-to-be-checked
 */
public class TodoBoardTest {

	private Recommender trainer;

	@Before
	public void setup() {
		trainer = new Trainer();
	}

	@Test
	public void testTodo1() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TodoBoard_1.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));

		// TODO Low priority. Strictly speaking red can play column[1], column[2], or column[5] to block yellow's potential trap
		Assert.assertEquals(2, trainer.recommend(board, Disc.RED));
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0).hasCondition(ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE));

		Assert.assertEquals(2, trainer.recommend(board, Disc.YELLOW)); // yellow can only play column[2] to setup this trap
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0).hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));
	}

	@Test
	public void testTodo2() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TodoBoard_2.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));

		trainer.recommend(board, Disc.YELLOW);
		Assert.assertEquals(3, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBoardAnalysis().get(1).hasCondition(ColumnAnalysis.FLAG_FORCED_WIN));
		Assert.assertTrue(trainer.getLastBoardAnalysis().get(3).hasCondition(ColumnAnalysis.FLAG_FORCED_WIN));
		Assert.assertTrue(trainer.getLastBoardAnalysis().get(3).hasCondition(ColumnAnalysis.FLAG_MAKE_3_SETUP));
		Assert.assertTrue(trainer.getLastBoardAnalysis().get(3).hasCondition(ColumnAnalysis.FLAG_MAKE_3_DOUBLE_SETUP));
		Assert.assertTrue(trainer.getLastBoardAnalysis().get(4).hasCondition(ColumnAnalysis.FLAG_FORCED_WIN));

		// TODO red thinks it has three options, but they all rely on column[3]. Playing here should block everything.
		trainer.recommend(board, Disc.RED);
		Assert.assertEquals(3, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBoardAnalysis().get(1).hasCondition(ColumnAnalysis.FLAG_BLOCK_FORCED_WIN));
		Assert.assertTrue(trainer.getLastBoardAnalysis().get(3).hasCondition(ColumnAnalysis.FLAG_BLOCK_FORCED_WIN));
		Assert.assertTrue(trainer.getLastBoardAnalysis().get(3).hasCondition(ColumnAnalysis.FLAG_BLOCK_MAKE_3_SETUP));
		Assert.assertTrue(trainer.getLastBoardAnalysis().get(3).hasCondition(ColumnAnalysis.FLAG_BLOCK_MAKE_3_DOUBLE_SETUP));
		Assert.assertTrue(trainer.getLastBoardAnalysis().get(4).hasCondition(ColumnAnalysis.FLAG_BLOCK_FORCED_WIN));
	}

	@Test
	public void testTodo3() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TodoBoard_3.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));

		Assert.assertEquals(5, trainer.recommend(board, Disc.YELLOW)); // yellow wins
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0).hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));

		// TODO red should block by playing column[6] but doesn't :(
		Assert.assertEquals(2, trainer.recommend(board, Disc.RED));
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0).hasCondition(ColumnAnalysis.FLAG_BLOCK_MAKE_3_SETUP));
	}
}
