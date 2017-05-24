package connect4.web;

import java.io.Serializable;

import connect4.Board;
import connect4.GameException;

public class RecommendResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	private int recommendColumn;
	private Board board;
	private GameException exception;

	public int getRecommendColumn() {
		return recommendColumn;
	}

	public void setRecommendColumn(final int recommendColumn) {
		this.recommendColumn = recommendColumn;
	}

	public Board getBoard() {
		return board;
	}

	public void setBoard(final Board board) {
		this.board = board;
	}

	public GameException getException() {
		return exception;
	}

	public void setException(final GameException exception) {
		this.exception = exception;
	}
}
