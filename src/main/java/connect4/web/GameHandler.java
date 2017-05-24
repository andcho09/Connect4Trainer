package connect4.web;

import connect4.Board;
import connect4.BoardHelper;
import connect4.Disc;
import connect4.GameException;
import connect4.GameException.ErrorCode;
import connect4.IllegalMoveException;
import connect4.Move;
import connect4.trainer.Trainer;

/**
 * Plays a game of Connect 4 via the Web. The encoding of the request and response are implemented by other classes.
 */
public class GameHandler {

	private final Trainer trainer;

	public GameHandler() {
		this.trainer = new Trainer();
	}

	/**
	 * Recommends where to play next. This assumes the game is not over.
	 * @param request the state of the game which a recommendation will be made
	 * @return the recommendation
	 */
	public RecommendResponse recommend(final RecommendRequest request) {
		final RecommendResponse response = new RecommendResponse();
		response.setRecommendColumn(-1);
		final Board board = request.getBoard();
		response.setBoard(board);
		final Disc winner = BoardHelper.hasWinner(board);
		if (winner != null) {
			response.setException(new GameException(ErrorCode.ALREADY_WON, "Cannot recommend next move, the game is already won."));
			return response;
		} else if (board.isFull()) {
			response.setException(new GameException(ErrorCode.BOARD_FULL, "Cannot recommend next move, the board is full."));
			return response;
		}

		final Disc currentPlayer = request.getCurrentPlayer();
		final int recommendColumn = trainer.recommend(board, currentPlayer);
		response.setRecommendColumn(recommendColumn);
		try {
			board.putDisc(recommendColumn, currentPlayer);
		} catch (final IllegalMoveException e) {
			response.setException(e);
			return response;
		}
		return response;
	}

	/**
	 * Plays the player's disc and also makes a move for the AI {@link Trainer} opponent.
	 * @param request the state of the game to play
	 * @return the new game state including a move by the AI {@link Trainer}
	 */
	public PlayResponse next(final PlayRequest request) {
		final PlayResponse response = new PlayResponse();
		response.setState(GameState.PLAYER_1_TURN); // TODO this is weird. Numbers to discs?
		final Board board = request.getBoard();
		response.setPlayerBoard(board);

		Disc winner = BoardHelper.hasWinner(board);
		if (winner != null) {
			response.setException(new GameException(ErrorCode.ALREADY_WON, "Cannot recommend next move, the game is already won."));
			return response;
		} else if (board.isFull()) {
			response.setException(new GameException(ErrorCode.BOARD_FULL, "Cannot recommend next move, the board is full."));
			return response;
		}

		response.setPlayerBoard(board);
		final int playerRow;
		try {
			playerRow = board.putDisc(request.getColumn(), request.getCurrentPlayer());
		} catch (final IllegalMoveException e) {
			response.setException(e);
			return response;
		}
		response.setPlayerRow(playerRow);
		winner = BoardHelper.hasWinner(board, new Move(request.getCurrentPlayer(), request.getColumn(), playerRow));
		if (request.getCurrentPlayer().equals(winner)) {
			response.setState(GameState.PLAYER_1_WON); // TODO this is weird. Numbers to discs?
		} else if (board.isFull()) {
			response.setState(GameState.DRAW);
		} else {
			final Board opponentBoard = new Board(board);
			response.setAiBoard(opponentBoard);
			final Disc opponent = Disc.getOpposite(request.getCurrentPlayer());
			response.setState(GameState.PLAYER_2_TURN);
			final int aiCol = trainer.recommend(opponentBoard, opponent);
			final int aiRow;
			try {
				aiRow = opponentBoard.putDisc(aiCol, opponent);
			} catch (final IllegalMoveException e) {
				response.setException(e);
				return response;
			}
			response.setAiCol(aiCol);
			response.setAiRow(aiRow);
			winner = BoardHelper.hasWinner(opponentBoard, new Move(opponent, aiCol, aiRow));
			if (opponent.equals(winner)) {
				response.setState(GameState.PLAYER_2_WON); // TODO this is weird. Numbers to discs?
			} else if (opponentBoard.isFull()) {
				response.setState(GameState.DRAW);
			} else {
				response.setState(GameState.PLAYER_1_TURN);
			}
		}
		return response;
	}
}
