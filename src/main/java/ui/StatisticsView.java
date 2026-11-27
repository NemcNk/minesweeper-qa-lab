package ui;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.BooleanSupplier;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import model.Difficulty;
import stats.DifficultyStats;
import stats.StatsRepository;

/** View displaying statistics for each difficulty level.
 * The player can delete all statistics after a confirmation.
 */
public final class StatisticsView extends VBox {

    /** Provides access to stored game statistics. */
    private final StatsRepository stats;

    /** Determines whether the player confirms the statistics reset. */
    private final BooleanSupplier confirmReset;

    /** Contains the statistics table. */
    private final VBox card = new VBox();

    /** Creates the statistics view using the default confirmation dialog.
     * @param stats repository containing game statistics
     * @param backAction action to execute when returning to the menu
     */
    public StatisticsView(StatsRepository stats, Runnable backAction) {
        this(stats, backAction, StatisticsView::askToConfirmReset);
    }

    /** Creates the statistics view with a custom confirmation handler.
     * @param stats repository containing game statistics
     * @param backAction action to execute when returning to the menu
     * @param confirmReset returns true if the player confirms the reset
     */
    StatisticsView(
            StatsRepository stats,
            Runnable backAction,
            BooleanSupplier confirmReset
    ) {
        // Validate and store the required dependencies.
        this.stats = Objects.requireNonNull(stats);
        this.confirmReset = Objects.requireNonNull(confirmReset);

        // Configure the main view layout.
        setAlignment(Pos.CENTER);
        setSpacing(20);

        getStyleClass().add("menu-root");
        setId(UiIds.STATISTICS_VIEW);

        // Create and configure the statistics screen title.
        Label title = new Label("Statistics");
        title.getStyleClass().add("statistics-title");

        // Configure the container holding the statistics table.
        card.getStyleClass().add("statistics-card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(USE_PREF_SIZE);

        // Populate the table with the current statistics.
        refreshTable();

        // Create the button used to return to the main menu.
        Button backButton = new Button("←   Back to menu");
        backButton.getStyleClass().add("back-button");
        backButton.setId(UiIds.STATISTICS_BACK_BUTTON);
        backButton.setOnAction(event -> backAction.run());

        // Create the button used to reset all stored statistics.
        Button resetButton = new Button("Reset statistics");
        resetButton.getStyleClass().add("stats-link-button");
        resetButton.setId(UiIds.STATISTICS_RESET_BUTTON);
        resetButton.setOnAction(event -> resetStatistics());

        // Arrange the navigation and reset buttons horizontally.
        HBox buttons = new HBox(12, backButton, resetButton);
        buttons.setAlignment(Pos.CENTER);

        // Add all interface elements to the main view.
        getChildren().addAll(title, card, buttons);
    }

    /** Deletes all statistics if the player confirms the operation,
     * then refreshes the displayed table.*/
    private void resetStatistics() {

        // Stop the operation if the player cancels the confirmation.
        if (!confirmReset.getAsBoolean()) {
            return;
        }

        // Clear the repository and update the displayed statistics.
        stats.reset();
        refreshTable();
    }

    /** Rebuilds the statistics table using the data from the repository.
     */
    private void refreshTable() {
        // Create and configure a new grid for the statistics.
        GridPane table = new GridPane();

        table.setHgap(30);
        table.setVgap(12);
        table.setAlignment(Pos.CENTER);

        // Define the column headers.
        String[] headers = {
                "Difficulty",
                "Wins",
                "Losses",
                "Best time"
        };

        // Add the header labels to the first row.
        for (int col = 0; col < headers.length; col++) {
            table.add(cell(headers[col], true), col, 0);
        }

        int row = 1;

        // Populate the table with statistics for each difficulty level.
        for (Difficulty difficulty : Difficulty.values()) {
            DifficultyStats entry = stats.get(difficulty);

            Label wins = cell(String.valueOf(entry.wins()), false);
            Label losses = cell(String.valueOf(entry.losses()), false);
            Label best = cell(
                    formatTime(entry.bestTimeSeconds()),
                    false
            );

            // Assign unique identifiers to the statistics labels.
            wins.setId(UiIds.statsWins(difficulty));
            losses.setId(UiIds.statsLosses(difficulty));
            best.setId(UiIds.statsBestTime(difficulty));

            // Add the difficulty name and its statistics to the current row.
            table.add(cell(difficulty.getTitle(), false), 0, row);
            table.add(wins, 1, row);
            table.add(losses, 2, row);
            table.add(best, 3, row);

            row++;
        }

        // Replace the previous table with the updated one.
        card.getChildren().setAll(table);
    }

    /** Displays a confirmation dialog before deleting all statistics.
     * The Cancel button is selected by default to prevent accidental deletion.
     *
     * @return true if the player chose Reset, otherwise false
     */
    private static boolean askToConfirmReset() {

        // Define the confirmation button.
        ButtonType reset = new ButtonType(
                "Reset",
                ButtonBar.ButtonData.OK_DONE
        );

        // Define the cancellation button.
        ButtonType cancel = new ButtonType(
                "Cancel",
                ButtonBar.ButtonData.CANCEL_CLOSE
        );

        // Create a confirmation dialog explaining the consequences of the reset.
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "All wins, losses and best times will be deleted. "
                        + "This cannot be undone.",
                reset,
                cancel
        );

        alert.setTitle("Reset statistics");
        alert.setHeaderText("Reset all statistics?");

        // Prevent the Reset button from being activated by default.
        ((Button) alert.getDialogPane().lookupButton(reset))
                .setDefaultButton(false);

        // Make Cancel the default button to avoid accidental deletion.
        ((Button) alert.getDialogPane().lookupButton(cancel))
                .setDefaultButton(true);

        // Return true only when the player explicitly confirms the reset.
        return alert.showAndWait()
                .filter(button -> button == reset)
                .isPresent();
    }

    /** Creates a table cell with the appropriate CSS classes.
     * @param text text displayed in the cell
     * @param header whether the cell is a header
     * @return configured label.*/
    private Label cell(String text, boolean header) {
        Label label = new Label(text);

        // Apply the common CSS style to the cell.
        label.getStyleClass().add("statistics-cell");

        // Apply the additional header style when necessary.
        if (header) {
            label.getStyleClass().add("header");
        }

        return label;
    }

    /** Formats a time in seconds as minutes and seconds.
     * @param seconds optional time in seconds
     * @return formatted time or an em dash if unavailable.*/
    private String formatTime(OptionalInt seconds) {

        // Display an em dash when no best time has been recorded.
        if (seconds.isEmpty()) {
            return "—";
        }

        int value = seconds.getAsInt();

        // Convert seconds into minutes and remaining seconds.
        return String.format("%d:%02d", value / 60, value % 60);
    }
}


