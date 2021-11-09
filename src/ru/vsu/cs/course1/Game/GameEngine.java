package ru.vsu.cs.course1.Game;

import ru.vsu.cs.util.ArrayUtils;

public class GameEngine {

    public enum GameState {
        NOT_STARTED,
        PLAYING,
        WIN,
    }

    private GameState state = GameState.NOT_STARTED;
    private boolean[][] field = null;
    private int countOfMoves = 0;

    public void newGame(GameLevels params) {
        field = (intToBoolean("levels/level_0" + params.getLevelNumber() + ".txt"));
        state = GameState.PLAYING;
        countOfMoves = 0;
    }

    private static boolean[][] intToBoolean(String fileName) {
        int[][] matrix = ArrayUtils.readIntArray2FromFile(fileName);
        boolean[][] field = new boolean[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j] == 1) {
                    field[i][j] = true;
                }
            }
        }
        return field;
    }

    public void leftMouseClick(int row, int col) {
        countOfMoves++;
        int rowCount = getRowCount(), colCount = getColCount();
        if (state != GameState.PLAYING ||
                row < 0 || row >= rowCount ||
                col < 0 || col >= colCount)
            return;
        field[row][col] = !field[row][col];
        if (row - 1 >= 0) {
            field[row - 1][col] = !field[row - 1][col];
        }
        if (row + 1 < field.length) {
            field[row + 1][col] = !field[row + 1][col];
        }
        if (col - 1 >= 0) {
            field[row][col - 1] = !field[row][col - 1];
        }
        if (col + 1 < field[0].length) {
            field[row][col + 1] = !field[row][col + 1];
        }
        calcState();
    }

    private void calcState() {
        int rowCount = getRowCount(), colCount = getColCount();
        for (int r = 0; r <rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                if (field[r][c]) {
                    return;
                }
            }
        }
        state = GameState.WIN;
    }

    public boolean check (int row, int col) {
        return field[row][col];
    }

    public GameState getState() {
        return state;
    }

    public int getRowCount() {
        return (field == null) ? 0 : field.length;
    }

    public int getColCount() {
        return (field == null) ? 0 : field[0].length;
    }

    public int getCountOfMoves() {
        return countOfMoves;
    }
}




