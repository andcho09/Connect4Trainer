package connect4.api.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.GameException;
import connect4.api.IllegalMoveException;
import connect4.api.analysis.BoardAnalysis;
import connect4.api.analysis.ColumnAnalysis;

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

	private static final JsonStreamingObjectFactory INSTANCE = new JsonStreamingObjectFactory();
	private static final JsonFactory FACTORY = new JsonFactory();

	protected JsonStreamingObjectFactory() {
		// Protected constructor so can only be created by subclasses
	}

	/**
	 * Serialize the board with the default field name of "board". Note this doesn't write start and end object tokens (so the board can be
	 * embedded).
	 * @param g {@link JsonGenerator} representing the output
	 * @param board the {@link Board} to serialise
	 * @throws IOException if the board cannot be serialised
	 */
	public void serialize(final JsonGenerator g, final Board board) throws IOException {
		serialize(g, board, "board");
	}

	/**
	 * Serialise the board with the given field name. Note this doesn't write start and end object tokens (so the board can be embedded).
	 * @param g {@link JsonGenerator} representing the output
	 * @param board the {@link Board} to serialise
	 * @param fieldName the name of the field to serialise the board to
	 * @throws IOException if the board cannot be serialised
	 */
	public void serialize(final JsonGenerator g, final Board board, final String fieldName) throws IOException {
		if (board == null) {
			g.writeNullField(fieldName);
			return;
		}
		g.writeObjectFieldStart(fieldName);
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
			final String fieldName = jp.getCurrentName();
			jp.nextToken();
			if ("numCols".equals(fieldName)) {
				numCols = jp.getIntValue();
			} else if ("numRows".equals(fieldName)) {
				numRows = jp.getIntValue();
			} else if ("rows".equals(fieldName)) {
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

	/**
	 * Serialises the store board request. Note this will start and end the object (so cannot be embedded) and will add the default "action"
	 * field with value "store".
	 * @param g the {@link JsonGenerator} representing the output
	 * @param request the {@link StoreBoardRequest}
	 * @throws IOException if the board request cannot be serialised
	 */
	public void serialize(final JsonGenerator g, final StoreBoardRequest request) throws IOException {
		g.writeStartObject();
		g.writeStringField("action", "store"); // usually we don't write the action but this is serialising a request
		serialize(g, request.getCurrentPlayer());
		serialize(g, request.getBoard());
		serialize(g, request.getBoardAnalysis());
		g.writeEndObject();
	}

	/**
	 * Serialises the store board request with the specified "action" field.
	 * @param g the {@link JsonGenerator} representing the output
	 * @param request the {@link StoreBoardRequest}
	 * @param action the value of the "action" field. If <code>null</code> no "action" field is written and the start and end
	 *        object tokens will not be written to allow embedding.
	 * @throws IOException if the board request cannot be serialised
	 */
	public void serialize(final JsonGenerator g, final StoreBoardRequest request, final String action) throws IOException {
		if (action != null) {
			// usually we don't write the action but this is serialising a request which has been abused to represent a response from
			// DynamoDB too
			g.writeStartObject();
			g.writeStringField("action", "store");
		}
		serialize(g, request.getCurrentPlayer());
		serialize(g, request.getBoard());
		serialize(g, request.getBoardAnalysis());
		if (action != null) {
			g.writeEndObject();
		}
	}

	/**
	 * Serialise board analysis using the default field name "boardAnalysis". Note this doesn't write start and end object tokens (so the
	 * board can be embedded).
	 * @param g the {@link JsonGenerator} representing the output
	 * @param boardAnalysis the {@link BoardAnalysis}
	 * @throws IOException if the board analysis cannot be serialised
	 */
	public void serialize(final JsonGenerator g, final BoardAnalysis boardAnalysis) throws IOException {
		g.writeArrayFieldStart("boardAnalysis");
		for (final ColumnAnalysis columnAnalysis : boardAnalysis) {
			if (columnAnalysis.getFlags() != ColumnAnalysis.FLAG_NO_OPINION) {
				g.writeStartObject();
				g.writeNumberField("col", columnAnalysis.getColumn());
				g.writeNumberField("flags", columnAnalysis.getFlags());
				g.writeEndObject();
			}
		}
		g.writeEndArray();
	}

	public StoreBoardRequest deserializeStoreRequest(final JsonParser jp) throws IOException {
		if (!JsonToken.START_OBJECT.equals(jp.nextToken())) {
			throw new IOException("Could not parse StoreRequest. Does not appear to be JSON.");
		}
		return doDeserializeStoreBoardRequest(jp);
	}

	private StoreBoardRequest doDeserializeStoreBoardRequest(final JsonParser jp) throws IOException {
		final StoreBoardRequest result = new StoreBoardRequest();
		while (jp.nextToken() == JsonToken.FIELD_NAME) {
			final String fieldName = jp.getCurrentName();
			jp.nextToken();
			if ("board".contentEquals(fieldName)) {
				result.setBoard(deserializeBoard(jp));
			} else if ("disc".equals(fieldName)) {
				result.setCurrentPlayer(deserializeDisc(jp));
			} else if ("boardAnalysis".equals(fieldName)) {
				if (result.getBoard() == null) {
					throw new IOException("Could not parse StoreRequest. The 'board' must be defined before 'boardAnalysis'.");
				}
				result.setBoardAnalysis(deserializeBoardAnalysis(jp, result.getBoard().getNumCols()));
			}
		}
		return result;
	}

	public BoardAnalysis deserializeBoardAnalysis(final JsonParser jp, final int maxColumns) throws IOException {
		final BoardAnalysis boardAnalysis = new BoardAnalysis();
		while (jp.nextToken() != JsonToken.END_OBJECT) {
			final TreeMap<Integer, Integer> columnToFlags = new TreeMap<>();
			while (jp.nextToken() != JsonToken.END_ARRAY) {
				int column = -1;
				int flags = -1;
				while (jp.nextToken() != JsonToken.END_OBJECT) {
					final String fieldName = jp.getCurrentName();
					if ("col".equals(fieldName)) {
						column = jp.getValueAsInt();
					} else if ("flags".equals(fieldName)) {
						flags = jp.getValueAsInt();
					}
				}
				columnToFlags.put(column, flags);
			}
			for (int i = 0; i < maxColumns; i++) {
				final ColumnAnalysis columnAnalysis = new ColumnAnalysis(i);
				if (columnToFlags.containsKey(i)) {
					columnAnalysis.setFlags(columnToFlags.get(i));
				}
				boardAnalysis.add(columnAnalysis);
			}
		}

		return boardAnalysis;
	}

	public AbstractBoardRequest deserializeAbstractBoardRequest(final JsonParser jp) throws IOException {
		if (!JsonToken.START_OBJECT.equals(jp.nextToken())) {
			throw new IOException("Could not parse request. Does not appear to be JSON.");
		}
		while (jp.nextToken() == JsonToken.FIELD_NAME) {
			final String fieldName = jp.getCurrentName();
			jp.nextToken();
			if ("action".equals(fieldName)) {
				final String action = jp.getValueAsString();
				if ("store".equals(action)) {
					return doDeserializeStoreBoardRequest(jp);
				} else if ("getrandom".equals(action)) {
					return new GetRandomBoardRequest();
				}
			}
		}
		throw new IOException("Could not determine request type. Does not contain an 'action' key. Current parsing location is: "
				+ jp.getTokenLocation().toString());
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

	public JsonGenerator getGenerator(final Writer writer) throws IOException {
		return JsonStreamingObjectFactory.FACTORY.createGenerator(writer);
	}

	public JsonGenerator getGenerator(final OutputStream outputStream) throws IOException {
		return JsonStreamingObjectFactory.FACTORY.createGenerator(outputStream);
	}

	public JsonParser getParser(final InputStream inputStream) throws JsonParseException, IOException {
		return JsonStreamingObjectFactory.FACTORY.createParser(inputStream);
	}

	public JsonParser getParser(final String text) throws JsonParseException, IOException {
		return JsonStreamingObjectFactory.FACTORY.createParser(text);
	}

	public static JsonStreamingObjectFactory getInstance() {
		return INSTANCE;
	}
}
