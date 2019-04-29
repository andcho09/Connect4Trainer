package connect4.aws;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import connect4.rest.JsonStreamingObjectFactory;
import connect4.web.GameHandler;
import connect4.web.PlayRequest;
import connect4.web.PlayResponse;
import connect4.web.RecommendRequest;
import connect4.web.RecommendResponse;

public class AwsLambdaServer {

	private final GameHandler gameHandler = new GameHandler();// TODO should these be here?
	private final JsonStreamingObjectFactory factory = JsonStreamingObjectFactory.getInstance();

	public void handle(final InputStream input, final OutputStream output, final Context context) throws IOException {
		final JsonParser parser = factory.getParser(input);
		final RecommendRequest request = factory.deserialiseGenericRequest(parser);
		parser.close();

		final JsonGenerator g = factory.getGenerator(output);
		if (request instanceof PlayRequest) {
			final PlayResponse response = gameHandler.next((PlayRequest) request);
			factory.serialize(g, response);
		} else {
			final RecommendResponse response = gameHandler.recommend(request);
			factory.serialize(g, response);
		}
		g.close();
		output.flush();
	}
}
