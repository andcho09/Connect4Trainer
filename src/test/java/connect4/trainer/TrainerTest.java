package connect4.trainer;

import static connect4.BoardHelperTest.RESOURCES_DIR;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import connect4.Board;
import connect4.BoardHelper;
import connect4.Disc;
import connect4.loader.BoardLoader;

public class TrainerTest {

	private Recommender trainer;

	@Before
	public void setup() {
		trainer = new Trainer();
	}

	@Test
	public void testEmpty() {
		final Board board = new Board(7, 6);
		Assert.assertNull(BoardHelper.hasWinner(board));
		final int recommendedColumn = trainer.recommend(board, Disc.RED);
		Assert.assertTrue(recommendedColumn >= 0);
		Assert.assertTrue(recommendedColumn < board.getNumCols());
		final BoardAnalysis lastBestBoardAnalysis = trainer.getLastBestBoardAnalysis();
		Assert.assertEquals(7, lastBestBoardAnalysis.size());
		// If center was preferred, we'd get deterministic result
	}

	@Test
	public void testWin1Move() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_1.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));

		Assert.assertEquals(1, trainer.recommend(board, Disc.RED)); // red blocks yellow win
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0)
				.hasCondition(ColumnAnalysis.FLAG_BLOCK_LOSS_1));

		Assert.assertEquals(1, trainer.recommend(board, Disc.YELLOW)); // yellow wins
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(
				trainer.getLastBestBoardAnalysis().get(0).hasCondition(ColumnAnalysis.FLAG_WIN_1));
	}

	@Test
	public void testEnableOpponentWin() throws IOException {
		final Board board = BoardLoader
				.readBoard(new File(RESOURCES_DIR + "TrainerTest_OppWin_1.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));

		// playing 3 would allow yellow to win by playing ontop of our move
		Assert.assertEquals(2, trainer.recommend(board, Disc.RED));
		Assert.assertTrue(trainer.getLastBoardAnalysis().get(3)
				.hasCondition(ColumnAnalysis.FLAG_ENABLE_OPPONENT_WIN));
	}

	@Test
	public void testEnableTrapWin() throws IOException {
		Board board = BoardLoader
				.readBoard(new File(RESOURCES_DIR + "TrainerTest_EnableTrapWin_1.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		int column = trainer.recommend(board, Disc.YELLOW);
		Assert.assertTrue(column == 1 || column == 4);
		Assert.assertEquals(2, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0)
				.hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(1)
				.hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));

		board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_EnableTrapWin_2.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		Assert.assertEquals(3, trainer.recommend(board, Disc.YELLOW));
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0)
				.hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));

		board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_EnableTrapWin_3.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		column = trainer.recommend(board, Disc.YELLOW);
		Assert.assertTrue(3 == column || 4 == column);
		Assert.assertEquals(2, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0)
				.hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(1)
				.hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));

		Assert.assertEquals(2, trainer.recommend(board, Disc.RED));
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0)
				.hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));

		board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_EnableTrapWin_4.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		column = trainer.recommend(board, Disc.YELLOW);
		Assert.assertTrue(column == 5);
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0)
				.hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));

	}

	@Test
	public void testBlockTrapWin() throws IOException {
		Board board = BoardLoader
				.readBoard(new File(RESOURCES_DIR + "TrainerTest_EnableTrapWin_1.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		final int column = trainer.recommend(board, Disc.RED);
		Assert.assertTrue(column == 1 || column == 4);
		Assert.assertEquals(2, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0)
				.hasCondition(ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE));
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(1)
				.hasCondition(ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE));

		board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_EnableTrapWin_2.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		Assert.assertEquals(3, trainer.recommend(board, Disc.RED));
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0)
				.hasCondition(ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE));
	}

	@Test
	public void testEnableMultiTrapWin1() throws IOException {
		final Board board = BoardLoader
				.readBoard(new File(RESOURCES_DIR + "TrainerTest_EnableMultiTrapWin_1.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		final int column = trainer.recommend(board, Disc.YELLOW);
		Assert.assertTrue(column == 0 || column == 3 || column == 6); // this test is terrible
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		// Assert.assertTrue(trainer.getLastBestColumnAnalysis().get(0)
		// .hasCondition(ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE));
	}

	@Test
	public void testForceTrapWin1() throws IOException {
		final Board board = BoardLoader
				.readBoard(new File(RESOURCES_DIR + "TrainerTest_ForceWin_1.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		final int column = trainer.recommend(board, Disc.YELLOW);
		Assert.assertTrue(column == 3);
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0)
				.hasCondition(ColumnAnalysis.FLAG_FORCED_WIN));
	}

	@Test
	public void testForceTrapWin2() throws IOException {
		// No opinion. This is testing a bug
		final Board board = BoardLoader
				.readBoard(new File(RESOURCES_DIR + "TrainerTest_ForceWin_2.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		trainer.recommend(board, Disc.YELLOW);
		Assert.assertEquals(7, trainer.getLastBestBoardAnalysis().size());
		Assert.assertEquals(ColumnAnalysis.FLAG_NO_OPINION,
				trainer.getLastBestBoardAnalysis().get(0).getFlags());
	}
}
