package ru.vsu.cs.course1.Game;

public class GameLevels {
    private int levelNumber;
    private int countOfLevels = 5;

    public GameLevels(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public int getCountOfLevels() {
        return countOfLevels;
    }
}
