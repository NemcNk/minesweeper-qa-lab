package core;

import model.CellState;
import model.Coord;

final class BoardState {

    private final BoardGeometry geometry;
    private final int totalBombs;

    private Matrix<CellState> stateMap;

    private int openedSafeCount;
    private int flaggedCount;

    BoardState(BoardGeometry geometry, int totalBombs) {

        this.geometry = geometry;
        this.totalBombs = totalBombs;

        start();
    }

    void start() {

        stateMap = new Matrix<>(
                geometry,
                () -> CellState.CLOSED
        );

        openedSafeCount = 0;
        flaggedCount = 0;
    }

    CellState get(Coord coord) {
        return stateMap.get(coord);
    }

    void setOpened(Coord coord) {

        if (stateMap.get(coord) == CellState.CLOSED) {

            stateMap.set(coord, CellState.OPENED);

            openedSafeCount++;
        }
    }

    void toggleFlag(Coord coord) {

        switch (stateMap.get(coord)) {

            case FLAGGED -> {
                stateMap.set(coord, CellState.CLOSED);
                flaggedCount--;
            }

            case CLOSED -> {
                if (flaggedCount < totalBombs) {
                    stateMap.set(coord, CellState.FLAGGED);
                    flaggedCount++;
                }
            }

            default -> {
            }
        }
    }

    void setExploded(Coord coord) {
        stateMap.set(coord, CellState.EXPLODED);
    }

    void openBomb(Coord coord) {

        if (stateMap.get(coord) == CellState.CLOSED) {
            stateMap.set(coord, CellState.OPENED);
        }
    }

    void markWrongFlag(Coord coord) {

        if (stateMap.get(coord) == CellState.FLAGGED) {

            stateMap.set(coord, CellState.WRONG_FLAG);

            flaggedCount--;
        }
    }

    int getFlaggedCount() {
        return flaggedCount;
    }

    int getOpenedSafeCount() {
        return openedSafeCount;
    }

    int getFlaggedBoxesAround(Coord coord) {

        int count = 0;

        for (Coord around : geometry.getCoordsAround(coord)) {

            if (stateMap.get(around) == CellState.FLAGGED) {
                count++;
            }
        }

        return count;
    }
}


