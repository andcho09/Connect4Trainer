package connect4.rest;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import connect4.api.json.JsonStreamingObjectFactory;
import connect4.api.json.WarmRequest;
import connect4.web.GameState;
import connect4.web.PlayRequest;
import connect4.web.PlayResponse;
import connect4.web.RecommendRequest;
import connect4.web.RecommendResponse;

/**
 * Extends {@link JsonStreamingObjectFactory} to handle JSON (de)serialising for web REST requests.
 */
public class WebJsonStreamingObjectFactory extends JsonStreamingObjectFactory {

	private static final WebJsonStreamingObjectFactory INSTANCE = new WebJsonStreamingObjectFactory();

	private WebJsonStreamingObjectFactory() {
	}

	public void serialize(final JsonGenerator g, final RecommendResponse recommendResponse) throws IOException {
		g.writeStartObject();
		if (recommendResponse.getException() != null) {
			serialize(g, recommendResponse.getException());
		} else {
			serialize(g, recommendResponse.getState());
			g.writeNumberField("recommendColumn", recommendResponse.getRecommendColumn());
			g.writeNumberField("recommendRow", recommendResponse.getRecommendRow());
		}
		serialize(g, recommendResponse.getBoard());
		g.writeEndObject();
	}

	public RecommendRequest deserializeRecommendRequest(final JsonParser jp) throws IOException {
		if (!JsonToken.START_OBJECT.equals(jp.nextToken())) {
			throw new IOException("Could not parse RecommendRequest. Does not appear to be JSON.");
		}
		return doDeserializeRecommendRequest(jp);
	}

	private RecommendRequest doDeserializeRecommendRequest(final JsonParser jp) throws IOException {
		final RecommendRequest result = new RecommendRequest();
		while (jp.nextToken() == JsonToken.FIELD_NAME) {
			final String fieldName = jp.getCurrentName();
			jp.nextToken();
			if ("currentPlayer".equals(fieldName)) {
				result.setCurrentPlayer(deserializeDisc(jp));
			} else if ("board".equals(fieldName)) {
				result.setBoard(deserializeBoard(jp));
			}
		}
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

	private void serialize(final JsonGenerator g, final GameState state) throws IOException {
		g.writeStringField("gameState", "" + state.ordinal());
	}

	public PlayRequest deserializePlayRequest(final JsonParser jp) throws IOException {
		if (!JsonToken.START_OBJECT.equals(jp.nextToken())) {
			throw new IOException("Could not parse PlayRequest. Does not appear to be JSON.");
		}
		return doDeserializePlayRequest(jp);
	}

	private PlayRequest doDeserializePlayRequest(final JsonParser jp) throws IOException {
		final PlayRequest result = new PlayRequest();
		while (jp.nextToken() == JsonToken.FIELD_NAME) {
			final String fieldName = jp.getCurrentName();
			jp.nextToken();
			if ("currentPlayer".equals(fieldName)) {
				result.setCurrentPlayer(deserializeDisc(jp));
			} else if ("board".equals(fieldName)) {
				result.setBoard(deserializeBoard(jp));
			} else if ("column".equals(fieldName)) {
				result.setColumn(jp.getIntValue());
			}
		}
		return result;
	}

	public Serializable deserialiseGenericRequest(final JsonParser jp) throws IOException {
		if (!JsonToken.START_OBJECT.equals(jp.nextToken())) {
			throw new IOException("Could not parse request. Does not appear to be JSON.");
		}
		while (jp.nextToken() == JsonToken.FIELD_NAME) {
			final String fieldName = jp.getCurrentName();
			jp.nextToken();
			if ("action".equals(fieldName)) {
				final String action = jp.getValueAsString();
				if ("next".equals(action)) {
					return doDeserializePlayRequest(jp);
				} else if ("recommend".equals(action)) {
					return doDeserializeRecommendRequest(jp);
				} else if ("warm".equals(action)) {
					return new WarmRequest();
				}
			}
		}
		throw new IOException("Could not determine request type. Does not contain an 'action' key. Current parsing location is: "
				+ jp.getTokenLocation().toString());
	}

	public static WebJsonStreamingObjectFactory getInstance() {
		return INSTANCE;
	}
}
