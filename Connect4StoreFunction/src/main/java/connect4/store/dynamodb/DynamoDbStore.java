package connect4.store.dynamodb;

import static connect4.store.dynamodb.BoardItemHelper.BOARD_ANALYSIS_CONVERTER;
import static connect4.store.dynamodb.BoardItemHelper.BOARD_CONVERTER;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.api.analysis.BoardAnalysis;
import connect4.api.json.StoreBoardRequest;

/**
 * Application-level class for accessing Connect 4 DynamoDB. Uses the environment variable {@value #ENV_REGION} to be set the
 * region code where DynamoDB lives (or if not set, uses the default region).
 */
public class DynamoDbStore {

	private static final Logger LOGGER = Logger.getLogger(DynamoDbStore.class);
	private static final Random RANDOM = new Random();
	private static final String ENV_REGION = "DYNAMODB_REGION";
	private static DynamoDbStore INSTANCE;

	private final DynamoDB dynamoDb;

	private DynamoDbStore() {
		final String region = System.getenv(ENV_REGION);
		final AmazonDynamoDB dynamoDbClient;
		if (StringUtils.isBlank(region)) {
			LOGGER.debug("Initialising using default region");
			dynamoDbClient = AmazonDynamoDBClientBuilder.defaultClient();
		} else {
			dynamoDbClient = AmazonDynamoDBClientBuilder.standard().withRegion(region).build();
		}
		this.dynamoDb = new DynamoDB(dynamoDbClient);
	}

	/**
	 * Stores a {@link StoreBoardRequest} in DynamoDB. This is dumb storage. There is no board normalisation and it's assumed the current
	 * player is {@link Disc#YELLOW}
	 * @param request
	 */
	public void createOrUpdate(final StoreBoardRequest request) {
		if (!Disc.YELLOW.equals(request.getCurrentPlayer())) {
			throw new IllegalArgumentException("The current player is expected to be YELLOW but was " + request.getCurrentPlayer());
		}

		final Map<String, String> expressionAttributeNames = new HashMap<>();
		expressionAttributeNames.put("#bo", BoardItemHelper.ATTR_BOARD);
		expressionAttributeNames.put("#an", BoardItemHelper.ATTR_BOARD_ANALYSIS);
		expressionAttributeNames.put("#sc", BoardItemHelper.ATTR_SEEN_COUNT);

		final Map<String, Object> expressionAttributeValues = new HashMap<>();
		expressionAttributeValues.put(":val1", BOARD_CONVERTER.convert(request.getBoard()));
		expressionAttributeValues.put(":val2", BOARD_ANALYSIS_CONVERTER.convert(request.getBoardAnalysis()));
		expressionAttributeValues.put(":val3", 1);
		expressionAttributeValues.put(":zero", 0);

		final Table table = this.dynamoDb.getTable(BoardItemHelper.TABLE);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Create or updating board with hashcode " + request.getBoard().hashCode());
		}
		final UpdateItemOutcome updateItemOutcome = table.updateItem(BoardItemHelper.KEY_HASH, BoardItemHelper.KEY_HASH_VALUE,
				BoardItemHelper.KEY_RANGE, request.getBoard().hashCode(),
				"set #bo = :val1, #an = :val2, #sc = if_not_exists(#sc, :zero) + :val3", expressionAttributeNames,
				expressionAttributeValues);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Completed creating or updating board with hashcode " + request.getBoard().hashCode() + " with request ID "
					+ updateItemOutcome.getUpdateItemResult().getSdkResponseMetadata().getRequestId());
		}
	}

	/**
	 * Retrieve a board from DynamoDB.
	 * @param boardHashCode the hash code of the board
	 * @return {@link StoreBoardRequest} containing the {@link Board} and {@link BoardAnalysis}. Could be <code>null</code> if doesn't
	 *         exist.
	 */
	public StoreBoardRequest get(final int boardHashCode) {
		final Table table = this.dynamoDb.getTable(BoardItemHelper.TABLE);
		final Item item = table.getItem(BoardItemHelper.KEY_HASH, BoardItemHelper.KEY_HASH_VALUE, BoardItemHelper.KEY_RANGE, boardHashCode);
		if (item == null) {
			return null;
		}
		final StoreBoardRequest response = new StoreBoardRequest();
		response.setBoard(BOARD_CONVERTER.unconvert(item.getString(BoardItemHelper.ATTR_BOARD)));
		response.setBoardAnalysis(BOARD_ANALYSIS_CONVERTER.unconvert(item.getString(BoardItemHelper.ATTR_BOARD_ANALYSIS)));
		response.setCurrentPlayer(Disc.YELLOW);
		return response;
	}

	/**
	 * Retrieve a random board from DynamoDB.
	 * @return {@link StoreBoardRequest} containing the {@link Board} and {@link BoardAnalysis} or <code>null</code> if no board could be
	 *         found
	 */
	public StoreBoardRequest getRandom() {
		final int randomHashCode = RANDOM.nextInt();
		final Table table = this.dynamoDb.getTable(BoardItemHelper.TABLE);
		LOGGER.debug("Getting random board with hashcode <= " + randomHashCode);
		QuerySpec query = buildQuery(randomHashCode, false);
		ItemCollection<QueryOutcome> queryOutcomes = table.query(query);
		QueryOutcome queryOutcome = queryOutcomes.firstPage().getLowLevelResult(); // This line actually fires the query
		if (queryOutcomes.getAccumulatedItemCount() == 0) {
			// Didn't find anything, this could be edge case where the random hash code is smaller than all of the board hash codes in
			// DynamoDB. Try again with greater than
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Hmm <= didn't work for request ID " + queryOutcome.getQueryResult().getSdkResponseMetadata().getRequestId()
						+ ". Trying with hashcode >= " + randomHashCode);
			}
			query = buildQuery(randomHashCode, true);
			queryOutcomes = table.query(query);
			queryOutcome = queryOutcomes.firstPage().getLowLevelResult();
		}

		if (queryOutcomes.getAccumulatedItemCount() == 1) {
			final StoreBoardRequest response = new StoreBoardRequest();
			final Item item = queryOutcome.getItems().get(0);
			response.setBoard(BOARD_CONVERTER.unconvert(item.getString(BoardItemHelper.ATTR_BOARD)));
			response.setBoardAnalysis(BOARD_ANALYSIS_CONVERTER.unconvert(item.getString(BoardItemHelper.ATTR_BOARD_ANALYSIS)));
			response.setCurrentPlayer(Disc.YELLOW);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Returning board with hashcode " + response.getBoard().hashCode() + " from request ID "
						+ queryOutcomes.getLastLowLevelResult().getQueryResult().getSdkResponseMetadata().getRequestId());
			}
			return response;
		} else if (queryOutcomes.getAccumulatedItemCount() == 0) {
			LOGGER.debug("No boards found, table must be empty");
			return null;
		} else {
			throw new RuntimeException("Query for random board returned more than one row. This shouldn't be possible.");
		}
	}

	private QuerySpec buildQuery(final int randomHashCode, final boolean useLessThan) {
		final RangeKeyCondition rangeKeyCondition = new RangeKeyCondition(BoardItemHelper.KEY_RANGE);
		if (useLessThan) {
			rangeKeyCondition.le(randomHashCode);
		} else {
			rangeKeyCondition.ge(randomHashCode); // need to use >= first otherwise <= will always pick the first row
		}
		// page size 5 should result in <4KB being queried
		return new QuerySpec().withHashKey(BoardItemHelper.KEY_HASH, 0).withRangeKeyCondition(rangeKeyCondition).withMaxResultSize(1)
				.withMaxPageSize(5);// .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL); // we could return consumed capacity here
	}

	public static synchronized DynamoDbStore getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new DynamoDbStore();
		}
		return INSTANCE;
	}
}
