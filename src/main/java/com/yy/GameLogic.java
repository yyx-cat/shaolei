package com.yy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 游戏逻辑类
 * 处理扫雷游戏的核心逻辑，包括地雷布置、游戏状态管理等
 */
public class GameLogic {
    private GameConfig config;//游戏配置
    private Cell[][] cells;//单元格数组

    /**
     * 游戏状态枚举
     */
    public enum GameState {
        PLAYING,//进行时
        WON,//赢
        LOST//输
    }

    private GameState gameState;//游戏状态
    private int revealedCount;//已揭示的单元格数

    /**
     * 游戏监听器接口
     */
    public interface GameListener {
        /**
         * 游戏状态改变时调用
         * @param state 新的游戏状态
         */
        void onGameStateChanged(GameState state);

        /**
         * 地雷数量改变时调用
         * @param remainingMines 剩余地雷数量
         */
        void onMinesCountChanged(int remainingMines);
    }

    private List<GameListener> listeners;//监听器列表
    private int flaggedMines;//已标记的地雷数
    private boolean smartModeEnabled;//智能模式
    private long startTime;//游戏开始时间
    private long endTime;//游戏结束时间

    /**
     * 构造函数
     * @param config 游戏配置
     */
    public GameLogic(GameConfig config) {
        this.config = config;
        this.cells = new Cell[config.getRows()][config.getCols()];
        this.gameState = GameState.PLAYING;
        this.revealedCount = 0;
        this.listeners = new ArrayList<>();
        this.flaggedMines = 0;
        this.smartModeEnabled = false;
    }

    /**
     * 初始化游戏
     */
    public void initGame() {
        this.gameState = GameState.PLAYING;
        this.revealedCount = 0;
        this.flaggedMines = 0;
        this.startTime = System.currentTimeMillis();

        for (int i = 0; i < config.getRows(); i++) {
            for (int j = 0; j < config.getCols(); j++) {
                cells[i][j] = new Cell(i, j);
                cells[i][j].setCellListener(new Cell.CellListener() {
                    @Override
                    public void onLeftClick(Cell cell) {
                        handleLeftClick(cell);
                    }

                    @Override
                    public void onRightClick(Cell cell) {
                        handleRightClick(cell);
                    }
                });
            }
        }

        placeMines();
        calculateAdjacentMines();
        notifyMinesCountChanged(config.getMines() - flaggedMines);
    }

    /**
     * 随机布置地雷
     */
    private void placeMines() {
        Random random = new Random();
        int minesPlaced = 0;

        while (minesPlaced < config.getMines()) {
            int row = random.nextInt(config.getRows());
            int col = random.nextInt(config.getCols());

            if (!cells[row][col].isMine()) {
                cells[row][col].setMine(true);
                minesPlaced++;
            }
        }
    }

    /**
     * 计算每个单元格周围的地雷数量
     */
    private void calculateAdjacentMines() {
        for (int i = 0; i < config.getRows(); i++) {
            for (int j = 0; j < config.getCols(); j++) {
                if (!cells[i][j].isMine()) {
                    int count = countAdjacentMines(i, j);
                    cells[i][j].setAdjacentMines(count);
                }
            }
        }
    }

    /**
     * 计算指定单元格周围的地雷数量
     * @param row 行号
     * @param col 列号
     * @return 周围地雷数量
     */
    private int countAdjacentMines(int row, int col) {
        int count = 0;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }

                int newRow = row + i;
                int newCol = col + j;

                if (isValidPosition(newRow, newCol) && cells[newRow][newCol].isMine()) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * 检查位置是否有效
     * @param row 行号
     * @param col 列号
     * @return 位置是否有效
     */
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < config.getRows() && col >= 0 && col < config.getCols();
    }

    /**
     * 处理左键点击事件
     * @param cell 被点击的单元格
     */
    private void handleLeftClick(Cell cell) {
        if (gameState != GameState.PLAYING || cell.isFlagged()) {
            return;
        }

        // 智能模式：如果点击已揭示的格子，尝试揭开周围格子
        if (smartModeEnabled && cell.isRevealed() && cell.getAdjacentMines() > 0) {
            handleSmartClick(cell);
            return;
        }

        if (cell.isMine()) {
            gameOver(false);
        } else {
            // 揭示单元格
            revealCell(cell.getRow(), cell.getCol());
            checkWin();
        }
    }

    /**
     * 处理智能模式点击
     * @param cell 被点击的单元格
     */
    private void handleSmartClick(Cell cell) {
        int row = cell.getRow();
        int col = cell.getCol();
        int adjacentMines = cell.getAdjacentMines();

        // 计算周围标记的地雷数量
        int flaggedCount = countAdjacentFlags(row, col);

        // 如果标记的地雷数量不等于该格子的数字，不执行智能操作
        if (flaggedCount != adjacentMines) {
            return;
        }

        // 揭开周围未标记且未揭示的格子
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }

                int newRow = row + i;
                int newCol = col + j;

                if (isValidPosition(newRow, newCol)) {
                    Cell neighbor = cells[newRow][newCol];
                    if (!neighbor.isRevealed() && !neighbor.isFlagged()) {
                        if (neighbor.isMine()) {
                            gameOver(false);
                            return;
                        } else {
                            revealCell(newRow, newCol);
                        }
                    }
                }
            }
        }

        checkWin();
    }

    /**
     * 计算周围标记的地雷数量
     * @param row 行号
     * @param col 列号
     * @return 周围标记的地雷数量
     */
    private int countAdjacentFlags(int row, int col) {
        int count = 0;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }

                int newRow = row + i;
                int newCol = col + j;

                if (isValidPosition(newRow, newCol) && cells[newRow][newCol].isFlagged()) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * 处理右键点击事件
     * @param cell 被点击的单元格
     */
    private void handleRightClick(Cell cell) {
        // 判断游戏状态
        if (gameState != GameState.PLAYING || cell.isRevealed()) {
            return;
        }
        // 切换标记状态
        cell.toggleFlag();

        if (cell.isFlagged()) {
            flaggedMines++;
        } else {
            flaggedMines--;
        }
        // 通知监听器
        notifyMinesCountChanged(config.getMines() - flaggedMines);
    }

    /**
     * 揭示单元格
     * @param row 行号
     * @param col 列号
     */
    private void revealCell(int row, int col) {
        if (!isValidPosition(row, col)) {
            return;
        }

        Cell cell = cells[row][col];

        if (cell.isRevealed() || cell.isFlagged()) {
            return;
        }

        cell.reveal();
        revealedCount++;

        if (cell.getAdjacentMines() == 0) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) {
                        continue;
                    }
                    revealCell(row + i, col + j);
                }
            }
        }
    }

    /**
     * 检查是否获胜
     */
    private void checkWin() {
        // 计算总非地雷单元格数
        int totalNonMineCells = config.getRows() * config.getCols() - config.getMines();
        // 检查是否所有非地雷单元格都被揭示
        if (revealedCount == totalNonMineCells) {
            gameOver(true);
        }
    }

    /**
     * 游戏结束
     * @param won 是否获胜
     */
    private void gameOver(boolean won) {
        this.endTime = System.currentTimeMillis();
        gameState = won ? GameState.WON : GameState.LOST;
        revealAllMines();
        notifyGameStateChanged(gameState);
    }

    /**
     * 揭示所有地雷
     */
    private void revealAllMines() {
        for (int i = 0; i < config.getRows(); i++) {
            for (int j = 0; j < config.getCols(); j++) {
                if (cells[i][j].isMine()) {
                    cells[i][j].reveal();
                }
            }
        }
    }

    /**
     * 获取单元格
     * @param row 行号
     * @param col 列号
     * @return 单元格对象
     */
    public Cell getCell(int row, int col) {
        return cells[row][col];
    }

    /**
     * 获取游戏配置
     * @return 游戏配置对象
     */
    public GameConfig getConfig() {
        return config;
    }

    /**
     * 获取游戏状态
     * @return 当前游戏状态
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * 添加游戏监听器
     * @param listener 游戏监听器
     */
    public void addGameListener(GameListener listener) {
        listeners.add(listener);
    }

    /**
     * 通知游戏状态改变
     * @param state 新的游戏状态
     */
    private void notifyGameStateChanged(GameState state) {
        for (GameListener listener : listeners) {
            listener.onGameStateChanged(state);
        }
    }

    /**
     * 通知地雷数量改变
     * @param remainingMines 剩余地雷数量
     */
    private void notifyMinesCountChanged(int remainingMines) {
        for (GameListener listener : listeners) {
            listener.onMinesCountChanged(remainingMines);
        }
    }

    /**
     * 重新开始游戏
     */
    public void restartGame() {
        for (int i = 0; i < config.getRows(); i++) {
            for (int j = 0; j < config.getCols(); j++) {
                cells[i][j].reset();
            }
        }
        initGame();
    }

    /**
     * 设置智能模式状态
     * @param smartModeEnabled 智能模式是否启用
     */
    public void setSmartModeEnabled(boolean smartModeEnabled) {
        this.smartModeEnabled = smartModeEnabled;
    }

    /**
     * 获取游戏时长（毫秒）
     * @return 游戏时长
     */
    public long getElapsedTime() {
        if (gameState == GameState.PLAYING) {
            return System.currentTimeMillis() - startTime;
        } else {
            return endTime - startTime;
        }
    }

    /**
     * 格式化游戏时长
     * @return 格式化后的时长字符串
     */
    public String getFormattedElapsedTime() {
        long elapsed = getElapsedTime();// 获取游戏时长
        long seconds = elapsed / 1000;
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    /**
     * 保存游戏记录到数据库
     * @param result 游戏结果（"WIN" 或 "LOSE"）
     */
    public void saveGameRecord(String result) {
        String difficulty;
        if (config.getRows() == 9 && config.getCols() == 9 && config.getMines() == 10) {
            difficulty = "初级";
        } else if (config.getRows() == 14 && config.getCols() == 17 && config.getMines() == 40) {
            difficulty = "中级";
        } else if (config.getRows() == 14 && config.getCols() == 30 && config.getMines() == 80) {
            difficulty = "高级";
        } else {
            difficulty = "自定义";
        }

        GameRecord record = new GameRecord(
                difficulty,
                getElapsedTime(),
                result,
                config.getMines(),
                config.getRows(),
                config.getCols()
        );

        DatabaseManager.getInstance().saveGameRecord(record);
    }
}
