package model;

public enum Difficulty {

    EASY("Easy", 8, 10, 10),
    MEDIUM("Medium", 12, 20, 20),
    HARD("Hard", 16, 40, 40),
    EXPERT("Expert", 20, 80, 80),
    GOD("God", 24, 150, 150);

    private final String title;
    private final int size;
    private final int mines;
    private final int timeMinutes;

    Difficulty(String title, int size, int mines, int timeMinutes) {
        this.title = title;
        this.size = size;
        this.mines = mines;
        this.timeMinutes = timeMinutes;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public int getMines() {
        return mines;
    }

    public int getTimeMinutes() {
        return timeMinutes;
    }

    public GameConfig createConfig() {
        return new GameConfig(size, size, mines);
    }
}



