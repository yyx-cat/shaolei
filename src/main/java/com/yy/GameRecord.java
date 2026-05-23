package com.yy;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 游戏记录实体类
 * 对应数据库中的 game_records 表
 */
public class GameRecord {
    private int id;
    private String difficulty;
    private long gameTime;  // 毫秒
    private String gameResult;
    private int mineCount;
    private int rowsCount;
    private int colsCount;
    private Timestamp playDate;

    // 构造方法
    public GameRecord() {}

    public GameRecord(String difficulty, long gameTime, String gameResult,
                      int mineCount, int rowsCount, int colsCount) {
        this.difficulty = difficulty;
        this.gameTime = gameTime;
        this.gameResult = gameResult;
        this.mineCount = mineCount;
        this.rowsCount = rowsCount;
        this.colsCount = colsCount;
        this.playDate = new Timestamp(System.currentTimeMillis());
    }

    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public long getGameTime() {
        return gameTime;
    }

    public void setGameTime(long gameTime) {
        this.gameTime = gameTime;
    }

    public String getGameResult() {
        return gameResult;
    }

    public void setGameResult(String gameResult) {
        this.gameResult = gameResult;
    }

    public int getMineCount() {
        return mineCount;
    }

    public void setMineCount(int mineCount) {
        this.mineCount = mineCount;
    }

    public int getRowsCount() {
        return rowsCount;
    }

    public void setRowsCount(int rowsCount) {
        this.rowsCount = rowsCount;
    }

    public int getColsCount() {
        return colsCount;
    }

    public void setColsCount(int colsCount) {
        this.colsCount = colsCount;
    }

    public Timestamp getPlayDate() {
        return playDate;
    }

    public void setPlayDate(Timestamp playDate) {
        this.playDate = playDate;
    }

    /**
     * 获取格式化的游戏时间
     */
    public String getFormattedTime() {
        long seconds = gameTime / 1000;
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    /**
     * 获取格式化的游戏日期
     */
    public String getFormattedDate() {
        LocalDateTime dateTime = playDate.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    /**
     * 获取棋盘大小字符串
     */
    public String getBoardSize() {
        return rowsCount + "×" + colsCount;
    }

    @Override
    public String toString() {
        return String.format("难度: %s | 结果: %s | 用时: %s | 棋盘: %s | 地雷: %d | 时间: %s",
                difficulty, gameResult, getFormattedTime(), getBoardSize(), mineCount, getFormattedDate());
    }
}