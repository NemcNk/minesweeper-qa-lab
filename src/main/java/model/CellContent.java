package model;

public enum CellContent {

    ZERO(0),
    NUM1(1),
    NUM2(2),
    NUM3(3),
    NUM4(4),
    NUM5(5),
    NUM6(6),
    NUM7(7),
    NUM8(8),
    BOMB(-1);

    private final int number;

    CellContent(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public boolean isNumber() {
        return number >= 0;
    }


    public CellContent getNextNumber() {

        return switch (this) {
            case ZERO -> NUM1;
            case NUM1 -> NUM2;
            case NUM2 -> NUM3;
            case NUM3 -> NUM4;
            case NUM4 -> NUM5;
            case NUM5 -> NUM6;
            case NUM6 -> NUM7;
            case NUM7 -> NUM8;

            default -> throw new IllegalStateException(
                    "Cannot increment cell content: " + this
            );
        };
    }
}
