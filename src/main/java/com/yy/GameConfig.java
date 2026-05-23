package com.yy;

/**
 * 扫雷游戏配置类
 * 用于存储游戏的基本配置参数
 */
public class GameConfig {
    private int rows;//行数
    private int cols;//列数
    private int mines;//地雷数量
    private int cellSize;//单元格大小

    /**
     * 构造函数
     * @param rows 游戏行数
     * @param cols 游戏列数
     * @param mines 地雷数量
     */
    public GameConfig(int rows, int cols, int mines) {
        this(rows, cols, mines, 30);
    }

    /**
     * 构造函数
     * @param rows 游戏行数
     * @param cols 游戏列数
     * @param mines 地雷数量
     * @param cellSize 单元格大小（像素）
     */
    public GameConfig(int rows, int cols, int mines, int cellSize) {
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        this.cellSize = cellSize;
    }

    /**
     * 获取游戏行数
     * @return 行数
     */
    public int getRows() {
        return rows;
    }

    /**
     * 获取游戏列数
     * @return 列数
     */
    public int getCols() {
        return cols;
    }

    /**
     * 获取地雷数量
     * @return 地雷数量
     */
    public int getMines() {
        return mines;
    }

    /**
     * 获取初级难度配置
     * @return 初级配置对象
     */
    public static GameConfig getBeginnerConfig() {
        return new GameConfig(9, 9, 10);
    }

    /**
     * 获取中级难度配置
     * @return 中级配置对象
     */
    public static GameConfig getIntermediateConfig() {
        return new GameConfig(14, 17, 40);
    }

    /**
     * 获取高级难度配置
     * @return 高级配置对象
     */
    public static GameConfig getExpertConfig() {
        return new GameConfig(14, 30, 80);
    }
}
