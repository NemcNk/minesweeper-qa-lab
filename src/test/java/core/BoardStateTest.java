package core;

import model.CellState;

import org.junit.jupiter.api.Test;

import static core.TestBoards.at;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoardStateTest {

    private final BoardGeometry geometry = new BoardGeometry(3, 3);

    private BoardState state(int totalBombs) {
        return new BoardState(geometry, totalBombs);
    }

    // start

    @Test
    void startsWithAllCellsClosedAndZeroCounters() {

        BoardState state = state(2);

        for (var coord : geometry.getAllCoords()) {
            assertThat(state.get(coord)).isEqualTo(CellState.CLOSED);
        }

        assertThat(state.getFlaggedCount()).isZero();
        assertThat(state.getOpenedSafeCount()).isZero();
    }

    @Test
    void startResetsEverything() {

        BoardState state = state(2);

        state.setOpened(at(0, 0));
        state.toggleFlag(at(1, 1));

        state.start();

        assertThat(state.get(at(0, 0))).isEqualTo(CellState.CLOSED);
        assertThat(state.get(at(1, 1))).isEqualTo(CellState.CLOSED);
        assertThat(state.getFlaggedCount()).isZero();
        assertThat(state.getOpenedSafeCount()).isZero();
    }

    @Test
    void readingOutsideBoardThrows() {

        BoardState state = state(1);

        assertThatThrownBy(() -> state.get(at(3, 3)))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    // opening

    @Test
    void setOpenedOpensClosedCellAndCountsIt() {

        BoardState state = state(1);

        state.setOpened(at(0, 0));

        assertThat(state.get(at(0, 0))).isEqualTo(CellState.OPENED);
        assertThat(state.getOpenedSafeCount()).isEqualTo(1);
    }

    @Test
    void openingSameCellTwiceCountsItOnce() {

        BoardState state = state(1);

        state.setOpened(at(0, 0));
        state.setOpened(at(0, 0));

        assertThat(state.getOpenedSafeCount()).isEqualTo(1);
    }

    @Test
    void setOpenedDoesNotOpenFlaggedCell() {

        BoardState state = state(1);

        state.toggleFlag(at(0, 0));
        state.setOpened(at(0, 0));

        assertThat(state.get(at(0, 0))).isEqualTo(CellState.FLAGGED);
        assertThat(state.getOpenedSafeCount()).isZero();
    }

    // flags

    @Test
    void toggleFlagPlacesAndRemovesFlag() {

        BoardState state = state(2);

        state.toggleFlag(at(1, 1));

        assertThat(state.get(at(1, 1))).isEqualTo(CellState.FLAGGED);
        assertThat(state.getFlaggedCount()).isEqualTo(1);

        state.toggleFlag(at(1, 1));

        assertThat(state.get(at(1, 1))).isEqualTo(CellState.CLOSED);
        assertThat(state.getFlaggedCount()).isZero();
    }

    @Test
    void flagsCannotExceedBombCount() {

        BoardState state = state(2);

        state.toggleFlag(at(0, 0));
        state.toggleFlag(at(1, 0));
        state.toggleFlag(at(2, 0));

        assertThat(state.getFlaggedCount()).isEqualTo(2);
        assertThat(state.get(at(2, 0))).isEqualTo(CellState.CLOSED);
    }

    @Test
    void removingFlagFreesLimitForAnotherFlag() {

        BoardState state = state(1);

        state.toggleFlag(at(0, 0));
        state.toggleFlag(at(0, 0));
        state.toggleFlag(at(1, 0));

        assertThat(state.get(at(1, 0))).isEqualTo(CellState.FLAGGED);
    }

    @Test
    void openedCellCannotBeFlagged() {

        BoardState state = state(2);

        state.setOpened(at(0, 0));
        state.toggleFlag(at(0, 0));

        assertThat(state.get(at(0, 0))).isEqualTo(CellState.OPENED);
        assertThat(state.getFlaggedCount()).isZero();
    }

    @Test
    void zeroBombsMeansNoFlagsAtAll() {

        BoardState state = state(0);

        state.toggleFlag(at(0, 0));

        assertThat(state.get(at(0, 0))).isEqualTo(CellState.CLOSED);
    }

    // game over (reveal)

    @Test
    void setExplodedMarksCellRegardlessOfPreviousState() {

        BoardState state = state(1);

        state.setExploded(at(0, 0));

        assertThat(state.get(at(0, 0))).isEqualTo(CellState.EXPLODED);
    }

    @Test
    void openBombOpensClosedCellWithoutCountingItAsSafe() {

        BoardState state = state(1);

        state.openBomb(at(0, 0));

        assertThat(state.get(at(0, 0))).isEqualTo(CellState.OPENED);
        assertThat(state.getOpenedSafeCount()).isZero();
    }

    @Test
    void openBombKeepsFlagOnBomb() {

        BoardState state = state(1);

        state.toggleFlag(at(0, 0));
        state.openBomb(at(0, 0));

        assertThat(state.get(at(0, 0))).isEqualTo(CellState.FLAGGED);
    }

    @Test
    void markWrongFlagTurnsFlagIntoWrongFlag() {

        BoardState state = state(1);

        state.toggleFlag(at(0, 0));
        state.markWrongFlag(at(0, 0));

        assertThat(state.get(at(0, 0))).isEqualTo(CellState.WRONG_FLAG);
    }

    /** Regression test for BUG-001. */
    @Test
    void markWrongFlagDoesNotChangeFlagCounter() {

        BoardState state = state(1);

        state.toggleFlag(at(0, 0));
        state.markWrongFlag(at(0, 0));

        assertThat(state.getFlaggedCount()).isEqualTo(1);
    }

    @Test
    void markWrongFlagIgnoresCellsWithoutFlag() {

        BoardState state = state(1);

        state.markWrongFlag(at(0, 0));

        assertThat(state.get(at(0, 0))).isEqualTo(CellState.CLOSED);
    }

    // neighbours

    @Test
    void countsOnlyFlaggedNeighboursNotTheCellItself() {

        BoardState state = state(8);

        state.toggleFlag(at(0, 0));
        state.toggleFlag(at(1, 0));
        state.toggleFlag(at(1, 1));      // the centre cell itself

        assertThat(state.getFlaggedBoxesAround(at(1, 1))).isEqualTo(2);
    }

    @Test
    void doesNotCountOpenedOrClosedNeighbours() {

        BoardState state = state(8);

        state.setOpened(at(0, 0));

        assertThat(state.getFlaggedBoxesAround(at(1, 1))).isZero();
    }

    @Test
    void cornerCellHasOnlyThreeNeighboursToCount() {

        BoardState state = state(8);

        state.toggleFlag(at(1, 0));
        state.toggleFlag(at(0, 1));
        state.toggleFlag(at(1, 1));
        state.toggleFlag(at(2, 2));   // not a neighbour of the corner (0,0)

        assertThat(state.getFlaggedBoxesAround(at(0, 0))).isEqualTo(3);
    }
}