package connect4.web;

import java.io.Serializable;

import connect4.api.Board;
import connect4.api.Disc;

public class RecommendRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private Disc currentPlayer;
	private Board board;

	public Disc getCurrentPlayer() {
		return currentPlayer;
	}

	public void setCurrentPlayer(final Disc currentPlayer) {
		this.currentPlayer = currentPlayer;
	}

	public Board getBoard() {
		return board;
	}

	public void setBoard(final Board board) {
		this.board = board;
	}

}
