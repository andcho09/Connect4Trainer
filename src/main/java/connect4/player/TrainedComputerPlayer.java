package connect4.player;

import connect4.Board;
import connect4.Disc;
import connect4.trainer.Trainer;

/**
 * A computer player that uses a trainer.
 */
public class TrainedComputerPlayer extends Player {

	private final Trainer trainer;

	public TrainedComputerPlayer(final String name, final Disc disc) {
		super(name, disc);
		this.trainer = new Trainer();
	}

	@Override
	public int nextMove(final Board board) {
		return trainer.analyse(board, getDisc());
	}

}
