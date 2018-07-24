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
	 * Playing in this column blocks the opponent from opening up two columns to win on their next move
	 */
	public static final int FLAG_BLOCK_TRAP_MORE_THAN_ONE = 1 << 5;

	/**
	 * Playing in this column forces the opponent to play in a space where we'll eventually win
	 */
	public static final int FLAG_FORCED_WIN = 1 << 6;

	/**
	 * Playing in this column prevents the opponent forcing us into a loss
	 */
	public static final int FLAG_BLOCK_FORCED_WIN = 1 << 7;

	/**
	 * Playing in this column creates 3-in-a-row setup which the opponent can't block as there's a gap below the spot that completes the
	 * 4-in-a-row, i.e. someone has to play below that spot first (a potential for {@link #FLAG_ENABLE_OPPONENT_WIN}. This could set up a
	 * win later or at least shut down the column.
	 */
	public static final int FLAG_MAKE_3_SETUP = 1 << 8;

	/**
	 * Playing in this column not only creates 3-in-a-row setup but there's also a 3-in-a-row setup directly below the new setup. This
	 * effectively creates a {@link #FLAG_FORCED_WIN} later on.
	 */
	public static final int FLAG_MAKE_3_DOUBLE_SETUP = 1 << 9;

	/**
	 * Playing here blocks the opponent from making a 3-in-a-row setup.
	 */
	public static final int FLAG_BLOCK_MAKE_3_SETUP = 1 << 10;

	/**
	 * Playing here blocks the opponent from making a 3-in-a-row double setup.
	 */
	public static final int FLAG_BLOCK_MAKE_3_DOUBLE_SETUP = 1 << 11;

	private final int column;
	private int flags;

	public ColumnAnalysis(final int column) {
		this.column = column;
	}

	/**
	 * Copy constructor.
	 * @param analysis the {@link ColumnAnalysis} to copy
	 */
	public ColumnAnalysis(final ColumnAnalysis analysis) {
		this.column = analysis.column;
		this.flags = analysis.flags;
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

	public void removeCondition(final int flag) {
		flags = flags ^ flag;
	}

	public int getColumn() {
		return column;
	}

	@Override
	public String toString() {
		return String.format("ColumnAnalysis (col=%d, flags=%d)", column, flags);
	}

	public String toStringDetail() {
		final StringBuilder detail = new StringBuilder();
		detail.append("\n We will win next move: " + hasCondition(FLAG_WIN_1));
		detail.append("\n Board is unplayable: " + hasCondition(FLAG_UNPLAYABLE));
		detail.append("\n Block opponent winning: " + hasCondition(FLAG_BLOCK_LOSS_1));
		detail.append("\n Enable opponent to win: " + hasCondition(FLAG_ENABLE_OPPONENT_WIN));
		detail.append("\n Set trap to win move after: " + hasCondition(FLAG_TRAP_MORE_THAN_ONE));
		detail.append("\n Block opponents trap: " + hasCondition(FLAG_BLOCK_TRAP_MORE_THAN_ONE));
		detail.append("\n Force opponents move for us to win: " + hasCondition(FLAG_FORCED_WIN));
		detail.append("\n Block opponent forcing our move and losing : " + hasCondition(FLAG_BLOCK_FORCED_WIN));
		detail.append("\n Sets up a 3-in-a-row with a gap below: " + hasCondition(FLAG_MAKE_3_SETUP));
		detail.append("\n Block 3-in-a-row with a gap below: " + hasCondition(FLAG_BLOCK_MAKE_3_SETUP));
		detail.append(
				"\n Block 2x 3-in-a-row stacked ontop of ecah other with a gap below: " + hasCondition(FLAG_BLOCK_MAKE_3_DOUBLE_SETUP));
		return String.format("ColumnAnalysis (col=%d, flags=%d)%s", column, flags, detail.toString());
	}
}
