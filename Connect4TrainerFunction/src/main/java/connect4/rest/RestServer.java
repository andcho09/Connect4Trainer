package connect4.rest;

import static spark.Spark.exception;
import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.post;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import connect4.forwarder.AwsStoreHandlerForwarder;
import connect4.web.GameHandler;
import connect4.web.PlayRequest;
import connect4.web.PlayResponse;
import connect4.web.RecommendRequest;
import connect4.web.RecommendResponse;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Services REST requests encoded in JSON. Only used when run from Eclipse. See Lambda handlers for invocations into AWS.
 */
public class RestServer {

	private static final Logger LOGGER = LogManager.getLogger();

	public static void main(final String[] args) {
		final GameHandler gameHandler = new GameHandler(new AwsStoreHandlerForwarder());
		final WebJsonStreamingObjectFactory factory = WebJsonStreamingObjectFactory.getInstance();

		externalStaticFileLocation("src/main/webapp");

		post("/board/recommend", new Route() {
			@Override
			public Object handle(final Request req, final Response res) throws IOException {
				final JsonParser parser = factory.getParser(req.raw().getInputStream());
				final RecommendRequest request = factory.deserializeRecommendRequest(parser);
				parser.close();

				final RecommendResponse response = gameHandler.recommend(request);

				final Writer writer = new StringWriter();
				final JsonGenerator g = factory.getGenerator(writer);
				factory.serialize(g, response);
				g.close();
				return writer.toString();
			}
		});

		/**
		 * Processes the players move returning the new board and also the AI's move and the resulting board.
		 */
		post("/game/next", new Route() {
			@Override
			public Object handle(final Request req, final Response res) throws IOException {
				final JsonParser parser = factory.getParser(req.raw().getInputStream());
				final PlayRequest request = factory.deserializePlayRequest(parser);
				parser.close();

				final PlayResponse response;
				response = gameHandler.next(request);

				final Writer writer = new StringWriter();
				final JsonGenerator g = factory.getGenerator(writer);
				factory.serialize(g, response);
				g.close();
				return writer.toString();
			}
		});

		post("/game/play", new Route() {
			@Override
			public Object handle(final Request req, final Response res) throws Exception {

				final JsonParser parser = factory.getParser(req.raw().getInputStream());
				final Serializable genericRequest = factory.deserialiseGenericRequest(parser);
				parser.close();

				final Writer writer = new StringWriter();
				final JsonGenerator g = factory.getGenerator(writer);

				if (genericRequest instanceof PlayRequest) {
					final PlayResponse response = gameHandler.next((PlayRequest) genericRequest);
					factory.serialize(g, response);
				} else if (genericRequest instanceof RecommendRequest) {
					final RecommendResponse response = gameHandler.recommend((RecommendRequest) genericRequest);
					factory.serialize(g, response);
				}
				g.close();
				return writer.toString();
			}
		});

		// Generic exception handler
		exception(Exception.class, new ExceptionHandler<>() {
			@Override
			public void handle(final Exception exception, final Request request, final Response response) {
				final StringWriter writer = new StringWriter();
				try {
					LOGGER.error("Oopps, an unhandled error occurred during processing: " + exception.getClass().getName() + ": "
							+ exception.getMessage(), exception);
					final JsonGenerator g = factory.getGenerator(writer);
					g.writeStartObject();
					g.writeStringField("exceptionClass", exception.getClass().getCanonicalName());
					g.writeStringField("message", "" + exception.getMessage());
					g.writeEndObject();
					g.close();
				} catch (final IOException e) {
					// Error occurred inside exception handler
					LOGGER.error("An error occurred in the error handling '" + e.getMessage() + "' while trying to process error '"
							+ exception.getMessage() + "'", e);
					response.body("{message: 'An error occurred in the exception handler'}");
					response.type("application/json");
					response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
					return;
				}
				writer.flush();
				response.body(writer.toString());
				response.type("application/json");
				response.status(HttpStatus.BAD_REQUEST_400);
			}
		});
	}
}
