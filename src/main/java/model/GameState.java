package model;

/**
 * Lifecycle state of a single game.
 */
public enum GameState {

    /** The game is in progress (this includes the state before the first click). */
    PLAYING,

    /** The player opened a bomb. */
    LOST_BOMB,

    /** The time limit expired after the first click. */
    TIMEOUT,

    /** All safe cells are opened. */
    WON
}

