package connect4.rest;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;

import connect4.Board;
import connect4.Disc;
import connect4.IllegalMoveException;
import connect4.loader.BoardLoader;
import connect4.web.GameState;
import connect4.web.PlayRequest;
import connect4.web.PlayResponse;
import connect4.web.RecommendRequest;
import connect4.web.RecommendResponse;

public class JsonStreamingObjectFactoryTest {

	private static JsonStreamingObjectFactory INSTANCE = JsonStreamingObjectFactory.getInstance();

	@Test
	public void testDeserialiseRecommendRequest() throws IOException {
		final String json = FileUtils.readFileToString(new File("src/test/resources/Rest_Recommend_Req_1.json"), "UTF-8");
		final RecommendRequest request = INSTANCE.deserializeRecommendRequest(INSTANCE.getParser(json));

		Assert.assertEquals(
				BoardLoader.readBoard(FileUtils.readFileToString(new File("src/test/resources/Rest_Recommend_Req_1_board.txt"), "UTF-8")),
				request.getBoard());
		Assert.assertEquals(Disc.YELLOW, request.getCurrentPlayer());
	}

	@Test
	public void testSerialiseRecommendResponse() throws IOException {
		final RecommendResponse response = new RecommendResponse();
		response.setBoard(
				BoardLoader.readBoard(FileUtils.readFileToString(new File("src/test/resources/Rest_Recommend_Req_1_board.txt"), "UTF-8")));
		response.setRecommendColumn(1);
		response.setRecommendRow(0);
		response.setState(GameState.PLAYER_Y_TURN);

		final StringWriter writer = new StringWriter();
		final JsonGenerator generator = INSTANCE.getGenerator(writer);
		INSTANCE.serialize(generator, response);
		generator.close();
		writer.flush();
		Assert.assertEquals(FileUtils.readFileToString(new File("src/test/resources/Rest_Recommend_Res_1.json"), "UTF-8"),
				writer.toString());
	}

	@Test
	public void testDeserialisPlayRequest() throws IOException {
		final String json = FileUtils.readFileToString(new File("src/test/resources/Rest_Play_Req_1.json"), "UTF-8");
		final PlayRequest request = INSTANCE.deserializePlayRequest(INSTANCE.getParser(json));

		Assert.assertEquals(
				BoardLoader.readBoard(FileUtils.readFileToString(new File("src/test/resources/Rest_Recommend_Req_1_board.txt"), "UTF-8")),
				request.getBoard());
		Assert.assertEquals(Disc.YELLOW, request.getCurrentPlayer());
		Assert.assertEquals((Integer) 0, request.getColumn());
	}

	@Test
	public void testSerialisePlayResponse() throws IOException, IllegalMoveException {
		final PlayResponse response = new PlayResponse();
		final Board playerBoard = BoardLoader
				.readBoard(FileUtils.readFileToString(new File("src/test/resources/Rest_Recommend_Req_1_board.txt"), "UTF-8"));
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
		Assert.assertEquals(FileUtils.readFileToString(new File("src/test/resources/Rest_Play_Res_1.json"), "UTF-8"), writer.toString());
	}

	@Test
	public void testDeserialiseGenericRequest() throws IOException {
		String json = FileUtils.readFileToString(new File("src/test/resources/Rest_Play_Req_1.json"), "UTF-8");
		Assert.assertEquals(PlayRequest.class, INSTANCE.deserialiseGenericRequest(INSTANCE.getParser(json)).getClass());

		json = FileUtils.readFileToString(new File("src/test/resources/Rest_Recommend_Req_1.json"), "UTF-8");
		Assert.assertEquals(RecommendRequest.class, INSTANCE.deserialiseGenericRequest(INSTANCE.getParser(json)).getClass());
	}
}
