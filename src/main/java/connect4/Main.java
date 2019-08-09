package connect4;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.stream.Collectors;

import connect4.loader.BoardLoader;
import connect4.player.ConsoleHumanPlayer;
import connect4.player.Player;
import connect4.player.TrainedComputerPlayer;
import connect4.trainer.BoardAnalysis;
import connect4.trainer.ColumnAnalysis;
import connect4.trainer.Trainer;

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
			anaylseBoard(scanner);
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

	private static void anaylseBoard(final Scanner scanner) {
		File boardFile = null;
		while (boardFile == null || !boardFile.canRead()) {
			System.out.print("Enter path to file of a board to analyse: ");
			boardFile = new File(scanner.next().trim());
			if (!boardFile.canRead()) {
				System.out.println(String.format("Hmmm, couldn't load file '%s'. Enter a new file: ", boardFile.getAbsolutePath()));
			}
		}
		System.out.println(String.format("Analysing board '%s'...", boardFile.getAbsolutePath()));
		Board board;
		try {
			board = BoardLoader.readBoard(boardFile);
		} catch (final IOException e) {
			System.out.println(
					String.format("Can't load board from file '%s' because: %s", boardFile.getAbsolutePath(), e.getLocalizedMessage()));
			return;
		}
		System.out.println(board.toString(true));
		final Trainer trainer = new Trainer();

		for (final Disc disc : Disc.values()) {
			System.out.println(String.format("Analysing board for %s...", disc.toString()));
			final int column = trainer.recommend(board, disc);
			final BoardAnalysis lastBestBoardAnalysis = trainer.getLastBestBoardAnalysis();
			if (lastBestBoardAnalysis.size() == 1) {
				System.out.println(String.format("Recommended move is column %d (column[%d])", column + 1, column));
			} else {
				System.out.println(String.format("Recommended columns are: %s", lastBestBoardAnalysis.stream()
						.map(n -> (n.getColumn() + 1) + " (column[" + n.getColumn() + "])").collect(Collectors.joining(", "))));
			}
			for (final ColumnAnalysis columnAnalysis : lastBestBoardAnalysis) {
				System.out.println(columnAnalysis.toStringDetail());
			}
			System.out.println();
		}
	}
}
