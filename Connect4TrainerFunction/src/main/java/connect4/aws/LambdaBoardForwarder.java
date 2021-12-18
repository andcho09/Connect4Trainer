package connect4.aws;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.fasterxml.jackson.core.JsonGenerator;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.analysis.BoardAnalysis;
import connect4.api.json.JsonStreamingObjectFactory;
import connect4.api.json.StoreBoardRequest;
import connect4.forwarder.AbstractBoardForwarder;

/**
 * Forwards boards to a Lambda function asynchronously.
 */
public class LambdaBoardForwarder extends AbstractBoardForwarder {

	public static final String ENV_LAMBDA_REGION = "STORE_LAMBDA_REGION";
	public static final String ENV_LAMBDA_FUNCTION = "STORE_LAMBDA_FUNCTION";

	private static final Logger LOGGER = LogManager.getLogger();

	private static AWSLambdaAsync lambda;
	private final String lambdaFunction;
	private final String lambdaRegion;

	public LambdaBoardForwarder() {
		this.lambdaRegion = System.getenv(ENV_LAMBDA_REGION);
		if (StringUtils.isEmpty(this.lambdaRegion)) {
			throw new IllegalArgumentException("The system environment variable '" + ENV_LAMBDA_REGION + "' is empty but is required");
		}
		final String envlambdaFunction = System.getenv(ENV_LAMBDA_FUNCTION);
		if (StringUtils.isEmpty(envlambdaFunction)) {
			throw new IllegalArgumentException("The system environment variable '" + ENV_LAMBDA_FUNCTION + "' is empty but is required");
		} else {
			if (envlambdaFunction.trim().endsWith(":live")) { // Make sure we use the "live" alias
				this.lambdaFunction = envlambdaFunction.trim();
			} else {
				this.lambdaFunction = envlambdaFunction.trim() + ":live";
			}
		}
	}

	@Override
	protected void forward(final Disc currentPlayer, final Board board, final BoardAnalysis boardAnalysis) {
		final StoreBoardRequest boardRequest = new StoreBoardRequest();
		boardRequest.setBoard(board);
		boardRequest.setBoardAnalysis(boardAnalysis);
		boardRequest.setCurrentPlayer(currentPlayer);

		final JsonStreamingObjectFactory factory = JsonStreamingObjectFactory.getInstance();
		final StringWriter writer = new StringWriter();
		try {
			final JsonGenerator g = factory.getGenerator(writer);
			factory.serialize(g, boardRequest);
			g.close();
		} catch (final IOException e) {
			throw new RuntimeException("Couldn't serailase StoreBoardRequest to JSON for sending to Lambda", e);
		}

		final InvokeRequest request = new InvokeRequest();
		request.setFunctionName(this.lambdaFunction);
		request.setInvocationType(InvocationType.Event); // Event means aysnc
		request.setPayload(writer.toString());
		getLambdaClient(this.lambdaRegion).invoke(request);
		LOGGER.debug("Sent board to Lambda function asynchronously");
	}

	private static synchronized AWSLambdaAsync getLambdaClient(final String region) {
		if (lambda == null) {
			final long start = System.currentTimeMillis();
			lambda = AWSLambdaAsyncClientBuilder.standard().withRegion(region).build();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Initialised Lambda client in " + (System.currentTimeMillis() - start) + " ms.");
			}
		}
		return lambda;
	}

	@Override
	public void warmUp() {
		LOGGER.debug("Warming up Lambda async client");
		final InvokeRequest request = new InvokeRequest();
		request.setFunctionName(this.lambdaFunction);
		request.setInvocationType(InvocationType.Event); // Event means aysnc
		request.setPayload("{\"action\":\"warm\"}");
		getLambdaClient(this.lambdaRegion).invoke(request);
	}
}
