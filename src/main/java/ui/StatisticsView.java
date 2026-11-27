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

/**
 * View displaying statistics for each difficulty level.
 * The player can delete all statistics after a confirmation.
 */
public final class StatisticsView extends VBox {

    private final StatsRepository stats;
    private final BooleanSupplier confirmReset;
    private final VBox card = new VBox();

    /**
     * Creates the statistics view.
     *
     * @param stats repository containing game statistics
     * @param backAction action to execute when returning to the menu
     */
    public StatisticsView(StatsRepository stats, Runnable backAction) {
        this(stats, backAction, StatisticsView::askToConfirmReset);
    }

    /**
     * Creates the statistics view with a custom confirmation.
     *
     * @param stats repository containing game statistics
     * @param backAction action to execute when returning to the menu
     * @param confirmReset returns true if the player confirms the reset
     */
    StatisticsView(
            StatsRepository stats,
            Runnable backAction,
            BooleanSupplier confirmReset
    ) {
        this.stats = Objects.requireNonNull(stats);
        this.confirmReset = Objects.requireNonNull(confirmReset);

        setAlignment(Pos.CENTER);
        setSpacing(20);

        getStyleClass().add("menu-root");
        setId(UiIds.STATISTICS_VIEW);

        Label title = new Label("Statistics");
        title.getStyleClass().add("statistics-title");

        card.getStyleClass().add("statistics-card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(USE_PREF_SIZE);

        refreshTable();

        Button backButton = new Button("←   Back to menu");
        backButton.getStyleClass().add("back-button");
        backButton.setId(UiIds.STATISTICS_BACK_BUTTON);
        backButton.setOnAction(event -> backAction.run());

        Button resetButton = new Button("Reset statistics");
        resetButton.getStyleClass().add("stats-link-button");
        resetButton.setId(UiIds.STATISTICS_RESET_BUTTON);
        resetButton.setOnAction(event -> resetStatistics());

        HBox buttons = new HBox(12, backButton, resetButton);
        buttons.setAlignment(Pos.CENTER);

        getChildren().addAll(title, card, buttons);
    }

    /**
     * Deletes all statistics if the player confirms, then redraws
     * the table.
     */
    private void resetStatistics() {
        if (!confirmReset.getAsBoolean()) {
            return;
        }

        stats.reset();
        refreshTable();
    }

    /**
     * Rebuilds the table from the repository.
     */
    private void refreshTable() {
        GridPane table = new GridPane();

        table.setHgap(30);
        table.setVgap(12);
        table.setAlignment(Pos.CENTER);

        String[] headers = {
                "Difficulty",
                "Wins",
                "Losses",
                "Best time"
        };

        for (int col = 0; col < headers.length; col++) {
            table.add(cell(headers[col], true), col, 0);
        }

        int row = 1;

        for (Difficulty difficulty : Difficulty.values()) {
            DifficultyStats entry = stats.get(difficulty);

            Label wins = cell(String.valueOf(entry.wins()), false);
            Label losses = cell(String.valueOf(entry.losses()), false);
            Label best = cell(
                    formatTime(entry.bestTimeSeconds()),
                    false
            );

            wins.setId(UiIds.statsWins(difficulty));
            losses.setId(UiIds.statsLosses(difficulty));
            best.setId(UiIds.statsBestTime(difficulty));

            table.add(cell(difficulty.getTitle(), false), 0, row);
            table.add(wins, 1, row);
            table.add(losses, 2, row);
            table.add(best, 3, row);

            row++;
        }

        card.getChildren().setAll(table);
    }

    /**
     * Asks the player to confirm the deletion.
     * Cancel is the default button to prevent accidental deletion.
     *
     * @return true if the player chose Reset
     */
    private static boolean askToConfirmReset() {
        ButtonType reset = new ButtonType(
                "Reset",
                ButtonBar.ButtonData.OK_DONE
        );

        ButtonType cancel = new ButtonType(
                "Cancel",
                ButtonBar.ButtonData.CANCEL_CLOSE
        );

        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "All wins, losses and best times will be deleted. "
                        + "This cannot be undone.",
                reset,
                cancel
        );

        alert.setTitle("Reset statistics");
        alert.setHeaderText("Reset all statistics?");

        ((Button) alert.getDialogPane().lookupButton(reset))
                .setDefaultButton(false);

        ((Button) alert.getDialogPane().lookupButton(cancel))
                .setDefaultButton(true);

        return alert.showAndWait()
                .filter(button -> button == reset)
                .isPresent();
    }

    /**
     * Creates a table cell with the appropriate CSS classes.
     *
     * @param text text displayed in the cell
     * @param header whether the cell is a header
     * @return configured label
     */
    private Label cell(String text, boolean header) {
        Label label = new Label(text);

        label.getStyleClass().add("statistics-cell");

        if (header) {
            label.getStyleClass().add("header");
        }

        return label;
    }

    /**
     * Formats a time in seconds as minutes and seconds.
     *
     * @param seconds optional time in seconds
     * @return formatted time or an em dash if unavailable
     */
    private String formatTime(OptionalInt seconds) {
        if (seconds.isEmpty()) {
            return "—";
        }

        int value = seconds.getAsInt();

        return String.format("%d:%02d", value / 60, value % 60);
    }
}

