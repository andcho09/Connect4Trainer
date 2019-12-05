package connect4.loader;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonParser;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.IllegalMoveException;
import connect4.rest.WebJsonStreamingObjectFactory;

/**
 * Reconstitutes a {@link Board} from some external format.
 */
public class BoardLoader {

	public static char COMMENT_CHAR = '#';

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
	 * <li>comments start with the '#' character
	 * </ul>
	 * </p>
	 * <p>
	 * <b>JSON format:</b>
	 * See {@link WebJsonStreamingObjectFactory#deserializeBoard(JsonParser)}
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

		int sizeLineIndex = 0;
		String line;

		// Figure out columns and rows
		do {
			line = lines[sizeLineIndex++].trim();
		} while (isCommentLine(line) && sizeLineIndex < lines.length);
		final String[] boardParams = StringUtils.split(line, ' ');
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

		Board result;
		try {
			result = new Board(nCols, nRows);
		} catch (final IllegalArgumentException e) {
			throw new InvalidBoardFormatException("The board is invalid. Could not create board.", e);
		}
		int lineIndex = lines.length - 1;
		int rowsFound = 0;
		for (; rowsFound < nRows && lineIndex > 0; lineIndex--) {
			line = lines[lineIndex].trim();
			if (isCommentLine(line)) {
				continue;
			}
			if (nCols != line.length()) {
				throw new InvalidBoardFormatException(
						String.format("The board is invalid. Expected %d columns but the row was '%s'", nCols, line));
			}
			for (int c = 0; c < nCols; c++) {
				final Disc disc = Disc.getDisc(line.charAt(c));
				if (disc != null) {
					try {
						final int resultRow = result.putDisc(c, disc);
						if (resultRow != rowsFound) {
							throw new InvalidBoardFormatException("The board is invalid. Placed disc didn't match the expect row.");
						}
					} catch (final IllegalMoveException e) {
						throw new InvalidBoardFormatException("The board is invalid. It can't be played.", e);
					}
				}
			}
			rowsFound++;
		}
		if (nRows != rowsFound) {
			throw new InvalidBoardFormatException(
					String.format("The board is invalid. Metadata said %d rows but found %d rows.", nRows, rowsFound));
		}
		line = lines[lineIndex].trim();
		if (sizeLineIndex <= lineIndex && !isCommentLine(line)) {
			throw new InvalidBoardFormatException(String.format("The board is invalid. Expected %d rows but found more.", nRows));
		}

		return result;
	}

	public static Board readJsonBoard(final String json) {
		final WebJsonStreamingObjectFactory factory = WebJsonStreamingObjectFactory.getInstance();
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

	/**
	 * @param line the String to check
	 * @return <code>true</code> if the line is a comment or should be ignored, else <code>false</code>
	 */
	private static boolean isCommentLine(final String line) {
		return "".equals(line) || line.charAt(0) == COMMENT_CHAR;
	}
}
