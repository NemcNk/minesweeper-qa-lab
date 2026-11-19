package model;

/**
 * Represents the available difficulty levels of the Minesweeper game.
 * Each level defines the board size, number of mines, and time limit.
 */
public enum Difficulty {

    EASY("Easy", 8, 10, 10),
    MEDIUM("Medium", 12, 20, 20),
    HARD("Hard", 16, 40, 40),
    EXPERT("Expert", 20, 80, 80),
    GOD("God", 24, 150, 150);

    /** Display name of the difficulty level. */
    private final String title;

    /** Number of rows and columns on the game board. */
    private final int size;

    /** Number of mines on the game board. */
    private final int mines;

    /** Time limit for the game in minutes. */
    private final int timeMinutes;

    /**
     * Creates a difficulty level with the specified parameters.
     *
     * @param title display name of the difficulty level
     * @param size board width and height
     * @param mines number of mines
     * @param timeMinutes game time limit in minutes
     */
    Difficulty(String title, int size, int mines, int timeMinutes) {
        this.title = title;
        this.size = size;
        this.mines = mines;
        this.timeMinutes = timeMinutes;
    }

    /**
     * Returns the display name of the difficulty level.
     *
     * @return difficulty title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the board size.
     *
     * @return board width and height
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the number of mines.
     *
     * @return number of mines
     */
    public int getMines() {
        return mines;
    }

    /**
     * Returns the game time limit.
     *
     * @return time limit in minutes
     */
    public int getTimeMinutes() {
        return timeMinutes;
    }

    /**
     * Creates a game configuration based on this difficulty level.
     *
     * @return a new game configuration
     */
    public GameConfig createConfig() {
        return new GameConfig(size, size, mines);
    }
}




