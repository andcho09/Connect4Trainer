package connect4.web;

import java.io.Serializable;

import connect4.Board;
import connect4.GameException;

public class PlayResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private GameState state;
	private Board playerBoard;
	private Integer playerRow;
	private Integer aiCol;
	private Integer aiRow;
	private Board aiBoard;
	private GameException exception;

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

	/**
	 * @return the {@link Board} just after the player made their play
	 */
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

	/**
	 * @return the {@link Board} just after the AI player made their play
	 */
	public Board getAiBoard() {
		return aiBoard;
	}

	public void setAiBoard(final Board aiBoard) {
		this.aiBoard = aiBoard;
	}

	public GameException getException() {
		return exception;
	}

	public void setException(final GameException exception) {
		this.exception = exception;
	}
}
