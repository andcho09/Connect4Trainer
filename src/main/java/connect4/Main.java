package connect4;

import connect4.player.ConsoleHumanPlayer;
import connect4.player.Player;

/**
 * Start that sucker!
 */
public class Main {

	public static final int DEFAULT_COLS = 7;
	public static final int DEFAULT_ROWS = 6;

	public static void main(final String[] args) {
		Board board = new Board(DEFAULT_COLS, DEFAULT_ROWS);
		Player player1 = new ConsoleHumanPlayer("Player 1", Disc.RED, System.console());
		Player player2 = new ConsoleHumanPlayer("Player 2", Disc.YELLOW, System.console());

		Controller controller = new Controller(board, player1, player2);
		controller.startGame();
	}
}
