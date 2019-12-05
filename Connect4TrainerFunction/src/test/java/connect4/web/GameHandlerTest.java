package connect4.web;

import static connect4.BoardHelperTest.RESOURCES_DIR;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.GameException;
import connect4.api.IllegalMoveException;
import connect4.loader.BoardLoader;

public class GameHandlerTest {

	private final GameHandler gameHandler = new GameHandler();

	@Test
	public void testRecommendWon() throws IOException, IllegalMoveException {
		final RecommendRequest request = new RecommendRequest();
		request.setCurrentPlayer(Disc.YELLOW);
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_1.txt"));
		request.setBoard(new Board(board));

		final RecommendResponse response = gameHandler.recommend(request);
		Assert.assertNull(response.getException());
		Assert.assertEquals(GameState.PLAYER_Y_WON, response.getState());
		Assert.assertEquals(1, response.getRecommendColumn());
		board.putDisc(1, Disc.YELLOW);
		Assert.assertEquals(board, response.getBoard());
	}

	@Test
	public void testRecommendSwapTurn() throws IOException, IllegalMoveException {
		final RecommendRequest request = new RecommendRequest();
		request.setCurrentPlayer(Disc.YELLOW);
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardLoaderTest_1.txt"));
		request.setBoard(new Board(board));

		final RecommendResponse response = gameHandler.recommend(request);
		Assert.assertNull(response.getException());
		Assert.assertEquals(GameState.PLAYER_R_TURN, response.getState());
	}

	@Test
	public void testRecommendAlreadyWon() throws IOException, IllegalMoveException {
		final RecommendRequest request = new RecommendRequest();
		request.setCurrentPlayer(Disc.YELLOW);
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardHelperTest_hasWinner1.txt"));
		request.setBoard(new Board(board));

		final RecommendResponse response = gameHandler.recommend(request);
		Assert.assertEquals(GameException.ErrorCode.ALREADY_WON, response.getException().getErrorCode());
		Assert.assertEquals(board, response.getBoard());
	}

	@Test
	public void testPlayNormal() throws IOException, IllegalMoveException {
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
		Assert.assertEquals(GameState.PLAYER_Y_TURN, response.getState());
		Assert.assertEquals(1, response.getAiCol().intValue());
		Assert.assertEquals(5, response.getAiRow().intValue());
		board.putDisc(1, Disc.RED);
		Assert.assertEquals(board, response.getAiBoard());
	}

	@Test
	public void testPlayAlreadyFull() throws IOException {
		final PlayRequest request = new PlayRequest();
		request.setCurrentPlayer(Disc.YELLOW);
		request.setColumn(2);
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_isFull2.txt"));
		request.setBoard(board);
		final PlayResponse response = gameHandler.next(request);
		Assert.assertEquals(GameException.ErrorCode.BOARD_FULL, response.getException().getErrorCode());
	}

	@Test
	public void testPlayAlreadyWon() throws IOException {
		final PlayRequest request = new PlayRequest();
		request.setCurrentPlayer(Disc.YELLOW);
		request.setColumn(2);
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardHelperTest_hasWinner1.txt"));
		request.setBoard(board);
		final PlayResponse response = gameHandler.next(request);
		Assert.assertEquals(GameException.ErrorCode.ALREADY_WON, response.getException().getErrorCode());
	}

	@Test
	public void testPlayFull() throws IOException, IllegalMoveException {
		final PlayRequest request = new PlayRequest();
		request.setCurrentPlayer(Disc.RED);
		request.setColumn(0);
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_isFull1.txt"));
		request.setBoard(new Board(board));
		final PlayResponse response = gameHandler.next(request);

		Assert.assertEquals(GameState.PLAYER_R_TURN, response.getState());
		Assert.assertEquals(GameException.ErrorCode.COLUMN_FULL, response.getException().getErrorCode());
		Assert.assertNull(response.getAiBoard());
		Assert.assertNull(response.getAiCol());
		Assert.assertNull(response.getAiRow());
		Assert.assertNull(response.getPlayerRow());
		Assert.assertEquals(board, response.getPlayerBoard());
	}

	@Test
	public void testPlayIllegal() throws IOException, IllegalMoveException {
		final PlayRequest request = new PlayRequest();
		request.setCurrentPlayer(Disc.RED);
		request.setColumn(7);
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_isFull1.txt"));
		request.setBoard(new Board(board));
		final PlayResponse response = gameHandler.next(request);

		Assert.assertEquals(GameState.PLAYER_R_TURN, response.getState());
		Assert.assertEquals(GameException.ErrorCode.OUT_OF_BOUNDS, response.getException().getErrorCode());
		Assert.assertNull(response.getAiBoard());
		Assert.assertNull(response.getAiCol());
		Assert.assertNull(response.getAiRow());
		Assert.assertNull(response.getPlayerRow());
		Assert.assertEquals(board, response.getPlayerBoard());
	}

	@Test
	public void testPlayYellowWon() throws IOException, IllegalMoveException {
		final PlayRequest request = new PlayRequest();
		request.setCurrentPlayer(Disc.YELLOW);
		request.setColumn(1);
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_1.txt"));
		request.setBoard(new Board(board));
		final PlayResponse response = gameHandler.next(request);

		Assert.assertEquals(GameState.PLAYER_Y_WON, response.getState());
		Assert.assertNull(response.getException());
		Assert.assertNull(response.getAiBoard());
		Assert.assertNull(response.getAiCol());
		Assert.assertNull(response.getAiRow());
		board.putDisc(1, Disc.YELLOW);
		Assert.assertEquals(board, response.getPlayerBoard());
		Assert.assertEquals((Integer) 5, response.getPlayerRow());
	}

	@Test
	public void testPlayDraw() throws IOException, IllegalMoveException {
		final PlayRequest request = new PlayRequest();
		request.setCurrentPlayer(Disc.RED);
		request.setColumn(6);
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_isFull3.txt"));
		request.setBoard(new Board(board));
		final PlayResponse response = gameHandler.next(request);

		Assert.assertEquals(GameState.DRAW, response.getState());
		Assert.assertNull(response.getException());
		Assert.assertNull(response.getAiBoard());
		Assert.assertNull(response.getAiCol());
		Assert.assertNull(response.getAiRow());
		board.putDisc(6, Disc.RED);
		Assert.assertEquals(board, response.getPlayerBoard());
		Assert.assertEquals((Integer) 5, response.getPlayerRow());
	}

	@Test
	public void testPlayOpponentWin() throws IOException, IllegalMoveException {
		final PlayRequest request = new PlayRequest();
		request.setCurrentPlayer(Disc.RED);
		request.setColumn(2);
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "TrainerTest_1.txt"));
		request.setBoard(new Board(board));
		final PlayResponse response = gameHandler.next(request);

		Assert.assertEquals(GameState.PLAYER_Y_WON, response.getState());
		Assert.assertNull(response.getException());
		board.putDisc(2, Disc.RED);
		Assert.assertEquals(board, response.getPlayerBoard());
		Assert.assertEquals((Integer) 5, response.getPlayerRow());

		board.putDisc(1, Disc.YELLOW);
		Assert.assertEquals(board, response.getAiBoard());
		Assert.assertEquals((Integer) 1, response.getAiCol());
		Assert.assertEquals((Integer) 5, response.getAiRow());
	}

	@Test
	public void testPlayOpponentDraw() throws IOException, IllegalMoveException {
		final PlayRequest request = new PlayRequest();
		request.setCurrentPlayer(Disc.YELLOW);
		request.setColumn(1);
		final Board board = BoardLoader.readBoard(new File(RESOURCES_DIR + "BoardTest_isFull1.txt"));
		request.setBoard(new Board(board));
		final PlayResponse response = gameHandler.next(request);

		Assert.assertEquals(GameState.DRAW, response.getState());
		Assert.assertNull(response.getException());
		board.putDisc(1, Disc.YELLOW);
		Assert.assertEquals(board, response.getPlayerBoard());
		Assert.assertEquals((Integer) 5, response.getPlayerRow());

		board.putDisc(6, Disc.RED);
		Assert.assertEquals(board, response.getAiBoard());
		Assert.assertEquals((Integer) 6, response.getAiCol());
		Assert.assertEquals((Integer) 5, response.getAiRow());
	}
}
