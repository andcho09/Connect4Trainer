package connect4.store.lambda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import connect4.api.aws.xray.AWSXRay;
import connect4.api.json.GetRandomBoardRequest;
import connect4.api.json.JsonStreamingObjectFactory;
import connect4.api.json.StoreBoardRequest;
import connect4.api.json.WarmRequest;
import connect4.store.dynamodb.DynamoDbStore;

/**
 * Receives {@link StoreBoardRequest} (JSON) and saves them to DynamoDB.
 */
public class AwsLambdaStoreHandler implements RequestStreamHandler {

	private static final String ENV_DEBUG_ENABLED = "DEBUG_ENABLED";
	private static final Logger LOGGER = LogManager.getLogger();

	public AwsLambdaStoreHandler() {
		final boolean isDebugEnabled = Boolean.valueOf(System.getenv(ENV_DEBUG_ENABLED));
		if (isDebugEnabled) {
			Configurator.setRootLevel(Level.DEBUG);
		}
	}

	@Override
	public void handleRequest(final InputStream input, final OutputStream output, final Context context) throws IOException {
		final JsonStreamingObjectFactory factory = JsonStreamingObjectFactory.getInstance();
		final JsonGenerator g = factory.getGenerator(output);
		final JsonParser jp = factory.getParser(input);
		final Serializable request = factory.deserializeAbstractBoardRequest(jp);
		g.writeStartObject();
		if (request instanceof StoreBoardRequest) {
			final StoreBoardRequest boardRequest = (StoreBoardRequest) request;
			if (LOGGER.isDebugEnabled()) {
				final String boardAnalysisString = boardRequest.getBoardAnalysis().stream()
						.map(n -> n.getColumn() + 1 + " (column[" + n.getColumn() + "])").collect(Collectors.joining(", "));
				LOGGER.debug(String.format("Handling store request for board = %s, board analysis = %s and current player = %s",
						boardRequest.getBoard(), boardAnalysisString, boardRequest.getCurrentPlayer()));
			}
			handle(boardRequest);
			g.writeStringField("status", "received");
		} else if (request instanceof GetRandomBoardRequest) {
			LOGGER.debug("Handling get random board request");
			final StoreBoardRequest randomBoard = getRandomBoard();
			g.writeStringField("status", randomBoard == null ? "empty" : "retrieved");
			factory.serialize(g, randomBoard, null);
		} else if (request instanceof WarmRequest) {
			final long start = System.currentTimeMillis();
			getRandomBoard();
			LOGGER.debug("Warm up completed in " + (System.currentTimeMillis() - start) + " ms.");
		}
		g.writeEndObject();
		g.close();
		output.flush();
		LOGGER.debug("Handling request done");
	}

	public void handle(final StoreBoardRequest request) {
		AWSXRay.createSubsegment("store", (subsegment) -> {
			DynamoDbStore.getInstance().createOrUpdate(request);
		});
	}

	public StoreBoardRequest getRandomBoard() {
		return AWSXRay.createSubsegment("getrandom", (subsegment) -> {
			return DynamoDbStore.getInstance().getRandom();
		});
	}
}
