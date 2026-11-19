package core;

import model.CellContent;
import model.CellState;
import model.Coord;
import model.Difficulty;
import model.GameConfig;
import model.GameState;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Random;

import static core.TestBoards.at;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTest {

    // One bomb in the corner (0,0); the other cells are empty or show 1
    private static Game board3x3() {
        return TestBoards.game(
                "*..",
                "...",
                "...");
    }

    // 2x2: bomb at (0,0); the other three cells show 1
    private static Game board2x2() {
        return TestBoards.game(
                "*.",
                "..");
    }

    private static CellState stateOf(Game game, int x, int y) {
        return game.getCell(at(x, y)).getState();
    }

    // start

    @Test
    void newGameIsPlayingAndNotStarted() {

        Game game = board3x3();

        assertThat(game.getState()).isEqualTo(GameState.PLAYING);
        assertThat(game.isStarted()).isFalse();
        assertThat(game.getRemainingBombs()).isEqualTo(1);
    }

    @Test
    void contentIsHiddenBeforeFirstClick() {

        Game game = board3x3();

        assertThat(game.getCell(at(0, 0)).getContent())
                .isEqualTo(CellContent.ZERO);
        assertThat(stateOf(game, 0, 0)).isEqualTo(CellState.CLOSED);
    }

    @Test
    void publicConstructorRejectsBombsThatDoNotFitOutsideSafeArea() {

        // 4x4 = 16 cells, 9 are reserved for the safe area, so at most 7 bombs
        assertThatThrownBy(() -> new Game(new GameConfig(4, 4, 8)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsMaximumBombsThatLeaveSafeArea() {
        // 16 cells - 9 safe = 7 bombs is the limit
        assertThatCode(() -> new Game(new GameConfig(4, 4, 7)))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> new Game(new GameConfig(4, 4, 8)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void smallBoardUsesWholeBoardAsSafeArea() {
        // 2x2 = 4 cells, the safe area is min(9, 4) = 4, so 0 bombs fit
        assertThatCode(() -> new Game(new GameConfig(2, 2, 0)))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> new Game(new GameConfig(2, 2, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void publicConstructorCreatesPlayableGame() {
        Game game = new Game(Difficulty.EASY.createConfig());

        assertThat(game.getState()).isEqualTo(GameState.PLAYING);
        assertThat(game.getColumns()).isPositive();
    }

    // opening cells

    @Test
    void clickOnEmptyAreaOpensEverythingExceptBombAndWins() {

        Game game = board3x3();

        game.pressLeftButton(at(2, 2));

        assertThat(game.getState()).isEqualTo(GameState.WON);
        assertThat(stateOf(game, 0, 0)).isEqualTo(CellState.CLOSED);
        assertThat(stateOf(game, 1, 0)).isEqualTo(CellState.OPENED);
    }

    @Test
    void clickOnNumberOpensOnlyThatCell() {

        Game game = board3x3();

        game.pressLeftButton(at(1, 1));

        assertThat(stateOf(game, 1, 1)).isEqualTo(CellState.OPENED);
        assertThat(stateOf(game, 2, 2)).isEqualTo(CellState.CLOSED);
        assertThat(game.getState()).isEqualTo(GameState.PLAYING);
    }

    @Test
    void clickingOpenedEmptyCellDoesNotOpenNeighbours() {
        Game game = TestBoards.game("*...");

        game.pressRightButton(at(3, 0));   // flag a cell the flood fill must skip
        game.pressLeftButton(at(2, 0));    // opens (2,0); flagged (3,0) stays closed
        game.pressRightButton(at(3, 0));   // unflag -> CLOSED again
        game.pressLeftButton(at(2, 0));    // click an opened ZERO cell

        assertThat(game.getCell(at(3, 0)).getState()).isEqualTo(CellState.CLOSED);
        assertThat(game.getState()).isEqualTo(GameState.PLAYING);
    }

    @Test
    void firstClickStartsGameAndRevealsContent() {

        Game game = board3x3();

        game.pressLeftButton(at(1, 1));

        assertThat(game.isStarted()).isTrue();
        assertThat(game.getCell(at(0, 0)).getContent())
                .isEqualTo(CellContent.BOMB);
    }

    // loss

    @Test
    void openingBombLosesAndMarksExplodedCell() {

        Game game = board2x2();

        game.pressLeftButton(at(1, 1));
        game.pressLeftButton(at(0, 0));

        assertThat(game.getState()).isEqualTo(GameState.LOST_BOMB);
        assertThat(stateOf(game, 0, 0)).isEqualTo(CellState.EXPLODED);
    }

    @Test
    void clicksAreIgnoredAfterGameOver() {

        Game game = board2x2();

        game.pressLeftButton(at(1, 1));
        game.pressLeftButton(at(0, 0));

        game.pressLeftButton(at(1, 0));
        game.pressRightButton(at(0, 1));

        assertThat(game.getState()).isEqualTo(GameState.LOST_BOMB);
        assertThat(stateOf(game, 1, 0)).isEqualTo(CellState.CLOSED);
        assertThat(stateOf(game, 0, 1)).isEqualTo(CellState.CLOSED);
    }

    @Test
    void wrongFlagIsMarkedAfterLoss() {

        Game game = board2x2();

        game.pressLeftButton(at(1, 1));
        game.pressRightButton(at(1, 0));     // flag on a safe cell
        game.pressLeftButton(at(0, 0));      // bomb

        assertThat(stateOf(game, 1, 0)).isEqualTo(CellState.WRONG_FLAG);
    }

    /**
     * Reproduces BUG-001 (see docs/qa/bugs/BUG-001.md): wrong flags reduce
     * the flag counter when the board is revealed after a loss.
     */
    @Test
    void remainingBombsDoNotChangeAfterLoss() {

        Game game = board2x2();

        game.pressLeftButton(at(1, 1));
        game.pressRightButton(at(1, 0));
        int before = game.getRemainingBombs();

        game.pressLeftButton(at(0, 0));

        assertThat(game.getRemainingBombs()).isEqualTo(before);
    }

    // timeout

    @Test
    void timeoutBeforeFirstClickIsIgnored() {

        Game game = board2x2();

        game.timeout();

        assertThat(game.getState()).isEqualTo(GameState.PLAYING);
    }

    @Test
    void timeoutAfterStartEndsGameAndRevealsBombs() {

        Game game = board2x2();

        game.pressLeftButton(at(1, 1));
        game.timeout();

        assertThat(game.getState()).isEqualTo(GameState.TIMEOUT);
        assertThat(stateOf(game, 0, 0)).isEqualTo(CellState.OPENED);
    }

    // flags

    @Test
    void rightClickTogglesFlag() {

        Game game = board3x3();

        game.pressRightButton(at(1, 1));

        assertThat(stateOf(game, 1, 1)).isEqualTo(CellState.FLAGGED);
        assertThat(game.getRemainingBombs()).isZero();

        game.pressRightButton(at(1, 1));

        assertThat(stateOf(game, 1, 1)).isEqualTo(CellState.CLOSED);
        assertThat(game.getRemainingBombs()).isEqualTo(1);
    }

    @Test
    void flagsAreLimitedByBombCount() {

        Game game = board3x3();   // one bomb

        game.pressRightButton(at(1, 1));
        game.pressRightButton(at(2, 2));

        assertThat(stateOf(game, 2, 2)).isEqualTo(CellState.CLOSED);
        assertThat(game.getRemainingBombs()).isZero();
    }

    @Test
    void leftClickOnFlagIsIgnoredBeforeStart() {

        Game game = board3x3();

        game.pressRightButton(at(1, 1));
        game.pressLeftButton(at(1, 1));

        assertThat(game.isStarted()).isFalse();
        assertThat(stateOf(game, 1, 1)).isEqualTo(CellState.FLAGGED);
    }

    @Test
    void leftClickOnFlagIsIgnoredAfterStart() {

        Game game = board2x2();

        game.pressLeftButton(at(1, 1));
        game.pressRightButton(at(0, 0));
        game.pressLeftButton(at(0, 0));

        assertThat(game.getState()).isEqualTo(GameState.PLAYING);
        assertThat(stateOf(game, 0, 0)).isEqualTo(CellState.FLAGGED);
    }

    // chord

    @Test
    void chordWithMatchingFlagsOpensNeighbours() {

        Game game = board3x3();

        game.pressLeftButton(at(1, 1));
        game.pressRightButton(at(0, 0));
        game.pressLeftButton(at(1, 1));

        assertThat(stateOf(game, 2, 2)).isEqualTo(CellState.OPENED);
        assertThat(game.getState()).isEqualTo(GameState.WON);
    }

    @Test
    void chordWithTooFewFlagsDoesNothing() {

        Game game = board3x3();

        game.pressLeftButton(at(1, 1));
        game.pressLeftButton(at(1, 1));

        assertThat(stateOf(game, 1, 0)).isEqualTo(CellState.CLOSED);
        assertThat(game.getState()).isEqualTo(GameState.PLAYING);
    }

    @Test
    void chordWithWrongFlagOpensBombAndLoses() {

        Game game = board3x3();

        game.pressLeftButton(at(1, 1));
        game.pressRightButton(at(1, 0));  // flag not on a bomb
        game.pressLeftButton(at(1, 1));

        assertThat(game.getState()).isEqualTo(GameState.LOST_BOMB);
    }

    // invalid input

    @Test
    void nullAndOutOfBoardClicksAreIgnored() {

        Game game = board3x3();

        game.pressLeftButton(null);
        game.pressLeftButton(at(-1, 0));
        game.pressLeftButton(at(3, 3));
        game.pressRightButton(null);
        game.pressRightButton(at(0, -1));

        assertThat(game.isStarted()).isFalse();
        assertThat(game.getState()).isEqualTo(GameState.PLAYING);
        assertThat(game.getRemainingBombs()).isEqualTo(1);
    }

    @Test
    void getCellOutsideBoardThrows() {

        Game game = board3x3();

        assertThatThrownBy(() -> game.getCell(at(3, 0)))
                .isInstanceOf(IndexOutOfBoundsException.class);

        assertThatThrownBy(() -> game.getCell(null))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    // restart

    @Test
    void startBeginsNewGame() {

        Game game = board2x2();

        game.pressLeftButton(at(1, 1));
        game.pressLeftButton(at(0, 0));
        game.start();

        assertThat(game.getState()).isEqualTo(GameState.PLAYING);
        assertThat(game.isStarted()).isFalse();
        assertThat(stateOf(game, 0, 0)).isEqualTo(CellState.CLOSED);
        assertThat(game.getRemainingBombs()).isEqualTo(1);
    }

    // random layout (properties)

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    void firstClickIsAlwaysSafeAndBombCountIsExact(Difficulty difficulty) {

        int last = difficulty.getSize() - 1;
        int middle = difficulty.getSize() / 2;

        List<Coord> clicks = List.of(
                at(0, 0), at(last, last), at(middle, middle), at(last, 0)
        );

        BoardGeometry geometry =
                new BoardGeometry(difficulty.getSize(), difficulty.getSize());

        for (Coord click : clicks) {

            Game game = new Game(
                    difficulty.createConfig(),
                    new RandomBombPlacement(new Random(1))
            );

            game.pressLeftButton(click);

            assertThat(game.getCell(click).getContent())
                    .isNotEqualTo(CellContent.BOMB);

            for (Coord around : geometry.getCoordsAround(click)) {
                assertThat(game.getCell(around).getContent())
                        .isNotEqualTo(CellContent.BOMB);
            }

            long bombs = geometry.getAllCoords().stream()
                    .filter(c -> game.getCell(c).getContent()
                            == CellContent.BOMB)
                    .count();

            assertThat(bombs).isEqualTo(difficulty.getMines());
        }
    }
}
