package connect4.trainer;

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
		this.columnAnalyses = new ArrayList<ColumnAnalysis>();

	}

	public BoardAnalysis(final List<ColumnAnalysis> columnAnalyses) {
		this.columnAnalyses = columnAnalyses;
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
		for (final ColumnAnalysis original : columnAnalyses) {
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
	public ColumnAnalysis get(final int index) {
		return columnAnalyses.get(index);
	}

	/**
	 * Retrieves the {@link ColumnAnalysis} for the specified column.
	 * @param column the column (0-based)
	 * @return the {@link ColumnAnalysis} or <code>null</code> if there's no such analysis
	 */
	public ColumnAnalysis getAnalysisAtColumn(final int column) {
		for (final ColumnAnalysis columnAnalysis : columnAnalyses) {
			if (column == columnAnalysis.getColumn()) {
				return columnAnalysis;
			}
		}
		return null;
	}

	@Override
	public int size() {
		return columnAnalyses.size();
	}

	@Override
	public void clear() {
		this.columnAnalyses.clear();
	}
}
