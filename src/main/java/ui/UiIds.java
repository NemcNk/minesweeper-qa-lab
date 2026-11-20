package ui;

import model.Difficulty;

import java.util.Locale;

/**
 * Node ids of the user interface. UI tests find elements by these ids
 * (for example {@code lookup("#" + UiIds.PAUSE_BUTTON)}), so the ids are
 * kept in one place and are part of the testing contract: renaming an id
 * breaks the tests on purpose.
 *
 * <p>An id must be unique inside one scene. The main menu, the game and
 * the statistics are never shown together, so a screen may reuse a short
 * name such as {@code back-button} only once.
 */
public final class UiIds {

    // Main menu
    public static final String MENU_TITLE = "menu-title";
    public static final String PLAY_BUTTON = "play-button";
    public static final String STATISTICS_BUTTON = "statistics-button";

    // Game screen
    public static final String GAME_SCREEN = "game-screen";
    public static final String BOARD = "board";
    public static final String MINES_DISPLAY = "mines-display";
    public static final String TIME_DISPLAY = "time-display";
    public static final String FACE_BUTTON = "face-button";
    public static final String PAUSE_BUTTON = "pause-button";
    public static final String BACK_BUTTON = "back-button";
    public static final String PAUSE_OVERLAY = "pause-overlay";

    // Result overlay (always in the scene, visible only after a game)
    public static final String RESULT_OVERLAY = "result-overlay";
    public static final String RESULT_TITLE = "result-title";
    public static final String RESULT_SUBTITLE = "result-subtitle";
    public static final String RESULT_TIME = "result-time";
    public static final String RESULT_PRIMARY_BUTTON = "result-primary-button";
    public static final String RESULT_MENU_BUTTON = "result-menu-button";

    // Statistics screen
    public static final String STATISTICS_VIEW = "statistics-view";
    public static final String STATISTICS_BACK_BUTTON = "statistics-back-button";

    private UiIds() {
    }

    /**
     * Returns the id of a board cell, for example {@code cell-3-4}.
     *
     * @param x column, counted from 0
     * @param y row, counted from 0
     */
    public static String cell(int x, int y) {
        return "cell-" + x + "-" + y;
    }

    /**
     * Returns the id of a difficulty button, for example
     * {@code difficulty-easy}.
     */
    public static String difficultyButton(Difficulty difficulty) {
        return "difficulty-" + name(difficulty);
    }

    /** Returns the id of the "wins" value, for example {@code stats-easy-wins}. */
    public static String statsWins(Difficulty difficulty) {
        return "stats-" + name(difficulty) + "-wins";
    }

    /** Returns the id of the "losses" value, for example {@code stats-easy-losses}. */
    public static String statsLosses(Difficulty difficulty) {
        return "stats-" + name(difficulty) + "-losses";
    }

    /** Returns the id of the "best time" value, for example {@code stats-easy-best}. */
    public static String statsBestTime(Difficulty difficulty) {
        return "stats-" + name(difficulty) + "-best";
    }

    private static String name(Difficulty difficulty) {
        return difficulty.name().toLowerCase(Locale.ROOT);
    }
}
