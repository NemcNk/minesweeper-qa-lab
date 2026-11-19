package ui;

import model.Difficulty;

/**
 * Visual settings for each difficulty level are a UI concern
 * and therefore do not belong in the {@link Difficulty} domain model.
 */
final class BoardLayout {

    private BoardLayout() {
    }

    static int cellSize(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 36;
            case MEDIUM -> 30;
            case HARD -> 25;
            case EXPERT -> 21;
            case GOD -> 18;
        };
    }
}

