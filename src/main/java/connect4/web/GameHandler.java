package connect4.web;

import connect4.Board;
import connect4.BoardHelper;
import connect4.Disc;
import connect4.GameException;
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
	 * @throws GameException if a recommendation could not be made (e.g. if the board is full)
	 */
	public RecommendResponse recommend(final RecommendRequest request) throws GameException {
		final Board board = request.getBoard();
		final Disc winner = BoardHelper.hasWinner(board);
		if (winner != null) {
			throw new GameException("Cannot play next move, the game is already won.");
		} else if (board.isFull()) {
			throw new GameException("Cannot play next move, the board is full.");
		}

		final RecommendResponse response = new RecommendResponse();
		final Disc currentPlayer = request.getCurrentPlayer();
		final int recommendColumn = trainer.recommend(board, currentPlayer);
		response.setRecommendColumn(recommendColumn);
		final Board newBoard = new Board(board);
		newBoard.putDisc(recommendColumn, currentPlayer);
		response.setBoard(newBoard);
		return response;
	}

	/**
	 * Plays the player's disc and also makes a move for the AI {@link Trainer} opponent.
	 * @param request the state of the game to play
	 * @return the new game state including a move by the AI {@link Trainer}
	 * @throws GameException if the game is already won, is already full, the player or AI played an illegal move
	 */
	public PlayResponse next(final PlayRequest request) throws GameException {
		final Board board = request.getBoard();
		Disc winner = BoardHelper.hasWinner(board);
		if (winner != null) {
			throw new GameException("Cannot play next move, the game is already won.");
		} else if (board.isFull()) {
			throw new GameException("Cannot play next move, the board is full.");
		}

		final PlayResponse response = new PlayResponse();
		response.setPlayerBoard(board);
		final int playerRow = board.putDisc(request.getColumn(), request.getCurrentPlayer());
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
			final int aiCol = trainer.recommend(opponentBoard, opponent);
			final int aiRow = opponentBoard.putDisc(aiCol, opponent);
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
