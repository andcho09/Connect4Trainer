package connect4.api.analysis;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for a bunch of {@link ColumnAnalysis}. Note, the number of elements does not necessarily correlate to the number of columns the
 * board has.
 */
public class BoardAnalysis extends AbstractList<ColumnAnalysis> {

	private final List<ColumnAnalysis> columnAnalyses;

	public BoardAnalysis() {
		this.columnAnalyses = new ArrayList<>();
	}

	/**
	 * Applies the flag to the {@link ColumnAnalysis} indicated by the given column.
	 * @param column the column to apply the flag to
	 * @param flag the flag to apply to the original list
	 */
	public void apply(final Integer column, final int flag) {
		if (column == null) {
			return;
		}
		for (final ColumnAnalysis original : this.columnAnalyses) {
			if (column == original.getColumn()) {
				original.addCondition(flag);
				return;
			}
		}
	}

	/**
	 * Get columns with any of the following conditions
	 * @param flag the condition to return for
	 * @return a {@link List} of {@link ColumnAnalysis} which have the flag. Could be empty
	 */
	public BoardAnalysis getColumnsWithConditions(final Integer... flags) {
		final BoardAnalysis result = new BoardAnalysis();
		for (final ColumnAnalysis columnAnalysis : this.columnAnalyses) {
			for (final Integer flag : flags) {
				if (columnAnalysis.hasCondition(flag)) {
					result.add(columnAnalysis);
					break;
				}
			}

		}
		return result;
	}

	@Override
	public boolean add(final ColumnAnalysis analysis) {
		return this.columnAnalyses.add(analysis);
	}

	@Override
	public void add(final int index, final ColumnAnalysis analysis) {
		this.columnAnalyses.add(index, analysis);
	}

	/**
	 * Retrieves the analysis at position index in the list. Could throw {@link IndexOutOfBoundsException}.
	 */
	@Override
	public ColumnAnalysis get(final int index) {
		return this.columnAnalyses.get(index);
	}

	/**
	 * Retrieves the {@link ColumnAnalysis} for the specified column.
	 * @param column the column (0-based)
	 * @return the {@link ColumnAnalysis} or <code>null</code> if there's no such analysis
	 */
	public ColumnAnalysis getAnalysisAtColumn(final int column) {
		for (final ColumnAnalysis columnAnalysis : this.columnAnalyses) {
			if (column == columnAnalysis.getColumn()) {
				return columnAnalysis;
			}
		}
		return null;
	}

	/**
	 * Reverses the columns within the {@link BoardAnalysis}.
	 */
	public void reverse() {
		for (int i = 0; i < this.columnAnalyses.size() / 2; i++) {
			final int rightIndex = this.columnAnalyses.size() - 1 - i;
			final ColumnAnalysis temp = this.columnAnalyses.get(i);
			this.columnAnalyses.set(i, swapColumns(this.columnAnalyses.get(rightIndex), i));
			this.columnAnalyses.set(rightIndex, swapColumns(temp, rightIndex));
		}
	}

	private ColumnAnalysis swapColumns(final ColumnAnalysis columnAnalysis, final int newColumn) {
		final ColumnAnalysis result = new ColumnAnalysis(newColumn);
		result.setFlags(columnAnalysis.getFlags());
		return result;
	}

	@Override
	public int size() {
		return this.columnAnalyses.size();
	}

	@Override
	public void clear() {
		this.columnAnalyses.clear();
	}
}
