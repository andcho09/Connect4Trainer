package connect4;

import connect4.player.Player;

/**
 * Controls players, rules, and game state.
 */
public class Controller {

	private final Board board;
	private final Player player1;
	private final Player player2;

	public Controller(final Board board, final Player player1, final Player player2) {
		this.board = board;
		this.player1 = player1;
		this.player2 = player2;
	}

	public void startGame() {
		// while not in win condition
		// - check ends conditions (someone won, or no more moves)
		// - player 1 or 2 go
		// - check win conditions
		// - flop current player

	}
}
