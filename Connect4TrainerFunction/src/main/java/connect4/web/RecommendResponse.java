package connect4.web;

import java.io.Serializable;

import connect4.api.Board;
import connect4.api.GameException;

public class RecommendResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	private int recommendColumn;
	private int recommendRow;
	private Board board;
	private GameState state;
	private GameException exception;

	public int getRecommendColumn() {
		return recommendColumn;
	}

	public void setRecommendColumn(final int recommendColumn) {
		this.recommendColumn = recommendColumn;
	}

	public int getRecommendRow() {
		return recommendRow;
	}

	public void setRecommendRow(final int recommendRow) {
		this.recommendRow = recommendRow;
	}

	public Board getBoard() {
		return board;
	}

	public void setBoard(final Board board) {
		this.board = board;
	}

	public GameState getState() {
		return state;
	}

	public void setState(final GameState state) {
		this.state = state;
	}

	public GameException getException() {
		return exception;
	}

	public void setException(final GameException exception) {
		this.exception = exception;
	}
}
