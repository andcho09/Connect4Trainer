package connect4.forwarder;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.analysis.BoardAnalysis;
import connect4.api.json.StoreBoardRequest;
import connect4.store.lambda.AwsLambdaStoreHandler;

/**
 * Forwards to {@link AwsStoreHandlerForwarder} directly (i.e. not via Lambda). This is intended for local testing only but will actually
 * store in DynamoDb.
 */
public class AwsStoreHandlerForwarder extends AbstractBoardForwarder {

	private final AwsLambdaStoreHandler handler = new AwsLambdaStoreHandler();

	@Override
	protected void forward(final Disc currentPlayer, final Board board, final BoardAnalysis boardAnalysis) {
		final StoreBoardRequest request = new StoreBoardRequest();
		request.setBoard(board);
		request.setBoardAnalysis(boardAnalysis);
		request.setCurrentPlayer(currentPlayer);
		this.handler.handle(request);
	}
}
