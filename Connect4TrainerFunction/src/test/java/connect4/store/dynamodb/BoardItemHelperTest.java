package connect4.store.dynamodb;

import static connect4.BoardHelperTest.RESOURCES_DIR;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.analysis.BoardAnalysis;
import connect4.api.analysis.ColumnAnalysis;
import connect4.loader.BoardLoader;

public class BoardItemHelperTest {

	@Test
	public void testConvertDisc() {
		Assert.assertEquals("r", BoardItemHelper.DISC_CONVERTER.convert(Disc.RED));
		Assert.assertEquals(Disc.YELLOW, BoardItemHelper.DISC_CONVERTER.unconvert("y"));
	}

	@Test
	public void testConvertBoard() throws IOException {
		final String boardJson = FileUtils.readFileToString(new File(RESOURCES_DIR + "Convert_Board_1.json"), "UTF-8");
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "Convert_Board_1.txt"));
		Assert.assertEquals(boardJson, BoardItemHelper.BOARD_CONVERTER.convert(board));
		Assert.assertEquals(board, BoardItemHelper.BOARD_CONVERTER.unconvert(boardJson));
	}

	@Test
	public void testConvertBoardAnalysis() throws IOException {
		final String boardAnalysisJson = FileUtils.readFileToString(new File(RESOURCES_DIR + "Convert_BoardAnalysis_1.json"), "UTF-8");

		final BoardAnalysis boardAnalysis = new BoardAnalysis();
		ColumnAnalysis columnAnalysis = new ColumnAnalysis(1);
		columnAnalysis.addCondition(ColumnAnalysis.FLAG_BLOCK_MAKE_3_SETUP);
		boardAnalysis.add(columnAnalysis);
		columnAnalysis = new ColumnAnalysis(3);
		columnAnalysis.addCondition(ColumnAnalysis.FLAG_BOTTOM_CENTER_FREE);
		columnAnalysis.addCondition(ColumnAnalysis.FLAG_FORCED_WIN);
		boardAnalysis.add(columnAnalysis);
		columnAnalysis = new ColumnAnalysis(4);
		columnAnalysis.addCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE);
		columnAnalysis.addCondition(ColumnAnalysis.FLAG_BLOCK_LOSS_1);
		boardAnalysis.add(columnAnalysis);

		Assert.assertEquals(boardAnalysisJson, BoardItemHelper.BOARD_ANALYSIS_CONVERTER.convert(boardAnalysis));

		boardAnalysis.add(0, new ColumnAnalysis(0));
		boardAnalysis.add(2, new ColumnAnalysis(2));
		boardAnalysis.add(5, new ColumnAnalysis(5));
		boardAnalysis.add(6, new ColumnAnalysis(6));
		Assert.assertEquals(boardAnalysis, BoardItemHelper.BOARD_ANALYSIS_CONVERTER.unconvert(boardAnalysisJson));
	}
}
