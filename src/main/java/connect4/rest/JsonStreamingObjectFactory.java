package connect4.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import connect4.Board;
import connect4.Disc;
import connect4.GameException;
import connect4.IllegalMoveException;
import connect4.web.GameState;
import connect4.web.PlayRequest;
import connect4.web.PlayResponse;
import connect4.web.RecommendRequest;
import connect4.web.RecommendResponse;

/**
 * <p>
 * Jackson-based streaming JSON parser/generator. Hide all of the ugliness here.
 * </p>
 * <p>
 * Serializing rules:
 * </p>
 * <ul>
 * <li>child decides whether it wants to start a new object or not
 * </ul>
 * <p>
 * Deserializing rules:
 * </p>
 * <ul>
 * <li>current token should be the first field
 * </ul>
 */
public class JsonStreamingObjectFactory {

	private static JsonStreamingObjectFactory INSTANCE = new JsonStreamingObjectFactory();

	private final JsonFactory factory;

	private JsonStreamingObjectFactory() {
		this.factory = new JsonFactory(); // This is expensive to create
	}

	public void serialize(final JsonGenerator g, final Board board) throws IOException {
		serialize(g, board, "board");
	}

	public void serialize(final JsonGenerator g, final Board board, final String boardName) throws IOException {
		if (board == null) {
			g.writeNullField(boardName);
			return;
		}
		g.writeObjectFieldStart(boardName);
		g.writeNumberField("numCols", board.getNumCols());
		g.writeNumberField("numRows", board.getNumRows());
		g.writeArrayFieldStart("rows");
		for (int r = 0; r < board.getNumRows(); r++) {
			g.writeStartArray();
			for (int c = 0; c < board.getNumCols(); c++) {
				final Disc disc = board.getDisc(c, r);
				g.writeString(disc == null ? "." : "" + disc.getSymbol());
			}
			g.writeEndArray();
		}
		g.writeEndArray();
		g.writeEndObject();
	}

	/**
	 * Parse the JSON text as a {@link Board}. The JSON text should look like:
	 * <pre>{	"numCols": 7,
	"numRows": 6,
	"rows": [
		["r", "r", "r", "y", "y", "r", "r"],
		["r", "y", "y", "y", "r", "y", "y"],
		[".", "r", "r", "y", ".", "y", "."],
		[".", "y", "y", "r", ".", "r", "."],
		[".", "r", ".", "y", ".", ".", "."],
		[".", ".", ".", ".", ".", ".", "."]
	]}</pre>where "rows" is a 2D array. Each entry is a row. First row is the bottom row, last row is the top row.
	 * @param jp the {@link JsonParser}
	 * @return the {@link Board}
	 * @throws IOException if the board could not be parsed.
	 */
	public Board deserializeBoard(final JsonParser jp) throws IOException {
		int numCols = -1;
		int numRows = -1;
		final List<List<Disc>> rows = new LinkedList<>();
		while (jp.nextToken() != JsonToken.END_OBJECT) {
			final String fieldname = jp.getCurrentName();
			jp.nextToken();
			if ("numCols".equals(fieldname)) {
				numCols = jp.getIntValue();
			} else if ("numRows".equals(fieldname)) {
				numRows = jp.getIntValue();
			} else if ("rows".equals(fieldname)) {
				if (numCols == -1 || numRows == -1) {
					throw new IOException("Could not parse board. Both 'numCols' and 'numRows' must be defined before the 'board'.");
				}
				// "rows": [
				// ["y", "r", "r", "y", "r", "r", "."],
				// [".", "r", "r", "y", ".", ".", "."],
				// [".", "y", "y", ".", ".", ".", "."],
				// [".", "y", "r", ".", ".", ".", "."],
				// [".", "y", "r", ".", ".", ".", "."],
				// [".", "y", ".", ".", ".", ".", "."]
				// ]
				while (jp.nextToken() != JsonToken.END_ARRAY) {
					final List<Disc> row = new LinkedList<>();
					rows.add(row);
					while (jp.nextToken() != JsonToken.END_ARRAY) {
						row.add(deserializeDisc(jp));
					}
				}
			}
		}

		final Board board = new Board(numCols, numRows);
		for (int r = 0; r < numRows; r++) {
			final List<Disc> rowOfDiscs = rows.get(r);
			for (int c = 0; c < numCols; c++) {
				final Disc disc = rowOfDiscs.get(c);
				if (disc != null) {
					try {
						final int row = board.putDisc(c, disc);
						if (row != r) {
							throw new IOException(
									String.format("The board is invalid. Playing %s at col=%d row=%d produced row=%d", disc, c, r, row));
						}
					} catch (final IllegalMoveException e) {
						throw new IOException("The board is invalid. It contains an illegal move.", e);
					}
				}

			}
		}
		return board;
	}

	public void serialize(final JsonGenerator g, final Disc disc) throws IOException {
		if (disc == null) {
			g.writeNullField("disc");
		} else {
			g.writeStringField("disc", "" + disc.getSymbol());
		}
	}

	/**
	 * Deserialize a {@link Disc}.
	 * @param jp
	 * @return the disc or <code>null</code> if the space is empty (which can be any unrecognized character, typically a period '.')
	 * @throws IOException if the disc text is <code>null</code> or not one character
	 */
	public Disc deserializeDisc(final JsonParser jp) throws IOException {
		final String discText = jp.getText();
		if (discText == null || discText.length() != 1) {
			throw new IOException(String.format("Could not parse disc '%s'.", discText));
		}
		return Disc.getDisc(discText.charAt(0));
	}

	public void serialize(final JsonGenerator g, final RecommendResponse recommendResponse) throws IOException {
		g.writeStartObject();
		if (recommendResponse.getException() != null) {
			serialize(g, recommendResponse.getException());
		} else {
			g.writeNumberField("recommendColumn", recommendResponse.getRecommendColumn());
		}
		serialize(g, recommendResponse.getBoard());
		g.writeEndObject();
	}

	public RecommendRequest deserializeRecommendRequest(final JsonParser jp) throws IOException {
		final RecommendRequest result = new RecommendRequest();
		if (!JsonToken.START_OBJECT.equals(jp.nextToken())) {
			throw new IOException("Could not parse RecommendRequest. Does not appear to be JSON.");
		}
		while (jp.nextToken() == JsonToken.FIELD_NAME) {
			final String fieldname = jp.getCurrentName();
			jp.nextToken();
			if ("currentPlayer".equals(fieldname)) {
				result.setCurrentPlayer(deserializeDisc(jp));
			} else if ("board".equals(fieldname)) {
				result.setBoard(deserializeBoard(jp));
			}
		}
		jp.close();

		return result;
	}

	public void serialize(final JsonGenerator g, final PlayResponse playResponse) throws IOException {
		g.writeStartObject();
		if (playResponse.getException() != null) {
			serialize(g, playResponse.getException());
		}
		serialize(g, playResponse.getState());
		serialize(g, playResponse.getPlayerBoard(), "playerBoard");
		if (playResponse.getPlayerRow() == null) {
			g.writeNullField("playerRow");
		} else {
			g.writeNumberField("playerRow", playResponse.getPlayerRow());
		}
		serialize(g, playResponse.getAiBoard(), "aiBoard");
		if (playResponse.getAiCol() == null) {
			g.writeNullField("aiCol");
		} else {
			g.writeNumberField("aiCol", playResponse.getAiCol());
		}
		if (playResponse.getAiRow() == null) {
			g.writeNullField("aiRow");
		} else {
			g.writeNumberField("aiRow", playResponse.getAiRow());
		}
		g.writeEndObject();
	}

	public void serialize(final JsonGenerator g, final GameState state) throws IOException {
		g.writeStringField("gameState", "" + state.ordinal());
	}

	public PlayRequest deserializePlayRequest(final JsonParser jp) throws IOException {
		final PlayRequest result = new PlayRequest();
		if (!JsonToken.START_OBJECT.equals(jp.nextToken())) {
			throw new IOException("Could not parse PlayRequest. Does not appear to be JSON.");
		}
		while (jp.nextToken() == JsonToken.FIELD_NAME) {
			final String fieldname = jp.getCurrentName();
			jp.nextToken();
			if ("currentPlayer".equals(fieldname)) {
				result.setCurrentPlayer(deserializeDisc(jp));
			} else if ("board".equals(fieldname)) {
				result.setBoard(deserializeBoard(jp));
			} else if ("column".equals(fieldname)) {
				result.setColumn(jp.getIntValue());
			}
		}
		jp.close();

		return result;
	}

	public void serialize(final JsonGenerator g, final GameException exception) throws IOException {
		if (exception == null) {
			g.writeNullField("exception");
		} else {
			g.writeObjectFieldStart("exception");
			g.writeStringField("message", exception.getMessage());
			g.writeStringField("class", exception.getClass().getCanonicalName());
			g.writeStringField("code", exception.getErrorCode().name());
			if (exception instanceof IllegalMoveException) {
				g.writeNumberField("column", ((IllegalMoveException) exception).getColumn());
				serialize(g, ((IllegalMoveException) exception).getDisc());
			}
			g.writeEndObject();
		}
	}

	public void serialize(final JsonGenerator g, final Exception exception) throws IOException {
		if (exception == null) {
			g.writeNullField("exception");
		} else {
			g.writeObjectFieldStart("exception");
			g.writeStringField("message", exception.getMessage());
			g.writeStringField("class", exception.getClass().getCanonicalName());
			g.writeEndObject();
		}
	}

	public JsonGenerator getGenerator(final Writer writer) throws IOException {
		return factory.createGenerator(writer);
	}

	public JsonParser getParser(final InputStream inputStream) throws JsonParseException, IOException {
		return factory.createParser(inputStream);
	}

	public JsonParser getParser(final String text) throws JsonParseException, IOException {
		return factory.createParser(text);
	}

	public static JsonStreamingObjectFactory getInstance() {
		return INSTANCE;
	}
}
