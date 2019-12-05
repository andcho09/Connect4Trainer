package connect4;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.analysis.BoardAnalysis;
import connect4.api.analysis.ColumnAnalysis;
import connect4.api.json.StoreBoardRequest;
import connect4.forwarder.AwsStoreHandlerForwarder;
import connect4.loader.BoardLoader;
import connect4.player.ConsoleHumanPlayer;
import connect4.player.Player;
import connect4.player.TrainedComputerPlayer;
import connect4.rest.WebJsonStreamingObjectFactory;
import connect4.store.lambda.AwsLambdaStoreHandler;
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
		System.out.println(" 3. Analyse a board and output as a StoreBoardRequest JSON template");
		System.out.println(" 4. Send a board to DynamoDb for storage");
		System.out.println(" 5. Retrieve a random board from DynamoDb");
		String choice = "";
		while (!"1".equals(choice) && !"2".equals(choice) && !"3".equals(choice) && !"4".equals(choice) && !"5".equals(choice)) {
			System.out.print("Choice [1, 2, 3, 4, 5]: ");
			choice = scanner.next().trim();
		}
		if ("1".equals(choice)) {
			playGame();
		} else if ("2".equals(choice)) {
			anaylseBoard(scanner, false);
		} else if ("3".equals(choice)) {
			anaylseBoard(scanner, true);
		} else if ("4".equals(choice)) {
			sendBoardToDynamoDb(scanner);
		} else if ("5".equals(choice)) {
			getRandomBoardFromDynamoDb();
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

	private static void anaylseBoard(final Scanner scanner, final boolean outputStoreBoardRequestTemplate) {
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
						.map(n -> n.getColumn() + 1 + " (column[" + n.getColumn() + "])").collect(Collectors.joining(", "))));
			}
			for (final ColumnAnalysis columnAnalysis : lastBestBoardAnalysis) {
				System.out.println(columnAnalysis.toStringDetail());
			}
			if (outputStoreBoardRequestTemplate) {
				final StoreBoardRequest request = new StoreBoardRequest();
				request.setBoard(board);
				request.setBoardAnalysis(lastBestBoardAnalysis);
				request.setCurrentPlayer(disc);

				final StringWriter writer = new StringWriter();
				try {
					final JsonGenerator generator = WebJsonStreamingObjectFactory.getInstance().getGenerator(writer);
					WebJsonStreamingObjectFactory.getInstance().serialize(generator, request);
					generator.close();
				} catch (final IOException e) {
					throw new RuntimeException("Couldn't serialise StoreBoardRequest", e);
				}
				System.out.println("StoreBoardRequest JSON: " + writer.toString());
			}
			System.out.println();
		}
	}

	private static void sendBoardToDynamoDb(final Scanner scanner) {
		File boardFile = null;
		while (boardFile == null || !boardFile.canRead()) {
			System.out.print("Enter path to file of a board to send: ");
			boardFile = new File(scanner.next().trim());
			if (!boardFile.canRead()) {
				System.out.println(String.format("Hmmm, couldn't load file '%s'. Enter a new file: ", boardFile.getAbsolutePath()));
			}
		}

		Disc currentPlayer = null;
		while (currentPlayer == null) {
			System.out.print("Enter current player (r,y): ");
			final String discInpput = scanner.next().trim();
			if (discInpput.length() > 0) {
				currentPlayer = Disc.getDisc(discInpput.charAt(0));
			}
		}

		System.out.println(String.format("Analysing board '%s' for %s player ...", boardFile.getAbsolutePath(), currentPlayer));
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
		trainer.recommend(board, currentPlayer);

		if (new AwsStoreHandlerForwarder().receive(board, currentPlayer, trainer.getLastBestBoardAnalysis())) {
			System.out.println("Board sent to Lambda");
		} else {
			System.out.println("Board was not sent to Lambda. It's not intersting.");
		}
	}

	private static void getRandomBoardFromDynamoDb() {
		AwsLambdaStoreHandler handler = new AwsLambdaStoreHandler();
		StoreBoardRequest randomBoard = handler.getRandomBoard();
		if (randomBoard == null) {
			System.out.println("Response was null. Must be no boards in DynamoDB.");
		}
	}
}
