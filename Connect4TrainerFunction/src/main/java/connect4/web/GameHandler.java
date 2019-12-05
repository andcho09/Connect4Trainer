package connect4.web;

import connect4.api.Board;
import connect4.api.BoardHelper;
import connect4.api.Disc;
import connect4.api.GameException;
import connect4.api.IllegalMoveException;
import connect4.api.Move;
import connect4.api.GameException.ErrorCode;
import connect4.forwarder.AbstractBoardForwarder;
import connect4.trainer.Trainer;

/**
 * Plays a game of Connect 4 via the Web. The encoding of the request and response are implemented by other classes.
 */
public class GameHandler {

	private final Trainer trainer;

	public GameHandler() {
		this.trainer = new Trainer();
	}

	public GameHandler(final AbstractBoardForwarder forwarder) {
		this.trainer = new Trainer(forwarder);
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
		Disc winner = BoardHelper.hasWinner(board);
		if (winner != null) {
			response.setException(new GameException(ErrorCode.ALREADY_WON, "Cannot recommend next move, the game is already won."));
			return response;
		} else if (board.isFull()) {
			response.setException(new GameException(ErrorCode.BOARD_FULL, "Cannot recommend next move, the board is full."));
			return response;
		}

		final Disc currentPlayer = request.getCurrentPlayer();
		final int recommendedCol = this.trainer.recommend(board, currentPlayer);
		response.setRecommendColumn(recommendedCol);
		int recommendedRow;
		try {
			recommendedRow = board.putDisc(recommendedCol, currentPlayer);
		} catch (final IllegalMoveException e) {
			response.setException(e);
			return response;
		}

		winner = BoardHelper.hasWinner(board, new Move(request.getCurrentPlayer(), recommendedCol, recommendedRow));
		if (currentPlayer.equals(winner)) {
			response.setState(GameState.getWinnerState(currentPlayer));
		} else if (board.isFull()) {
			response.setState(GameState.DRAW);
		} else {
			response.setState(GameState.getTurnState(Disc.getOpposite(currentPlayer)));
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
		final Disc currentPlayer = request.getCurrentPlayer();
		response.setState(GameState.getTurnState(currentPlayer));
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

		final int playerRow;
		try {
			playerRow = board.putDisc(request.getColumn(), currentPlayer);
		} catch (final IllegalMoveException e) {
			response.setException(e);
			return response;
		}
		response.setPlayerRow(playerRow);
		winner = BoardHelper.hasWinner(board, new Move(currentPlayer, request.getColumn(), playerRow));
		if (currentPlayer.equals(winner)) {
			response.setState(GameState.getWinnerState(currentPlayer));
		} else if (board.isFull()) {
			response.setState(GameState.DRAW);
		} else {
			final Board opponentBoard = new Board(board);
			response.setAiBoard(opponentBoard);
			final Disc opponent = Disc.getOpposite(currentPlayer);
			response.setState(GameState.getTurnState(opponent));
			final int aiCol = this.trainer.recommend(opponentBoard, opponent);
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
				response.setState(GameState.getWinnerState(opponent));
			} else if (opponentBoard.isFull()) {
				response.setState(GameState.DRAW);
			} else {
				response.setState(GameState.getTurnState(currentPlayer));
			}
		}
		return response;
	}
}
