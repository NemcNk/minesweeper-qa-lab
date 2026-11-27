package ui;

import model.Difficulty;

import java.util.Locale;

/** Centralized collection of user interface node identifiers.
 *
 * <p>UI tests use these identifiers to locate interface elements
 * (for example, {@code lookup("#" + UiIds.PAUSE_BUTTON)}).
 * Therefore, all identifiers are part of the testing contract.
 * Renaming an identifier may cause existing tests to fail.</p>
 *
 * <p>Each identifier must be unique within a scene. The main menu,
 * game screen, and statistics screen are displayed separately,
 * so different screens may reuse identifiers when necessary.</p>
 */
public final class UiIds {

    /** Identifiers used by the main menu. */
    public static final String MENU_TITLE = "menu-title";

    /** Identifies the button used to start a new game. */
    public static final String PLAY_BUTTON = "play-button";

    /** Identifies the button used to open the statistics screen. */
    public static final String STATISTICS_BUTTON = "statistics-button";

    /** Identifies the main game screen container. */
    public static final String GAME_SCREEN = "game-screen";

    /** Identifies the Minesweeper game board. */
    public static final String BOARD = "board";

    /** Identifies the display showing the remaining mine count. */
    public static final String MINES_DISPLAY = "mines-display";

    /** Identifies the display showing the elapsed game time. */
    public static final String TIME_DISPLAY = "time-display";

    /** Identifies the button displaying the current game face icon. */
    public static final String FACE_BUTTON = "face-button";

    /** Identifies the button used to pause the game. */
    public static final String PAUSE_BUTTON = "pause-button";

    /** Identifies the button used to return to the main menu. */
    public static final String BACK_BUTTON = "back-button";

    /** Identifies the overlay displayed when the game is paused. */
    public static final String PAUSE_OVERLAY = "pause-overlay";

    /** Identifies the overlay displaying the game result. */
    public static final String RESULT_OVERLAY = "result-overlay";

    /** Identifies the main victory or defeat message. */
    public static final String RESULT_TITLE = "result-title";

    /** Identifies the additional game result message. */
    public static final String RESULT_SUBTITLE = "result-subtitle";

    /** Identifies the label displaying the completed game time. */
    public static final String RESULT_TIME = "result-time";

    /** Identifies the primary action button on the result screen. */
    public static final String RESULT_PRIMARY_BUTTON = "result-primary-button";

    /** Identifies the button used to return to the main menu from the result screen. */
    public static final String RESULT_MENU_BUTTON = "result-menu-button";

    /** Identifies the statistics screen container. */
    public static final String STATISTICS_VIEW = "statistics-view";

    /** Identifies the button used to return from statistics to the main menu. */
    public static final String STATISTICS_BACK_BUTTON = "statistics-back-button";

    /** Identifies the button used to reset all game statistics. */
    public static final String STATISTICS_RESET_BUTTON = "statistics-reset-button";

    /** Prevents instantiation of this utility class.*/
    private UiIds() {
    }

    /** Returns the identifier of a board cell.
     *
     * <p>For example, the cell at column 3 and row 4 has the identifier
     * {@code cell-3-4}.</p>
     *
     * @param x column index, starting from 0
     * @param y row index, starting from 0
     * @return unique identifier of the specified board cell
     */
    public static String cell(int x, int y) {
        return "cell-" + x + "-" + y;
    }

    /** Returns the identifier of a difficulty selection button.
     *
     * <p>For example, the Easy difficulty button has the identifier
     * {@code difficulty-easy}.</p>
     *
     * @param difficulty selected game difficulty
     * @return identifier of the corresponding difficulty button
     */
    public static String difficultyButton(Difficulty difficulty) {
        return "difficulty-" + name(difficulty);
    }

    /** Returns the identifier of the wins value for a difficulty level.

     * <p>For example, the Easy difficulty wins value has the identifier
     * {@code stats-easy-wins}.</p>
     *
     * @param difficulty game difficulty
     * @return identifier of the wins value.*/
    public static String statsWins(Difficulty difficulty) {
        return "stats-" + name(difficulty) + "-wins";
    }

    /** Returns the identifier of the losses value for a difficulty level.
     *
     * <p>For example, the Easy difficulty losses value has the identifier
     * {@code stats-easy-losses}.</p>
     *
     * @param difficulty game difficulty
     * @return identifier of the losses value.*/
    public static String statsLosses(Difficulty difficulty) {
        return "stats-" + name(difficulty) + "-losses";
    }

    /** Returns the identifier of the best time value for a difficulty level.
     *
     * <p>For example, the Easy difficulty best time value has the identifier
     * {@code stats-easy-best}.</p>
     *
     * @param difficulty game difficulty
     * @return identifier of the best time value.*/
    public static String statsBestTime(Difficulty difficulty) {
        return "stats-" + name(difficulty) + "-best";
    }

    /** Converts a difficulty name to lowercase using a locale-independent
     * transformation.
     *
     * @param difficulty game difficulty
     * @return lowercase difficulty name.*/
    private static String name(Difficulty difficulty) {
        return difficulty.name().toLowerCase(Locale.ROOT);
    }
}
