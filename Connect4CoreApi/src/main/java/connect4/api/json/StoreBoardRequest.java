package connect4.api.json;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.analysis.BoardAnalysis;

/**
 * Represents a request to store a board and analysis.
 */
public class StoreBoardRequest extends AbstractBoardRequest {

	private static final long serialVersionUID = 1L;

	private Disc currentPlayer;
	private Board board;
	private BoardAnalysis boardAnalysis;

	public Disc getCurrentPlayer() {
		return this.currentPlayer;
	}

	public void setCurrentPlayer(final Disc currentPlayer) {
		this.currentPlayer = currentPlayer;
	}

	public Board getBoard() {
		return this.board;
	}

	public void setBoard(final Board board) {
		this.board = board;
	}

	public BoardAnalysis getBoardAnalysis() {
		return this.boardAnalysis;
	}

	public void setBoardAnalysis(final BoardAnalysis boardAnalysis) {
		this.boardAnalysis = boardAnalysis;
	}
}
