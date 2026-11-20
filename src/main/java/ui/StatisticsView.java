package ui;

import java.util.OptionalInt;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import model.Difficulty;
import stats.DifficultyStats;
import stats.StatsRepository;

/**
 * View displaying statistics for each difficulty level.
 */
public final class StatisticsView extends VBox {

    private static final String TEXT_COLOR = "#7a7a7a";
    private static final String TITLE_COLOR = "#212b3f";

    /**
     * Creates the statistics view.
     *
     * @param stats repository containing game statistics
     * @param backAction action to execute when returning to the menu
     */
    public StatisticsView(StatsRepository stats, Runnable backAction) {
        setAlignment(Pos.CENTER);
        setSpacing(20);

        getStyleClass().add("menu-root");
        setId(UiIds.STATISTICS_VIEW);

        Label title = new Label("Statistics");
        title.setStyle(
                "-fx-text-fill: " + TITLE_COLOR + ";"
                        + "-fx-font-size: 28px;"
                        + "-fx-font-weight: bold;"
        );

        GridPane table = new GridPane();
        table.setHgap(30);
        table.setVgap(12);
        table.setAlignment(Pos.CENTER);

        String[] headers = {"Difficulty", "Wins", "Losses", "Best time"};

        for (int col = 0; col < headers.length; col++) {
            table.add(cell(headers[col], true), col, 0);
        }

        int row = 1;

        for (Difficulty difficulty : Difficulty.values()) {
            DifficultyStats entry = stats.get(difficulty);

            Label wins = cell(String.valueOf(entry.wins()), false);
            Label losses = cell(String.valueOf(entry.losses()), false);
            Label best = cell(formatTime(entry.bestTimeSeconds()), false);

            wins.setId(UiIds.statsWins(difficulty));
            losses.setId(UiIds.statsLosses(difficulty));
            best.setId(UiIds.statsBestTime(difficulty));

            table.add(cell(difficulty.getTitle(), false), 0, row);
            table.add(wins, 1, row);
            table.add(losses, 2, row);
            table.add(best, 3, row);

            row++;
        }

        VBox card = new VBox(table);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(24, 32, 24, 32));
        card.setMaxWidth(USE_PREF_SIZE);
        card.setStyle(
                "-fx-background-color: white;"
                        + "-fx-background-radius: 16;"
        );

        Button backButton = new Button("←   Back to menu");
        backButton.getStyleClass().add("back-button");
        backButton.setId(UiIds.STATISTICS_BACK_BUTTON);
        backButton.setOnAction(event -> backAction.run());

        getChildren().addAll(title, card, backButton);
    }

    /**
     * Creates a table cell.
     *
     * @param text text displayed in the cell
     * @param header whether the cell is a header
     * @return configured label
     */
    private Label cell(String text, boolean header) {
        Label label = new Label(text);

        label.setStyle(
                "-fx-text-fill: " + (header ? TITLE_COLOR : TEXT_COLOR) + ";"
                        + "-fx-font-size: 16px;"
                        + (header ? "-fx-font-weight: bold;" : "")
        );

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
