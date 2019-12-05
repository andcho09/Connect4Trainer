package connect4.trainer;

import org.junit.Assert;
import org.junit.Test;

import connect4.api.analysis.ColumnAnalysis;

public class ColumnAnalysisTest {

	@Test
	public void testRemove() {
		final ColumnAnalysis analysis = new ColumnAnalysis(1);
		Assert.assertEquals(1, analysis.getColumn());
		analysis.addCondition(ColumnAnalysis.FLAG_ENABLE_OPPONENT_WIN);
		Assert.assertTrue(analysis.hasCondition(ColumnAnalysis.FLAG_ENABLE_OPPONENT_WIN));

		analysis.addCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE);
		Assert.assertTrue(analysis.hasCondition(ColumnAnalysis.FLAG_ENABLE_OPPONENT_WIN));
		Assert.assertTrue(analysis.hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));

		analysis.removeCondition(ColumnAnalysis.FLAG_ENABLE_OPPONENT_WIN);
		Assert.assertFalse(analysis.hasCondition(ColumnAnalysis.FLAG_ENABLE_OPPONENT_WIN));
		Assert.assertTrue(analysis.hasCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));
	}

	public void testHasConditions() {
		final ColumnAnalysis analysis = new ColumnAnalysis(1);
		Assert.assertTrue(analysis.hasCondition(ColumnAnalysis.FLAG_NO_OPINION));
		Assert.assertTrue(analysis.hasConditions(ColumnAnalysis.FLAG_NO_OPINION));

		analysis.addCondition(ColumnAnalysis.FLAG_ENABLE_OPPONENT_WIN);
		Assert.assertTrue(analysis.hasConditions(ColumnAnalysis.FLAG_ENABLE_OPPONENT_WIN));

		analysis.addCondition(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE);
		Assert.assertTrue(analysis.hasConditions(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE));
		Assert.assertTrue(analysis.hasConditions(ColumnAnalysis.FLAG_TRAP_MORE_THAN_ONE | ColumnAnalysis.FLAG_ENABLE_OPPONENT_WIN));
	}
}
