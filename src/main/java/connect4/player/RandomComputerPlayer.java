package connect4.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import connect4.Board;
import connect4.Disc;

/**
 * A computer player that plays purely randomly (i.e. badly).
 */
public class RandomComputerPlayer extends Player {

	private final Random random;

	public RandomComputerPlayer(final String name, final Disc disc) {
		super(name, disc);
		random = new Random();
	}

	@Override
	public int nextMove(final Board board) {
		final List<Integer> freeColumns = new ArrayList<>(board.getNumCols());
		for (int c = 0; c < board.getNumCols(); c++) {
			if (board.getDisc(c, board.getNumRows() - 1) == null) {
				freeColumns.add(c);
			}
		}
		final int move = random.nextInt(freeColumns.size());
		return move - 1;
	}

}
