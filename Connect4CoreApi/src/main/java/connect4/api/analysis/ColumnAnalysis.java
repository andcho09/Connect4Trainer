package connect4.api.analysis;

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

	/**
	 * Bottom center column is free.
	 */
	public static final int FLAG_BOTTOM_CENTER_FREE = 1 << 12;

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
		return this.flags;
	}

	/**
	 * Sets multiple conditions
	 * @param flags the flags representing conditions
	 * @see #addCondition(int)
	 */
	public void setFlags(final int flags) {
		this.flags = flags;
	}

	public boolean hasCondition(final int flag) {
		return (this.flags & flag) == flag;
	}

	/**
	 * Checks if any of the flags are present.
	 * @param flag XOR'ed flags to check
	 * @return true if any of the conditions are present, else false
	 */
	public boolean hasConditions(final int flag) {
		return (this.flags & flag) > 0;
	}

	public void addCondition(final int flag) {
		this.flags = this.flags | flag;
	}

	public void removeCondition(final int flag) {
		this.flags = this.flags ^ flag;
	}

	public int getColumn() {
		return this.column;
	}

	@Override
	public String toString() {
		return String.format("ColumnAnalysis (col=%d, flags=%d)", this.column, this.flags);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof ColumnAnalysis) {
			final ColumnAnalysis other = (ColumnAnalysis) obj;
			return this.column == other.column && this.flags == other.flags;
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.column;
		result = prime * result + this.flags;
		return result;
	}

	public String toStringDetail() {
		final StringBuilder detail = new StringBuilder();
		detail.append(hasCondition(FLAG_WIN_1) ? "\n We will win next move" : "");
		detail.append(hasCondition(FLAG_UNPLAYABLE) ? "\n Column is unplayable" : "");
		detail.append(hasCondition(FLAG_BLOCK_LOSS_1) ? "\n Block opponent winning" : "");
		detail.append(hasCondition(FLAG_ENABLE_OPPONENT_WIN) ? "\n Enable opponent to win" : "");
		detail.append(hasCondition(FLAG_TRAP_MORE_THAN_ONE) ? "\n Set trap to win move after" : "");
		detail.append(hasCondition(FLAG_BLOCK_TRAP_MORE_THAN_ONE) ? "\n Block opponents trap" : "");
		detail.append(hasCondition(FLAG_FORCED_WIN) ? "\n Force opponents move for us to win" : "");
		detail.append(hasCondition(FLAG_BLOCK_FORCED_WIN) ? "\n Block opponent forcing our move and losing" : "");
		detail.append(hasCondition(FLAG_MAKE_3_SETUP) ? "\n Sets up a 3-in-a-row with a gap below" : "");
		detail.append(hasCondition(FLAG_MAKE_3_DOUBLE_SETUP) ? "\n Sets up a 2x 3-in-a-row ontop of each other with a gap below" : "");
		detail.append(hasCondition(FLAG_BLOCK_MAKE_3_SETUP) ? "\n Block 3-in-a-row with a gap below" : "");
		detail.append(
				hasCondition(FLAG_BLOCK_MAKE_3_DOUBLE_SETUP) ? "\n Block 2x 3-in-a-row stacked ontop of ecah other with a gap below" : "");
		detail.append(hasCondition(FLAG_BOTTOM_CENTER_FREE) ? "\n Bottom center column is free" : "");
		return String.format("ColumnAnalysis (col=%d, flags=%d)%s", this.column, this.flags, detail.toString());
	}
}
