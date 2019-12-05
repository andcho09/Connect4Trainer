package connect4.rest;

import static connect4.BoardHelperTest.RESOURCES_DIR;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.IllegalMoveException;
import connect4.api.analysis.BoardAnalysis;
import connect4.api.analysis.ColumnAnalysis;
import connect4.api.json.GetRandomBoardRequest;
import connect4.api.json.StoreBoardRequest;
import connect4.loader.BoardLoader;
import connect4.web.GameState;
import connect4.web.PlayRequest;
import connect4.web.PlayResponse;
import connect4.web.RecommendRequest;
import connect4.web.RecommendResponse;

public class JsonStreamingObjectFactoryTest {

	private static WebJsonStreamingObjectFactory INSTANCE = WebJsonStreamingObjectFactory.getInstance();

	@Test
	public void testDeserialiseRecommendRequest() throws IOException {
		final String json = FileUtils.readFileToString(new File(RESOURCES_DIR + "Rest_Recommend_Req_1.json"), "UTF-8");
		final RecommendRequest request = INSTANCE.deserializeRecommendRequest(INSTANCE.getParser(json));

		Assert.assertEquals(BoardLoader.readBoard(new File(RESOURCES_DIR + "Rest_Recommend_Req_1_board.txt")), request.getBoard());
		Assert.assertEquals(Disc.YELLOW, request.getCurrentPlayer());
	}

	@Test
	public void testSerialiseRecommendResponse() throws IOException {
		final RecommendResponse response = new RecommendResponse();
		response.setBoard(BoardLoader.readBoard(new File(RESOURCES_DIR + "Rest_Recommend_Req_1_board.txt")));
		response.setRecommendColumn(1);
		response.setRecommendRow(0);
		response.setState(GameState.PLAYER_Y_TURN);

		final StringWriter writer = new StringWriter();
		final JsonGenerator generator = INSTANCE.getGenerator(writer);
		INSTANCE.serialize(generator, response);
		generator.close();
		Assert.assertEquals(FileUtils.readFileToString(new File(RESOURCES_DIR + "Rest_Recommend_Res_1.json"), "UTF-8"), writer.toString());
	}

	@Test
	public void testDeserialisePlayRequest() throws IOException {
		final String json = FileUtils.readFileToString(new File(RESOURCES_DIR + "Rest_Play_Req_1.json"), "UTF-8");
		final PlayRequest request = INSTANCE.deserializePlayRequest(INSTANCE.getParser(json));

		Assert.assertEquals(BoardLoader.readBoard(new File(RESOURCES_DIR + "Rest_Recommend_Req_1_board.txt")), request.getBoard());
		Assert.assertEquals(Disc.YELLOW, request.getCurrentPlayer());
		Assert.assertEquals((Integer) 0, request.getColumn());
	}

	@Test
	public void testSerialisePlayResponse() throws IOException, IllegalMoveException {
		final PlayResponse response = new PlayResponse();
		final Board playerBoard = BoardLoader.readBoard(new File(RESOURCES_DIR + "Rest_Recommend_Req_1_board.txt"));
		response.setPlayerRow(playerBoard.putDisc(0, Disc.YELLOW));
		response.setPlayerBoard(playerBoard);

		final Board aiBoard = new Board(playerBoard);
		response.setAiCol(0);
		response.setAiRow(aiBoard.putDisc(0, Disc.RED));
		response.setAiBoard(aiBoard);
		response.setState(GameState.PLAYER_R_TURN);

		final StringWriter writer = new StringWriter();
		final JsonGenerator generator = INSTANCE.getGenerator(writer);
		INSTANCE.serialize(generator, response);
		generator.close();
		writer.flush();
		Assert.assertEquals(FileUtils.readFileToString(new File(RESOURCES_DIR + "Rest_Play_Res_1.json"), "UTF-8"), writer.toString());
	}

	@Test
	public void testDeserialiseStoreRequest() throws IOException {
		final String json = FileUtils.readFileToString(new File(RESOURCES_DIR + "Rest_Store_Req_1.json"), "UTF-8");
		final StoreBoardRequest request = INSTANCE.deserializeStoreRequest(INSTANCE.getParser(json));
		Assert.assertEquals(Disc.RED, request.getCurrentPlayer());
		Assert.assertEquals(BoardLoader.readBoard(new File(RESOURCES_DIR + "Rest_Recommend_Req_1_board.txt")), request.getBoard());
		final BoardAnalysis boardAnalysis = request.getBoardAnalysis();
		Assert.assertEquals(7, boardAnalysis.size());

		Assert.assertEquals(ColumnAnalysis.FLAG_NO_OPINION, boardAnalysis.getAnalysisAtColumn(0).getFlags());
		Assert.assertEquals(ColumnAnalysis.FLAG_BLOCK_MAKE_3_SETUP, boardAnalysis.getAnalysisAtColumn(1).getFlags());
		Assert.assertEquals(ColumnAnalysis.FLAG_NO_OPINION, boardAnalysis.getAnalysisAtColumn(2).getFlags());
		Assert.assertEquals(ColumnAnalysis.FLAG_BOTTOM_CENTER_FREE | ColumnAnalysis.FLAG_FORCED_WIN,
				boardAnalysis.getAnalysisAtColumn(3).getFlags());
		Assert.assertEquals(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE | ColumnAnalysis.FLAG_BLOCK_LOSS_1,
				boardAnalysis.getAnalysisAtColumn(4).getFlags());
		Assert.assertEquals(ColumnAnalysis.FLAG_NO_OPINION, boardAnalysis.getAnalysisAtColumn(5).getFlags());
		Assert.assertEquals(ColumnAnalysis.FLAG_NO_OPINION, boardAnalysis.getAnalysisAtColumn(6).getFlags());
	}

	@Test
	public void testSerialiseStoreBoardRequest() throws IOException {
		final StoreBoardRequest request = new StoreBoardRequest();
		request.setCurrentPlayer(Disc.RED);
		request.setBoard(BoardLoader.readBoard(new File(RESOURCES_DIR + "Rest_Recommend_Req_1_board.txt")));

		final BoardAnalysis boardAnalysis = new BoardAnalysis();
		request.setBoardAnalysis(boardAnalysis);

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

		final StringWriter writer = new StringWriter();
		final JsonGenerator generator = INSTANCE.getGenerator(writer);
		INSTANCE.serialize(generator, request);
		generator.close();
		writer.flush();
		Assert.assertEquals(FileUtils.readFileToString(new File(RESOURCES_DIR + "Rest_Store_Req_1.json"), "UTF-8"), writer.toString());
	}

	@Test
	public void testDeserialiseGenericRequest() throws IOException {
		String json = FileUtils.readFileToString(new File(RESOURCES_DIR + "Rest_Play_Req_1.json"), "UTF-8");
		Assert.assertEquals(PlayRequest.class, INSTANCE.deserialiseGenericRequest(INSTANCE.getParser(json)).getClass());

		json = FileUtils.readFileToString(new File(RESOURCES_DIR + "Rest_Recommend_Req_1.json"), "UTF-8");
		Assert.assertEquals(RecommendRequest.class, INSTANCE.deserialiseGenericRequest(INSTANCE.getParser(json)).getClass());
	}

	@Test
	public void testDeserialiseAbstractBoardRequest() throws IOException {
		String json = FileUtils.readFileToString(new File(RESOURCES_DIR + "Rest_Store_Req_1.json"), "UTF-8");
		Assert.assertEquals(StoreBoardRequest.class, INSTANCE.deserializeAbstractBoardRequest(INSTANCE.getParser(json)).getClass());
		json = FileUtils.readFileToString(new File(RESOURCES_DIR + "Rest_GetRandom_Req_1.json"), "UTF-8");
		Assert.assertEquals(GetRandomBoardRequest.class, INSTANCE.deserializeAbstractBoardRequest(INSTANCE.getParser(json)).getClass());
	}

	@Test
	public void testDeserialiseUnknownGenericRequest() {
		try {
			INSTANCE.deserialiseGenericRequest(INSTANCE.getParser("{\"bad\": \"json request\"}"));
		} catch (final JsonParseException e) {
			e.printStackTrace();
			Assert.fail("Didn't expect to fail with JsonParseException ");
		} catch (final IOException expected) {
			Assert.assertEquals(
					"Could not determine request type. Does not contain an 'action' key. Current parsing location is: [Source: (String)\"{\"bad\": \"json request\"}\"; line: 1, column: 23]",
					expected.getMessage());
		}
	}
}
