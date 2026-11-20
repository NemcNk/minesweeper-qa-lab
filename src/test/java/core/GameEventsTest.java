package core;

import model.Coord;
import model.GameState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static core.TestBoards.at;
import static org.assertj.core.api.Assertions.assertThat;

class GameEventsTest {

    private final List<String> events = new ArrayList<>();

    private final GameEventListener recorder = new GameEventListener() {

        @Override
        public void gameStarted(Coord firstClick) {
            events.add("started " + firstClick.getX() + "," + firstClick.getY());
        }

        @Override
        public void flagToggled(Coord coord, boolean flagged) {
            events.add((flagged ? "flag " : "unflag ")
                    + coord.getX() + "," + coord.getY());
        }

        @Override
        public void gameFinished(GameState result) {
            events.add("finished " + result);
        }
    };

    // Bombs at (0,0) and (2,0); no empty cells, so a click opens one cell only.
    private Game game() {
        return TestBoards.game(recorder, "*.*", "...");
    }

    @Test
    void startIsReportedOnlyForTheFirstClick() {
        Game game = game();

        game.pressLeftButton(at(1, 1));
        game.pressLeftButton(at(0, 1));

        assertThat(events).containsExactly("started 1,1");
    }

    @Test
    void flagPlacedAndRemovedAreReported() {
        Game game = game();

        game.pressRightButton(at(1, 0));
        game.pressRightButton(at(1, 0));

        assertThat(events).containsExactly("flag 1,0", "unflag 1,0");
    }

    @Test
    void flagOnOpenedCellIsNotReported() {
        Game game = game();

        game.pressLeftButton(at(1, 1));
        game.pressRightButton(at(1, 1));

        assertThat(events).containsExactly("started 1,1");
    }

    @Test
    void flagOverTheLimitIsNotReported() {
        Game game = game();

        game.pressRightButton(at(0, 0));
        game.pressRightButton(at(2, 0));
        game.pressRightButton(at(1, 0));   // limit = 2 bombs, rejected

        assertThat(events).containsExactly("flag 0,0", "flag 2,0");
    }

    @Test
    void lossIsReportedOnce() {
        Game game = game();

        game.pressLeftButton(at(1, 1));
        game.pressLeftButton(at(0, 0));    // bomb
        game.pressLeftButton(at(1, 0));    // ignored: game is over

        assertThat(events)
                .containsExactly("started 1,1", "finished LOST_BOMB");
    }

    @Test
    void winIsReportedAfterStart() {
        Game game = TestBoards.game(recorder, "*...");

        game.pressLeftButton(at(3, 0));    // flood fill opens every safe cell

        assertThat(events).containsExactly("started 3,0", "finished WON");
    }

    @Test
    void timeoutIsReportedOnce() {
        Game game = game();

        game.pressLeftButton(at(1, 1));
        game.timeout();
        game.timeout();

        assertThat(events)
                .containsExactly("started 1,1", "finished TIMEOUT");
    }

    @Test
    void timeoutBeforeFirstClickReportsNothing() {
        Game game = game();

        game.timeout();

        assertThat(events).isEmpty();
    }
}
