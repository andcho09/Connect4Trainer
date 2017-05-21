package connect4.rest;

import static spark.Spark.exception;
import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.post;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import connect4.Board;
import connect4.BoardHelper;
import connect4.Disc;
import connect4.IllegalMoveException;
import connect4.Move;
import connect4.trainer.Trainer;
import connect4.web.GameState;
import connect4.web.PlayRequest;
import connect4.web.PlayResponse;
import connect4.web.ProcessingException;
import connect4.web.Processor;
import connect4.web.RecommendRequest;
import connect4.web.RecommendResponse;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Services REST requests.
 */
public class RestServer {

	private static final Logger LOGGER = Logger.getLogger(RestServer.class);

	public static void main(final String[] args) {
		final Processor processor = new Processor();
		final JsonStreamingObjectFactory factory = JsonStreamingObjectFactory.getInstance();

		externalStaticFileLocation("src/main/webapp");// TODO this won't work if the build a .war/.jar and not safe for WEB-INF

		post("/board/recommend", new Route() {
			@Override
			public Object handle(final Request req, final Response res) throws IOException {
				final JsonParser parser = factory.getParser(req.raw().getInputStream());
				final RecommendRequest request = factory.deserializeRecommendRequest(parser);
				parser.close();

				RecommendResponse response;
				try {
					response = processor.recommend(request);
				} catch (final ProcessingException e) {
					throw new IOException("Can't make a recommendation", e);
				}

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

				final Board board = request.getBoard();
				Disc winner = BoardHelper.hasWinner(board);
				if (winner != null) {
					throw new IOException("Cannot play next move, the game is already won.");
				} else if (board.isFull()) {
					throw new IOException("Cannot play next move, the board is full.");
				}

				final PlayResponse response = new PlayResponse();
				response.setPlayerBoard(board);
				int playerRow;
				try {
					playerRow = board.putDisc(request.getColumn(), request.getCurrentPlayer());
					response.setPlayerRow(playerRow);
				} catch (final IllegalMoveException e) {
					throw new IOException(
							String.format("Could not play %s into column %d", request.getCurrentPlayer(), request.getColumn()), e);
				}

				winner = BoardHelper.hasWinner(board, new Move(request.getCurrentPlayer(), request.getColumn(), playerRow));
				if (request.getCurrentPlayer().equals(winner)) {
					response.setState(GameState.PLAYER_1_WON); // TODO this is weird. Numbers to discs?
				} else if (board.isFull()) {
					response.setState(GameState.DRAW);
				} else {
					final Trainer trainer = new Trainer();
					final Board opponentBoard = new Board(board);
					response.setAiBoard(opponentBoard);
					final Disc opponent = Disc.getOpposite(request.getCurrentPlayer());
					final int aiCol = trainer.recommend(opponentBoard, opponent);
					int aiRow;
					try {
						aiRow = opponentBoard.putDisc(aiCol, opponent);
						response.setAiCol(aiCol);
						response.setAiRow(aiRow);
					} catch (final IllegalMoveException e) {
						throw new IOException(String.format("Opponent %s could not play into column %d", opponent, aiCol), e);
					}

					winner = BoardHelper.hasWinner(opponentBoard, new Move(opponent, aiCol, aiRow));
					if (opponent.equals(winner)) {
						response.setState(GameState.PLAYER_2_WON); // TODO this is weird. Numbers to discs?
					} else if (opponentBoard.isFull()) {
						response.setState(GameState.DRAW);
					} else {
						response.setState(GameState.PLAYER_1_TURN);
					}
				}

				final Writer writer = new StringWriter();
				final JsonGenerator g = factory.getGenerator(writer);
				factory.serialize(g, response);
				g.close();
				return writer.toString();
			}
		});

		exception(IllegalMoveException.class, new ExceptionHandler() {
			@Override
			public void handle(final Exception exception, final Request request, final Response response) {
				response.type("application/json");
				response.status(HttpStatus.BAD_REQUEST_400);
			}
		});
		// Generic exception handler
		exception(Exception.class, new ExceptionHandler() {
			@Override
			public void handle(final Exception exception, final Request request, final Response response) {
				final StringWriter writer = new StringWriter();
				try {
					final JsonGenerator g = factory.getGenerator(writer);
					g.writeStartObject();
					final Throwable nested = exception.getCause();
					if (nested instanceof IllegalMoveException) {
						final IllegalMoveException cause = (IllegalMoveException) exception.getCause();
						response.body("{exceptionClass: '" + cause + "', disc: '" + cause.getDisc().getSymbol() + "'}");
						g.writeStringField("exceptionClass", cause.getClass().getCanonicalName());
						g.writeStringField("disc", "" + cause.getDisc().getSymbol());
						g.writeStringField("message", "" + cause.getMessage());
					} else {
						// Unhandled
						LOGGER.error("Oopps, an unhandled error occurred during processing: " + exception.getClass().getName() + ": "
								+ exception.getMessage(), exception);
						g.writeStringField("exceptionClass", exception.getClass().getCanonicalName());
						g.writeStringField("message", "" + exception.getMessage());
					}
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
