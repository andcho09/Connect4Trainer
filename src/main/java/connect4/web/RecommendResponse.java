package connect4.web;

import java.io.Serializable;

import connect4.Board;

public class RecommendResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	private int recommendColumn;
	private Board board;

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

}
