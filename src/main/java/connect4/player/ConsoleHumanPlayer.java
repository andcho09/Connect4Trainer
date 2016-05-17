package connect4.player;

import java.io.Console;

import connect4.Board;
import connect4.Disc;
import connect4.Player;

/**
 * A human player who inputs commands via the console.
 */
public class ConsoleHumanPlayer extends Player {

	private final Console console;

	public ConsoleHumanPlayer(final String name, final Disc disc, Console console) {
		super(name, disc);
		this.console = console;
	}

	@Override
	public int nextMove(final Board board) {
		console.printf("%s", board.toString());
		final String line = console.readLine("%s, which column to play? [1-%d]: ", toString(),
				board.getNumCols() - 1);

		int col = -1;
		while (col == -1) {
			try {
				col = Integer.parseInt(line);
			} catch (NumberFormatException e) {
				console.printf("The input '%s' is not valid column. Valid columns are 1 to %d.",
						line, board.getNumCols());
			}

		}
		return col;
	}
}
