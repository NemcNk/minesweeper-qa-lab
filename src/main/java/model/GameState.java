package model;

/** Represents the lifecycle state of a single game.
 */
public enum GameState {

    /** The game is currently in progress. */
    PLAYING,

    /** The player lost the game by hitting a mine. */
    LOST_BOMB,

    /** The game ended because the time limit was reached. */
    TIMEOUT,

    /** The player successfully completed the game. */
    WON
}

