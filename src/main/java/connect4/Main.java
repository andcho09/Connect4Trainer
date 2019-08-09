package connect4;

import java.util.Scanner;

import connect4.player.ConsoleHumanPlayer;
import connect4.player.Player;
import connect4.player.TrainedComputerPlayer;

/**
 * Start that sucker!
 */
public class Main {

	public static final int DEFAULT_COLS = 7;
	public static final int DEFAULT_ROWS = 6;

	public static void main(final String[] args) {
		final Scanner scanner = new Scanner(System.in);
		System.out.println("Connect 4 Trainer");
		System.out.println("=================");
		System.out.println();
		System.out.println("What do you want to do?");
		System.out.println(" 1. Play a game");
		System.out.println(" 2. Analyse a board");
		String choice = "";
		while (!"1".equals(choice) && !"2".equals(choice)) {
			System.out.print("Choice [1, 2]: ");
			choice = scanner.next().trim();
		}
		if ("1".equals(choice)) {
			playGame();
		} else if ("2".equals(choice)) {
			anaylseBoard();
		}
		scanner.close();
	}

	private static void playGame() {
		System.out.println("Let's play!");
		final Board board = new Board(DEFAULT_COLS, DEFAULT_ROWS);

		// final Player player1 = new ConsoleHumanPlayer("Player 1", Disc.RED, System.out, System.in);
		final Player player1 = new TrainedComputerPlayer("Trained 1", Disc.RED);
		final Player player2 = new ConsoleHumanPlayer("Player 2", Disc.YELLOW, System.out, System.in);

		final Controller controller = new Controller(board, player1, player2);
		controller.startGame();
	}

	private static void anaylseBoard() {
		// TODO Auto-generated method stub

	}
}
