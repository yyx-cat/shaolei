package com.yy;

import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Font;

/**
 * JavaFX单元格类
 * 表示扫雷游戏中的单个格子，处理格子的显示和鼠标交互
 */
public class Cell extends Button {
    private int row;//行号
    private int col;//列号
    private boolean isMine;//是否是地雷
    private boolean isRevealed;//是否已揭示
    private boolean isFlagged;//标记
    private int adjacentMines;//周围地雷数量

    /**
     * 单元格监听器接口
     */
    public interface CellListener {
        /**
         * 单元格被左键点击时调用
         * @param cell 被点击的单元格
         */
        void onLeftClick(Cell cell);

        /**
         * 单元格被右键点击时调用
         * @param cell 被点击的单元格
         */
        void onRightClick(Cell cell);
    }

    private CellListener listener;//单元格监听器

    /**
     * 构造函数
     * @param row 行号
     * @param col 列号
     */
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.isMine = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.adjacentMines = 0;

        setPrefSize(40, 40);
        setFont(Font.font(16));

        //一个lambda表达式，当鼠标点击时调用监听器中的方法
        setOnMouseClicked(event -> {
            if (listener != null) {
                // 处理鼠标点击事件
                if (event.getButton() == MouseButton.PRIMARY) {//主按键，通常是左键
                    listener.onLeftClick(this);
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    listener.onRightClick(this);
                }
            }
        });

        updateDisplay();
    }

    /**
     * 获取行号
     * @return 行号
     */
    public int getRow() {
        return row;
    }

    /**
     * 获取列号
     * @return 列号
     */
    public int getCol() {
        return col;
    }

    /**
     * 设置是否是地雷
     * @param mine 是否是地雷
     */
    public void setMine(boolean mine) {
        this.isMine = mine;
    }

    /**
     * 判断是否是地雷
     * @return 是否是地雷
     */
    public boolean isMine() {
        return isMine;
    }

    /**
     * 判断是否已被揭示
     * @return 是否已被揭示
     */
    public boolean isRevealed() {
        return isRevealed;
    }

    /**
     * 揭示单元格
     */
    public void reveal() {
        this.isRevealed = true;
        updateDisplay();
    }

    /**
     * 判断是否被标记
     * @return 是否被标记
     */
    public boolean isFlagged() {
        return isFlagged;
    }

    /**
     * 切换标记状态
     */
    public void toggleFlag() {
        if (!isRevealed) {
            this.isFlagged = !isFlagged;
            updateDisplay();
        }
    }

    /**
     * 设置周围地雷数量
     * @param count 周围地雷数量
     */
    public void setAdjacentMines(int count) {
        this.adjacentMines = count;
    }

    /**
     * 获取周围地雷数量
     * @return 周围地雷数量
     */
    public int getAdjacentMines() {
        return adjacentMines;
    }

    /**
     * 设置单元格监听器
     * @param listener 单元格监听器
     */
    public void setCellListener(CellListener listener) {
        this.listener = listener;
    }

    /**
     * 重置单元格状态
     */
    public void reset() {
        this.isMine = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.adjacentMines = 0;
        updateDisplay();
    }

    private void updateDisplay() {
        if (isRevealed) {
            // 数字格子永远不禁用，以便智能模式使用
            if (isMine) {
                setDisable(true);
                setText("💣");
                setStyle("-fx-background-color: #ff6b6b;");
            } else {
                // 数字格子不禁用，可以接收点击事件
                setDisable(false);

                if (adjacentMines > 0) {
                    setText(String.valueOf(adjacentMines));
                    // 为了视觉区分，给数字格子添加不同的背景色
                    setStyle("-fx-background-color: #d0e0e0; -fx-text-fill: " + getNumberColor(adjacentMines) + ";");
                } else {
                    setText("");
                    setStyle("-fx-background-color: #e0e0e0;");
                }
            }
        } else {
            setDisable(false);
            if (isFlagged) {
                setText("🚩");
                setStyle("-fx-background-color: #ffd93d;");
            } else {
                setText("");
                setStyle("-fx-background-color: #a8a8a8;");
            }
        }
    }

    /**
     * 根据数字获取对应的颜色
     * @param number 数字
     * @return 对应的颜色
     */
    private String getNumberColor(int number) {
        switch (number) {
            case 1:
                return "#0000ff";
            case 2:
                return "#008000";
            case 3:
                return "#ff0000";
            case 4:
                return "#000080";
            case 5:
                return "#800000";
            case 6:
                return "#008080";
            case 7:
                return "#000000";
            case 8:
                return "#808080";
            default:
                return "#000000";
        }
    }
}
