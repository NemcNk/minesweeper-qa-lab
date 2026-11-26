package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CellSnapshotTest {

    @Test
    void shouldThrowExceptionWhenContentIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CellSnapshot(null, CellState.CLOSED)
        );
    }

    @Test
    void shouldThrowExceptionWhenStateIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CellSnapshot(CellContent.ZERO, null)
        );
    }

    @Test
    void shouldCreateSnapshotWhenAllArgumentsAreValid() {
        CellSnapshot snapshot = new CellSnapshot(
                CellContent.NUM3,
                CellState.CLOSED
        );

        assertEquals(CellContent.NUM3, snapshot.getContent());
        assertEquals(CellState.CLOSED, snapshot.getState());
    }

    @Test
    void shouldReturnTrueWhenComparingObjectWithItself() {
        CellSnapshot snapshot = new CellSnapshot(
                CellContent.NUM1,
                CellState.CLOSED
        );

        assertEquals(snapshot, snapshot);
    }

    @Test
    void shouldReturnFalseWhenComparingWithNull() {
        CellSnapshot snapshot = new CellSnapshot(
                CellContent.NUM1,
                CellState.CLOSED
        );

        assertNotEquals(null, snapshot);
    }

    @Test
    void shouldReturnFalseWhenComparingWithDifferentClass() {
        CellSnapshot snapshot = new CellSnapshot(
                CellContent.NUM1,
                CellState.CLOSED
        );

        assertNotEquals(snapshot, "not a snapshot");
    }

    @Test
    void shouldReturnTrueWhenContentAndStateAreEqual() {
        CellSnapshot first = new CellSnapshot(
                CellContent.NUM1,
                CellState.CLOSED
        );

        CellSnapshot second = new CellSnapshot(
                CellContent.NUM1,
                CellState.CLOSED
        );

        assertEquals(first, second);
    }

    @Test
    void shouldReturnFalseWhenContentIsDifferent() {
        CellSnapshot first = new CellSnapshot(
                CellContent.NUM1,
                CellState.CLOSED
        );

        CellSnapshot second = new CellSnapshot(
                CellContent.NUM2,
                CellState.CLOSED
        );

        assertNotEquals(first, second);
    }

    @Test
    void shouldReturnFalseWhenStateIsDifferent() {
        CellSnapshot first = new CellSnapshot(
                CellContent.NUM1,
                CellState.CLOSED
        );

        CellSnapshot second = new CellSnapshot(
                CellContent.NUM1,
                CellState.OPENED
        );

        assertNotEquals(first, second);
    }

    @Test
    void shouldReturnSameHashCodeForEqualSnapshots() {
        CellSnapshot first = new CellSnapshot(
                CellContent.NUM1,
                CellState.CLOSED
        );

        CellSnapshot second = new CellSnapshot(
                CellContent.NUM1,
                CellState.CLOSED
        );

        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldReturnConsistentHashCode() {
        CellSnapshot snapshot = new CellSnapshot(
                CellContent.NUM1,
                CellState.CLOSED
        );

        int firstHash = snapshot.hashCode();
        int secondHash = snapshot.hashCode();

        assertEquals(firstHash, secondHash);
    }

    @ParameterizedTest
    @EnumSource(CellState.class)
    void shouldCreateSnapshotWithEveryCellState(CellState state) {

        CellSnapshot snapshot = new CellSnapshot(
                CellContent.ZERO,
                state
        );

        assertThat(snapshot.getState()).isEqualTo(state);
    }

    @ParameterizedTest
    @EnumSource(CellContent.class)
    void shouldCreateSnapshotWithEveryCellContent(CellContent content) {

        CellSnapshot snapshot = new CellSnapshot(
                content,
                CellState.CLOSED
        );

        assertThat(snapshot.getContent()).isEqualTo(content);
    }

    @Test
    void shouldSupportEveryContentAndStateCombination() {

        for (CellContent content : CellContent.values()) {
            for (CellState state : CellState.values()) {

                CellSnapshot snapshot =
                        new CellSnapshot(content, state);

                assertThat(snapshot.getContent()).isEqualTo(content);
                assertThat(snapshot.getState()).isEqualTo(state);
            }
        }
    }
}
