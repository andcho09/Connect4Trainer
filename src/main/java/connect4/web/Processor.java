package connect4.web;

import connect4.Board;
import connect4.Disc;
import connect4.IllegalMoveException;
import connect4.trainer.Trainer;

public class Processor {

	public RecommendResponse recommend(final RecommendRequest request) throws ProcessingException {
		final Board board = request.getBoard();
		final Disc currentPlayer = request.getCurrentPlayer();

		final RecommendResponse response = new RecommendResponse();
		final int recommendColumn = new Trainer().recommend(board, currentPlayer);
		response.setRecommendColumn(recommendColumn);
		final Board newBoard = new Board(board);
		try {
			newBoard.putDisc(recommendColumn, currentPlayer);
		} catch (final IllegalMoveException e) {
			throw new ProcessingException("Could not recommend next play as the recommend play is unplayable", e);
		}
		response.setBoard(newBoard);
		return response;
	}
}
