package connect4.player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;

import connect4.Board;
import connect4.Disc;

/**
 * A human player who inputs commands via the console. We don't use {@link System#console()} because
 * of an Eclipse <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=122429">bug</a>.
 */
public class ConsoleHumanPlayer extends Player {

	private final BufferedReader in;
	private final PrintWriter out;

	public ConsoleHumanPlayer(final String name, final Disc disc, final PrintStream out,
			final InputStream in) {
		super(name, disc);
		if (in == null) {
			throw new NullPointerException("The InputStreamReader must not be null");
		}
		if (out == null) {
			throw new NullPointerException("The PrintWriter must not be null");
		}
		this.out = new PrintWriter(out, true);
		this.in = new BufferedReader(new InputStreamReader(in));
	}

	@Override
	public int nextMove(final Board board) {
		out.printf("%s", board.toString());
		out.printf("%s, which column to play? [1-%d]: ", toString(), board.getNumCols());

		String line;
		try {
			line = in.readLine();
		} catch (final IOException e) {
			throw new RuntimeException("Could not read player input", e);
		}

		int col = -1;
		while (col == -1) {
			try {
				col = Integer.parseInt(line);
			} catch (final NumberFormatException e) {
				out.printf("The input '%s' is not valid column. Valid columns are 1 to %d.", line,
						board.getNumCols());
			}

		}
		return col - 1;
	}
}
