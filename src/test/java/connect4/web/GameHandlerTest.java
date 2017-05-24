package connect4.web;

import static connect4.BoardHelperTest.RESOURCES_DIR;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import connect4.Board;
import connect4.Disc;
import connect4.GameException;
import connect4.IllegalMoveException;
import connect4.loader.BoardLoader;

public class GameHandlerTest {

	private final GameHandler gameHandler = new GameHandler();

	@Test
	public void testRecommend1() throws IOException, IllegalMoveException {
		final RecommendRequest request = new RecommendRequest();
		request.setCurrentPlayer(Disc.YELLOW);
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_1.txt"));
		request.setBoard(new Board(board));

		final RecommendResponse response = gameHandler.recommend(request);
		Assert.assertNull(response.getException());
		Assert.assertEquals(1, response.getRecommendColumn());
		board.putDisc(1, Disc.YELLOW);
		Assert.assertEquals(board, response.getBoard());
	}

	@Test
	public void testRecommend2() throws IOException, IllegalMoveException {
		final RecommendRequest request = new RecommendRequest();
		request.setCurrentPlayer(Disc.YELLOW);
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardHelperTest_hasWinner1.txt"));
		request.setBoard(new Board(board));

		final RecommendResponse response = gameHandler.recommend(request);
		final GameException exception = response.getException();
		Assert.assertNotNull(exception);
		Assert.assertTrue(exception instanceof GameException);
		Assert.assertEquals(GameException.ErrorCode.ALREADY_WON, exception.getErrorCode());
		Assert.assertEquals(board, response.getBoard());
	}

	@Test
	public void testNext1() throws IOException, IllegalMoveException {
		final PlayRequest request = new PlayRequest();
		request.setCurrentPlayer(Disc.YELLOW);
		request.setColumn(2);
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_1.txt"));
		request.setBoard(new Board(board));

		final PlayResponse response = gameHandler.next(request);
		Assert.assertNull(response.getException());
		Assert.assertEquals(5, response.getPlayerRow().intValue());
		board.putDisc(2, Disc.YELLOW);
		Assert.assertEquals(board, response.getPlayerBoard());
		Assert.assertEquals(GameState.PLAYER_1_TURN, response.getState());
		Assert.assertEquals(1, response.getAiCol().intValue());
		Assert.assertEquals(5, response.getAiRow().intValue());
		board.putDisc(1, Disc.RED);
		Assert.assertEquals(board, response.getAiBoard());

	}
}
