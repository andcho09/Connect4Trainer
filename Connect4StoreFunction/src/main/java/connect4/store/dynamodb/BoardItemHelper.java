package connect4.store.dynamodb;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.analysis.BoardAnalysis;
import connect4.api.json.JsonStreamingObjectFactory;

/**
 * <p>
 * Helper class for modeling interesting {@link Board} objects (with {@link BoardAnalysis} and current player as a {@link Disc}) in
 * DynamoDB.
 * <p>
 * Note this model uses a fixed partition/hash key which is essentially a hack to allow selection of random rows by running a query where
 * the range key (hash of the board) is <= (or >=) to a random number. This does create a limitation where we are restricted to one
 * partition (10GB size, 3000 RCU and 1000 WCU).
 */
public class BoardItemHelper {

	public static final String TABLE = "BoardItem";
	public static final String KEY_HASH = "hack";
	public static final Integer KEY_HASH_VALUE = Integer.valueOf(0);
	public static final String KEY_RANGE = "boardhash";
	public static final String ATTR_BOARD = "board";
	public static final String ATTR_BOARD_ANALYSIS = "analysis";
	public static final String ATTR_SEEN_COUNT = "seen";

	private static final int DEFAULT_NUM_COLUMNS = 7;

	// TODO this should probably be pushed down into JsonStreamingObjectFactory
	public static abstract class DynamoDbConverter<T> {

		private final String type;

		private DynamoDbConverter(final String type) {
			this.type = type;
		}

		public String convert(final T t) {
			final JsonStreamingObjectFactory factory = JsonStreamingObjectFactory.getInstance();
			final StringWriter writer = new StringWriter();
			try {
				final JsonGenerator g = factory.getGenerator(writer);
				g.writeStartObject();
				serialise(factory, g, t);
				g.writeEndObject();
				g.flush();
			} catch (final IOException e) {
				throw new RuntimeException("Could not serialise " + this.type + " for storing in DynamoDB", e);
			}
			return writer.toString();
		}

		abstract void serialise(final JsonStreamingObjectFactory factory, final JsonGenerator g, final T t) throws IOException;

		public T unconvert(final String s) {
			final JsonStreamingObjectFactory factory = JsonStreamingObjectFactory.getInstance();
			try {
				final JsonParser parser = factory.getParser(s);
				return deserialise(factory, parser);
			} catch (final IOException e) {
				throw new RuntimeException("Could not deserialise " + this.type + "'" + s + "' from DynamoDB", e);
			}
		}

		abstract T deserialise(final JsonStreamingObjectFactory factory, final JsonParser parser) throws IOException;
	}

	public static final DynamoDbConverter<Board> BOARD_CONVERTER = new DynamoDbConverter<Board>("Board") {

		@Override
		void serialise(final JsonStreamingObjectFactory factory, final JsonGenerator g, final Board t) throws IOException {
			factory.serialize(g, t);
		}

		@Override
		Board deserialise(final JsonStreamingObjectFactory factory, final JsonParser parser) throws IOException {
			parser.nextToken();
			return factory.deserializeBoard(parser);
		}
	};

	public static final DynamoDbConverter<BoardAnalysis> BOARD_ANALYSIS_CONVERTER = new DynamoDbConverter<BoardAnalysis>("BoardAnalysis") {
		@Override
		void serialise(final JsonStreamingObjectFactory factory, final JsonGenerator g, final BoardAnalysis t) throws IOException {
			factory.serialize(g, t);
		}

		@Override
		BoardAnalysis deserialise(final JsonStreamingObjectFactory factory, final JsonParser parser) throws IOException {
			return factory.deserializeBoardAnalysis(parser, DEFAULT_NUM_COLUMNS);
		}
	};

	public static final DynamoDbConverter<Disc> DISC_CONVERTER = new DynamoDbConverter<Disc>("Disc") {

		@Override
		public String convert(final Disc disc) {
			return "" + disc.getSymbol();
		}

		@Override
		public Disc unconvert(final String s) {
			if (s != null && s.length() > 0) {
				return Disc.getDisc(s.charAt(0));
			}
			throw new RuntimeException("Could not deserialise Disc '" + s + "' from DynamoDB");
		}

		@Override
		void serialise(final JsonStreamingObjectFactory factory, final JsonGenerator g, final Disc t) throws IOException {
			// Unused
		}

		@Override
		Disc deserialise(final JsonStreamingObjectFactory factory, final JsonParser parser) {
			// Unused
			return null;
		}
	};

	public static String convertDisc(final Disc disc) {
		return "" + disc.getSymbol();
	}

	public static Disc unconvertDisc(final String object) {
		if (object != null && object.length() > 0) {
			return Disc.getDisc(object.charAt(0));
		}
		throw new RuntimeException("Could not deserialise Disc '" + object + "' from DynamoDB");
	}
}
