package connect4.player;

import connect4.Board;
import connect4.Disc;

/**
 * Represents a player.
 */
public abstract class Player {

	protected final String name;
	protected final Disc disc;

	public Player(final String name, final Disc disc) {
		if (name == null) {
			throw new NullPointerException("The player's name must not be null");
		}
		if (disc == null) {
			throw new NullPointerException("The player's disc must not be null");
		}
		this.name = name;
		this.disc = disc;
	}

	/**
	 * Asks the player to make their next move.
	 * @param board the current state of the board
	 * @return the column (0-based) that the player will drop their disc
	 */
	public abstract int nextMove(Board board);

	/**
	 * @return the name of the player
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the disc this player drops
	 */
	public Disc getDisc() {
		return disc;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", this.name, this.disc.name());
	}
}
