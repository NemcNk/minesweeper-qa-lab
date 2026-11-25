package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellContentTest {

    @Test
    void shouldReturnNextNumberForEveryNumericContent() {
        assertEquals(CellContent.NUM1, CellContent.ZERO.getNextNumber());
        assertEquals(CellContent.NUM2, CellContent.NUM1.getNextNumber());
        assertEquals(CellContent.NUM3, CellContent.NUM2.getNextNumber());
        assertEquals(CellContent.NUM4, CellContent.NUM3.getNextNumber());
        assertEquals(CellContent.NUM5, CellContent.NUM4.getNextNumber());
        assertEquals(CellContent.NUM6, CellContent.NUM5.getNextNumber());
        assertEquals(CellContent.NUM7, CellContent.NUM6.getNextNumber());
        assertEquals(CellContent.NUM8, CellContent.NUM7.getNextNumber());
    }

    @Test
    void shouldThrowExceptionWhenCurrentContentIsNum8() {
        assertThrows(
                IllegalStateException.class,
                () -> CellContent.NUM8.getNextNumber()
        );
    }

    @Test
    void shouldThrowExceptionWhenCurrentContentIsBomb() {
        assertThrows(
                IllegalStateException.class,
                () -> CellContent.BOMB.getNextNumber()
        );
    }
}
