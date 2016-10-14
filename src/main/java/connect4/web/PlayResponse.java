package connect4.web;

import java.io.Serializable;

import connect4.Board;

public class PlayResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private GameState state;
	private Board playerBoard;
	private Integer playerRow;
	private Integer aiCol;
	private Integer aiRow;
	private Board aiBoard;

	public GameState getState() {
		return state;
	}

	public void setState(final GameState state) {
		this.state = state;
	}

	public Integer getPlayerRow() {
		return playerRow;
	}

	public void setPlayerRow(final Integer playerRow) {
		this.playerRow = playerRow;
	}

	public Board getPlayerBoard() {
		return playerBoard;
	}

	public void setPlayerBoard(final Board playerBoard) {
		this.playerBoard = playerBoard;
	}

	public Integer getAiRow() {
		return aiRow;
	}

	public void setAiRow(final Integer aiRow) {
		this.aiRow = aiRow;
	}

	public Integer getAiCol() {
		return aiCol;
	}

	public void setAiCol(final Integer aiCol) {
		this.aiCol = aiCol;
	}

	public Board getAiBoard() {
		return aiBoard;
	}

	public void setAiBoard(final Board aiBoard) {
		this.aiBoard = aiBoard;
	}
}
