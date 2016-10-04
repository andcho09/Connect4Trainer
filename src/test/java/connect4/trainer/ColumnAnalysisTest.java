package connect4.trainer;

import org.junit.Assert;
import org.junit.Test;

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
}
