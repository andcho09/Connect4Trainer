package connect4.aws;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import connect4.api.json.WarmRequest;
import connect4.rest.WebJsonStreamingObjectFactory;
import connect4.web.GameHandler;
import connect4.web.PlayRequest;
import connect4.web.PlayResponse;
import connect4.web.RecommendRequest;
import connect4.web.RecommendResponse;

/**
 * Lambda handler for processing JSON REST requests sent by web UI.
 */
public class AwsLambdaTrainerHandler implements RequestStreamHandler {

	private static final String ENV_DEBUG_ENABLED = "DEBUG_ENABLED";
	private static final Logger LOGGER = LogManager.getLogger();
	private static GameHandler gameHandler;

	public AwsLambdaTrainerHandler() {
		final boolean isDebugEnabled = Boolean.valueOf(System.getenv(ENV_DEBUG_ENABLED));
		if (isDebugEnabled) {
			Configurator.setRootLevel(Level.DEBUG);
		}
	}

	@Override
	public void handleRequest(final InputStream input, final OutputStream output, final Context context) throws IOException {
		final long start = System.currentTimeMillis();
		final WebJsonStreamingObjectFactory factory = WebJsonStreamingObjectFactory.getInstance();
		final JsonParser parser = factory.getParser(input);
		final Serializable request = factory.deserialiseGenericRequest(parser);
		parser.close();

		final JsonGenerator g = factory.getGenerator(output);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Parsed request in " + (System.currentTimeMillis() - start) + " ms.");
		}
		if (request instanceof PlayRequest) {
			final PlayResponse response = getHandler().next((PlayRequest) request);
			factory.serialize(g, response);
		} else if (request instanceof RecommendRequest) {
			final RecommendResponse response = getHandler().recommend((RecommendRequest) request);
			factory.serialize(g, response);
		} else if (request instanceof WarmRequest) {
			getHandler().warmUp();
		}
		g.close();
		output.flush();
	}

	private static GameHandler getHandler() {
		if (gameHandler == null) {
			gameHandler = new GameHandler(new LambdaBoardForwarder());
		}
		return gameHandler;
	}
}
