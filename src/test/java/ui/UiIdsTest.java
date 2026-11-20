package ui;

import model.Difficulty;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class UiIdsTest {

    @Test
    void cellIdContainsColumnThenRow() {

        assertThat(UiIds.cell(3, 4)).isEqualTo("cell-3-4");
        assertThat(UiIds.cell(0, 0)).isEqualTo("cell-0-0");
    }

    @Test
    void cellIdsOfNeighboursAreDifferent() {

        // "cell-1-12" and "cell-11-2" must not be confused
        assertThat(UiIds.cell(1, 12)).isNotEqualTo(UiIds.cell(11, 2));
        assertThat(UiIds.cell(1, 2)).isNotEqualTo(UiIds.cell(2, 1));
    }

    @Test
    void difficultyIdsAreStableAndDistinct() {

        assertThat(UiIds.difficultyButton(Difficulty.EASY))
                .isEqualTo("difficulty-easy");

        List<String> ids = new ArrayList<>();

        for (Difficulty difficulty : Difficulty.values()) {
            ids.add(UiIds.difficultyButton(difficulty));
            ids.add(UiIds.statsWins(difficulty));
            ids.add(UiIds.statsLosses(difficulty));
            ids.add(UiIds.statsBestTime(difficulty));
        }

        assertThat(ids).doesNotHaveDuplicates();
    }

    @Test
    void statisticsIdsUseDifficultyName() {

        assertThat(UiIds.statsWins(Difficulty.HARD))
                .isEqualTo("stats-hard-wins");
        assertThat(UiIds.statsLosses(Difficulty.HARD))
                .isEqualTo("stats-hard-losses");
        assertThat(UiIds.statsBestTime(Difficulty.HARD))
                .isEqualTo("stats-hard-best");
    }

    @Test
    void idsDoNotDependOnDefaultLocale() {

        Locale original = Locale.getDefault();

        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            assertThat(UiIds.difficultyButton(Difficulty.MEDIUM))
                    .isEqualTo("difficulty-medium");
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void allFixedIdsAreUniqueAndCssFriendly() throws IllegalAccessException {

        List<String> ids = new ArrayList<>();

        for (Field field : UiIds.class.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers())
                    && Modifier.isStatic(field.getModifiers())
                    && field.getType() == String.class) {
                ids.add((String) field.get(null));
            }
        }

        assertThat(ids).isNotEmpty().doesNotHaveDuplicates();
        // lookup("#id") needs plain lowercase ids with dashes
        assertThat(ids).allMatch(id -> id.matches("[a-z]+(-[a-z0-9]+)*"));
    }
}
