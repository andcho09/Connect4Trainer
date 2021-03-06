package connect4.player;

import connect4.api.Board;
import connect4.api.Disc;
import connect4.trainer.Recommender;
import connect4.trainer.Trainer;

/**
 * A computer player that uses a trainer.
 */
public class TrainedComputerPlayer extends Player {

	private final Recommender recommender;

	public TrainedComputerPlayer(final String name, final Disc disc) {
		super(name, disc);
		this.recommender = new Trainer();
	}

	@Override
	public int nextMove(final Board board) {
		return recommender.recommend(board, getDisc());
	}

}
