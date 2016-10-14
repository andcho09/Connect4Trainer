package connect4.loader;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonParser;

import connect4.Board;
import connect4.Disc;
import connect4.IllegalMoveException;
import connect4.rest.JsonStreamingObjectFactory;

/**
 * Reconstitutes a {@link Board} from some external format.
 */
public class BoardLoader {

	private BoardLoader() {
	}

	/**
	 * <p>
	 * Reads a board from a {@link String} where the board is in either plain text or JSON format.
	 * </p>
	 * <p>
	 * <b>Plain text format:</b>
	 * <ul>
	 * <li>first row is: &lt;nCols> &lt;nRows>
	 * <li>next rows are the board in the format <pre>.......
	 *.......
	 *.......
	 *...r...
	 *...y...
	 *.r.yr..
	 *rr.yy..
	 * </pre>
	 * </ul>
	 * </p>
	 * <p>
	 * <b>JSON format:</b>
	 * See {@link JsonStreamingObjectFactory#deserializeBoard(JsonParser)}
	 * </p>
	 * @param board in {@link String} plain text or JSON format
	 * @return the {@link Board}or <code>null</code> if the board
	 */
	public static Board readBoard(final String board) {
		if (board == null) {
			return null;
		}
		final String b = board.trim();
		if (board.startsWith("{")) {
			return readJsonBoard(b);
		}
		final String[] lines = StringUtils.split(b, '\n');
		if (lines.length < 2) {
			throw new InvalidBoardFormatException("The board is invalid. Must be at least two lines.");
		}

		// Figure out columns and rows
		final String firstLine = lines[0].trim();
		final String[] boardParams = StringUtils.split(firstLine, ' ');
		if (boardParams.length < 2) {
			throw new InvalidBoardFormatException(
					"The board is invalid. The first line must have at least two arguments for: <nCols> <nRows>");
		}
		int nCols = -1;
		try {
			nCols = Integer.parseInt(boardParams[0]);
		} catch (final NumberFormatException e) {
			throw new InvalidBoardFormatException("The board is invalid. The number of columns could not be parsed as an integer.", e);
		}
		int nRows = -1;
		try {
			nRows = Integer.parseInt(boardParams[1]);
		} catch (final NumberFormatException e) {
			throw new InvalidBoardFormatException("The board is invalid. The number of rows could not be parsed as an integer.", e);
		}
		if (nRows + 1 != lines.length) {
			throw new InvalidBoardFormatException(
					String.format("The board is invalid. Metadata said %d rows but found %d rows", nRows, lines.length - 1));
		}

		Board result;
		try {
			result = new Board(nCols, nRows);
		} catch (final IllegalArgumentException e) {
			throw new InvalidBoardFormatException("The board is invalid. Could not create board.", e);
		}
		for (int r = nRows; r > 0; r--) {
			final String rowString = lines[r];
			for (int c = 0; c < nCols; c++) {
				final Disc disc = Disc.getDisc(rowString.charAt(c));
				if (disc != null) {
					try {
						final int resultRow = result.putDisc(c, disc);
						if (resultRow != nRows - r) {
							throw new InvalidBoardFormatException("The board is invalid. Placed disc didn't match the expect row.");
						}
					} catch (final IllegalMoveException e) {
						throw new InvalidBoardFormatException("The board is invalid. It can't be played.", e);
					}
				}
			}
		}

		return result;
	}

	public static Board readJsonBoard(final String json) {
		final JsonStreamingObjectFactory factory = JsonStreamingObjectFactory.getInstance();
		JsonParser parser;
		try {
			parser = factory.getParser(json);
		} catch (final IOException e) {
			throw new InvalidBoardFormatException("Could not get JSON parser when attempting to read board", e);
		}
		try {
			parser.nextToken();
			final Board board = factory.deserializeBoard(parser);
			parser.close();
			return board;
		} catch (final IOException e) {
			throw new InvalidBoardFormatException("The board is not a valid JSON board", e);
		}
	}

	/**
	 * Reads a board from a file.
	 * @param inputFile the file containing the board
	 * @return the {@link Board}
	 * @throws IOException if the file cannot be read
	 */
	public static Board readBoard(final File inputFile) throws IOException {
		final String inputBoardString = FileUtils.readFileToString(inputFile, "UTF-8");
		return BoardLoader.readBoard(inputBoardString);
	}
}
