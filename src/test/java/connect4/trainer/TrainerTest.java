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
		Assert.assertEquals(3, trainer.recommend(board, Disc.RED));
		final BoardAnalysis lastBestBoardAnalysis = trainer.getLastBestBoardAnalysis();
		Assert.assertEquals(1, lastBestBoardAnalysis.size());
		Assert.assertTrue(lastBestBoardAnalysis.getAnalysisAtColumn(3).hasCondition(ColumnAnalysis.FLAG_BOTTOM_CENTER_FREE));
	}

	@Test
	public void testWin1Move() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_1.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));

		Assert.assertEquals(1, trainer.recommend(board, Disc.RED)); // red blocks yellow win
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0).hasCondition(ColumnAnalysis.FLAG_BLOCK_LOSS_1));

		Assert.assertEquals(1, trainer.recommend(board, Disc.YELLOW)); // yellow wins
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0).hasCondition(ColumnAnalysis.FLAG_WIN_1));
	}

	@Test
	public void testWin1Move2() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_2.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		trainer.recommend(board, Disc.RED);
		Assert.assertEquals(2, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(4).hasCondition(ColumnAnalysis.FLAG_BLOCK_LOSS_1));
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(6).hasCondition(ColumnAnalysis.FLAG_BLOCK_LOSS_1));
	}

	@Test
	public void testEnableOpponentWin() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_OppWin_1.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));

		// playing 3 would allow yellow to win by playing ontop of our move
		Assert.assertEquals(2, trainer.recommend(board, Disc.RED));
		Assert.assertTrue(trainer.getLastBoardAnalysis().get(3).hasCondition(ColumnAnalysis.FLAG_ENABLE_OPPONENT_WIN));
	}

	@Test
	public void testEnableTrapWin() throws IOException {
		Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_EnableTrapWin_1.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		int column = trainer.recommend(board, Disc.YELLOW);
		Assert.assertTrue(column == 1 || column == 4);
		Assert.assertEquals(2, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0).hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(1).hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));

		board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_EnableTrapWin_2.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		Assert.assertEquals(3, trainer.recommend(board, Disc.YELLOW));
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0).hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));

		board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_EnableTrapWin_3.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		column = trainer.recommend(board, Disc.YELLOW);
		Assert.assertTrue(3 == column || 4 == column);
		Assert.assertEquals(2, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0).hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(1).hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));

		Assert.assertEquals(2, trainer.recommend(board, Disc.RED));
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0).hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));

		board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_EnableTrapWin_4.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		column = trainer.recommend(board, Disc.YELLOW);
		Assert.assertTrue(column == 5);
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0).hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));

	}

	@Test
	public void testBlockTrapWin() throws IOException {
		Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_EnableTrapWin_1.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		final int column = trainer.recommend(board, Disc.RED);
		Assert.assertTrue(column == 1 || column == 4);
		Assert.assertEquals(2, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0).hasCondition(ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE));
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(1).hasCondition(ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE));

		board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_EnableTrapWin_2.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		Assert.assertEquals(3, trainer.recommend(board, Disc.RED));
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0).hasCondition(ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE));
	}

	@Test
	public void testEnableMultiTrapWin1() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_EnableMultiTrapWin_1.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		final int column = trainer.recommend(board, Disc.YELLOW);
		Assert.assertTrue(column == 0 || column == 3 || column == 6); // this test is terrible
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		// Assert.assertTrue(trainer.getLastBestColumnAnalysis().get(0)
		// .hasCondition(ColumnAnalysis.FLAG_BLOCK_TRAP_MORE_THAN_ONE));
	}

	@Test
	public void testForceTrapWin1() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_ForceWin_1.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		Assert.assertEquals(3, trainer.recommend(board, Disc.YELLOW));
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0).hasCondition(ColumnAnalysis.FLAG_FORCED_WIN));
	}

	@Test
	public void testForceTrapWin2() throws IOException {
		// No opinion. This is testing a bug
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_ForceWin_2.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		Assert.assertEquals(3, trainer.recommend(board, Disc.YELLOW));
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertEquals(ColumnAnalysis.FLAG_BOTTOM_CENTER_FREE, trainer.getLastBestBoardAnalysis().get(0).getFlags());
	}

	@Test
	public void testForceTrapWin3() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_ForceWin_3.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		trainer.recommend(board, Disc.YELLOW);
		Assert.assertEquals(3, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(1).hasCondition(ColumnAnalysis.FLAG_FORCED_WIN));
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(2).hasCondition(ColumnAnalysis.FLAG_FORCED_WIN));
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(5).hasCondition(ColumnAnalysis.FLAG_FORCED_WIN));
	}

	@Test
	public void testForceTrapWin4() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_ForceWin_4.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		Assert.assertEquals(0, trainer.recommend(board, Disc.YELLOW));
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(0).hasCondition(ColumnAnalysis.FLAG_FORCED_WIN));
	}

	@Test
	public void testForceTrapWin5() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_ForceWin_5.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		trainer.recommend(board, Disc.YELLOW);
		Assert.assertEquals(2, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0).hasCondition(ColumnAnalysis.FLAG_FORCED_WIN));
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(1).hasCondition(ColumnAnalysis.FLAG_FORCED_WIN));
		Assert.assertEquals(0, trainer.getLastBestBoardAnalysis().get(0).getColumn());
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().get(1).getColumn());
	}

	@Test
	public void testForceTrapWin6() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_ForceWin_6.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		trainer.recommend(board, Disc.YELLOW);
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(3).hasCondition(ColumnAnalysis.FLAG_FORCED_WIN));
	}

	// @Test
	public void testForceTrapWin7() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_ForceWin_7.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		trainer.recommend(board, Disc.YELLOW);
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		// Yellow should play 3 so next move it can play 6 which executes a trap
		// Red could block above by playing 3, 6
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(3).hasCondition(ColumnAnalysis.FLAG_FORCED_WIN));
	}

	// @Test
	// This test fails. It's potentially not a block of a forced play since yellow needs two moves
	// to set the trap
	public void testBlockForceTrapWin1() throws IOException {
		// No opinion. This is testing a bug
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_BlockForceWin_1.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		trainer.recommend(board, Disc.RED);
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().get(0));
	}

	@Test
	public void testBlockForceTrapWin2() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_ForceWin_4.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		trainer.recommend(board, Disc.RED);
		Assert.assertEquals(2, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(0).hasCondition(ColumnAnalysis.FLAG_BLOCK_FORCED_WIN));
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(2).hasCondition(ColumnAnalysis.FLAG_BLOCK_FORCED_WIN));
	}

	@Test
	public void testBlockForceTrapWin3() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_ForceWin_3.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		trainer.recommend(board, Disc.RED);
		Assert.assertEquals(3, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(1).hasCondition(ColumnAnalysis.FLAG_BLOCK_FORCED_WIN));
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(2).hasCondition(ColumnAnalysis.FLAG_BLOCK_FORCED_WIN));
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(4).hasCondition(ColumnAnalysis.FLAG_BLOCK_FORCED_WIN));
		// Column 5 is not here because it enables an opponent win
	}

	@Test
	public void testBlockForceTrapWin4() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_ForceWin_4.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		trainer.recommend(board, Disc.RED);
		Assert.assertEquals(2, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(0).hasCondition(ColumnAnalysis.FLAG_BLOCK_FORCED_WIN));
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(2).hasCondition(ColumnAnalysis.FLAG_BLOCK_FORCED_WIN));
	}

	@Test
	public void testForceTrapError1() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_ForceError_1.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		trainer.recommend(board, Disc.YELLOW);
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(2).hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));
	}

	@Test
	public void testBlockMake31() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_Make3_1.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		trainer.recommend(board, Disc.YELLOW);
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0).hasCondition(ColumnAnalysis.FLAG_MAKE_3_SETUP));
	}

	@Test
	public void testBlockMake32() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_Make3_2.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		trainer.recommend(board, Disc.YELLOW);
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(4).hasCondition(ColumnAnalysis.FLAG_MAKE_3_SETUP));
	}

	@Test
	public void testBlockMake33() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_Make3_3.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		trainer.recommend(board, Disc.YELLOW);
		Assert.assertEquals(7, trainer.getLastBestBoardAnalysis().size());
	}

	@Test
	public void testBlockMake34() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_Make3_4.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		Assert.assertEquals(5, trainer.recommend(board, Disc.YELLOW));
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().get(0).hasCondition(ColumnAnalysis.FLAG_MAKE_3_SETUP));

		trainer.recommend(board, Disc.RED);
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBoardAnalysis().getAnalysisAtColumn(5).hasCondition(ColumnAnalysis.FLAG_BLOCK_MAKE_3_SETUP));
	}

	@Test
	public void testBlockMake3Double1() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_Make3Double_1.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		trainer.recommend(board, Disc.YELLOW);
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(5).hasCondition(ColumnAnalysis.FLAG_MAKE_3_DOUBLE_SETUP));

		trainer.recommend(board, Disc.RED);
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertEquals(5, trainer.getLastBestBoardAnalysis().get(0).getColumn());
		Assert.assertTrue(
				trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(5).hasCondition(ColumnAnalysis.FLAG_BLOCK_MAKE_3_DOUBLE_SETUP));
	}

	@Test
	public void testBlockMake3Double2() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_Make3Double_2.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		trainer.recommend(board, Disc.YELLOW);
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(6).hasCondition(ColumnAnalysis.FLAG_MAKE_3_DOUBLE_SETUP));

		trainer.recommend(board, Disc.RED);
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertTrue(
				trainer.getLastBestBoardAnalysis().getAnalysisAtColumn(6).hasCondition(ColumnAnalysis.FLAG_BLOCK_MAKE_3_DOUBLE_SETUP));
	}

	@Test
	public void testNoFreeWins1() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_NoFreeWins1.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		Assert.assertEquals(0, trainer.recommend(board, Disc.RED));
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
		Assert.assertEquals(0, trainer.getLastBestBoardAnalysis().get(0).getColumn());
		Assert.assertTrue(trainer.getLastBoardAnalysis().getAnalysisAtColumn(3).hasCondition(ColumnAnalysis.FLAG_ENABLE_OPPONENT_WIN));
	}

	@Test
	public void testNoFreeWins2() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_NoFreeWins2.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		Assert.assertEquals(4, trainer.recommend(board, Disc.RED));
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());

		// TODO this is a little weird that column 4 (blocks forced win) is chosen over column 5 (if we played here we prevent the double
		// setup)
		Assert.assertEquals(4, trainer.getLastBestBoardAnalysis().get(0).getColumn());
		Assert.assertTrue(trainer.getLastBoardAnalysis().getAnalysisAtColumn(4).hasCondition(ColumnAnalysis.FLAG_BLOCK_FORCED_WIN));
		Assert.assertTrue(
				trainer.getLastBoardAnalysis().getAnalysisAtColumn(5).hasCondition(ColumnAnalysis.FLAG_BLOCK_MAKE_3_DOUBLE_SETUP));
	}

	@Test
	public void testTakeBottomCenter() throws IOException {
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_BottomCenter_1.txt"));
		Assert.assertNull(BoardHelper.hasWinner(board));
		Assert.assertEquals(3, trainer.recommend(board, Disc.RED));
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());

		Assert.assertEquals(3, trainer.recommend(board, Disc.YELLOW));
		Assert.assertEquals(1, trainer.getLastBestBoardAnalysis().size());
	}

}
