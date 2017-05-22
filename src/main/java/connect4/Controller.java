package connect4;

import connect4.player.Player;

/**
 * Controls players, rules, and game state for playing via the command-line.
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
		Disc winner = BoardHelper.hasWinner(board);
		Player currentPlayer = player1;
		Move lastMove;

		// while game not over (no winner and still moves left)
		while (winner == null && !board.isFull()) {
			// move
			lastMove = playPlayerMove(currentPlayer);
			System.out.println(currentPlayer.getDisc() + " played in column " + (lastMove.getCol() + 1));

			// Is there a winner now?
			winner = BoardHelper.hasWinner(board, lastMove);
			if (winner != null) {
				break;
			}

			// switch players
			currentPlayer = currentPlayer == player1 ? player2 : player1;
		}

		System.out.println(board.toString());
		if (winner == null) {
			System.out.println("It's a draw!");
		} else {
			System.out.println("Player " + winner.name() + " won!");
		}
	}

	/**
	 * Hassles the player until the make a valid move.
	 * @param player the player's move to get
	 * @return the {@link Move}
	 */
	private Move playPlayerMove(final Player player) {
		final Move move = null;
		while (move == null) {
			final int col = player.nextMove(board);
			try {
				final int row = board.putDisc(col, player.getDisc());
				return new Move(player.getDisc(), col, row);
			} catch (final IllegalMoveException ignored) {}
		}
		return move;
	}
}
