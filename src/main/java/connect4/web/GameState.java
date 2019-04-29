package connect4.web;

import connect4.Disc;

public enum GameState {

	// Don't reorder these, the ordinal is for JSON
	PLAYER_R_TURN, PLAYER_Y_TURN, PLAYER_R_WON, PLAYER_Y_WON, DRAW,;

	/**
	 * Returns the {@link GameState} representing a disc whose turn it is.
	 * @param disc the disc that has the current turn
	 * @return the game state
	 */
	public static GameState getTurnState(final Disc disc) {
		if (disc == null) {
			return null;
		}
		return disc == Disc.YELLOW ? GameState.PLAYER_Y_TURN : PLAYER_R_TURN;
	}

	/**
	 * Returns the {@link GameState} representing a disc that has won.
	 * @param disc the disc that has won
	 * @return the game state
	 */
	public static GameState getWinnerState(final Disc disc) {
		if (disc == null) {
			return null;
		}
		return disc == Disc.YELLOW ? GameState.PLAYER_Y_WON : PLAYER_R_WON;
	}
}
