package connect4.trainer;

/**
 * Flags a column with a bunch of facts/analysis. Doesn't do scoring. Just facts.
 */
public class ColumnAnalysis {

	/**
	 * No idea.
	 */
	public static final int FLAG_NO_OPINION = 0;
	/**
	 * Current player will win in next move
	 */
	public static final int FLAG_WIN_1 = 1 << 0;
	/**
	 * The column can't be played, i.e. it's full
	 */
	public static final int FLAG_UNPLAYABLE = 1 << 1;

	/**
	 * Playing in this column will block the opponent from winning in their next move
	 */
	public static final int FLAG_BLOCK_LOSS_1 = 1 << 2;

	/**
	 * Playing in this column allows the opponent to win by playing ontop
	 */
	public static final int FLAG_ENABLE_OPPONENT_WIN = 1 << 3;

	/**
	 * Playing in this column opens up two columns to win on the next move
	 */
	public static final int FLAG_TRAP_MORE_THAN_ONE = 1 << 4;

	/**
	 * Playing in this column opens up two columns to win on the next move
	 */
	public static final int FLAG_BLOCK_TRAP_MORE_THAN_ONE = 1 << 5;

	private final int column;
	private int flags;

	public ColumnAnalysis(final int column) {
		this.column = column;
	}

	/**
	 * @return the flags that have been set
	 */
	public int getFlags() {
		return flags;
	}

	public boolean hasCondition(final int flag) {
		return (flags & flag) == flag;
	}

	public void addCondition(final int flag) {
		flags = flags | flag;
	}

	public int getColumn() {
		return column;
	}
}
