package com.yy;

import java.sql.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库管理类
 * 负责数据库连接和操作
 */
public class DatabaseManager {
    // 修改为使用用户目录的相对路径
    private static final String DB_DIR = "./data/";
    private static final String DB_NAME = "minesweeper.db";
    private static String DB_PATH;

    private static DatabaseManager instance;
    private Connection connection;
    private boolean initialized = false;

    private DatabaseManager() {
        ensureDataDirectory();
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * 确保数据目录存在
     */
    private void ensureDataDirectory() {
        try {
            // 获取当前工作目录
            String currentDir = System.getProperty("user.dir");
            File baseDir = new File(currentDir);

            // 创建data目录（在程序所在目录下）
            File dataDir = new File(baseDir, "data");
            if (!dataDir.exists()) {
                boolean created = dataDir.mkdirs();
                System.out.println("创建数据目录: " + created + ", 路径: " + dataDir.getAbsolutePath());
            }

            // 设置数据库路径
            DB_PATH = new File(dataDir, DB_NAME).getAbsolutePath();
            System.out.println("数据库路径设置为: " + DB_PATH);

        } catch (Exception e) {
            System.err.println("创建数据目录失败: " + e.getMessage());
            // 如果无法创建，使用当前目录
            DB_PATH = new File(DB_NAME).getAbsolutePath();
        }
    }

    /**
     * 初始化数据库
     */
    private synchronized void initializeDatabase() {
        if (initialized) {
            return;
        }

        try {
            // 加载SQLite驱动
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite驱动加载成功");

            // 创建数据库连接
            String dbUrl = "jdbc:sqlite:" + DB_PATH;
            System.out.println("连接数据库: " + dbUrl);

            connection = DriverManager.getConnection(dbUrl);
            System.out.println("数据库连接成功！路径: " + DB_PATH);

            // 设置连接属性
            connection.setAutoCommit(true);

            // 创建表
            createTable();

            initialized = true;

        } catch (ClassNotFoundException e) {
            System.err.println("找不到SQLite驱动: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("数据库连接失败: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("数据库初始化异常: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 创建游戏记录表
     */
    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS game_records (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                difficulty TEXT NOT NULL,
                game_time INTEGER NOT NULL,
                game_result TEXT NOT NULL,
                mine_count INTEGER NOT NULL,
                rows_count INTEGER NOT NULL,
                cols_count INTEGER NOT NULL,
                play_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("数据表创建成功！");

            // 检查表是否存在
            checkTableExists();
        } catch (SQLException e) {
            System.err.println("创建表失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 检查表是否存在
     */
    private void checkTableExists() {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='game_records'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                System.out.println("表 'game_records' 存在");
            } else {
                System.err.println("表 'game_records' 不存在！");
            }
        } catch (SQLException e) {
            System.err.println("检查表存在失败: " + e.getMessage());
        }
    }

    /**
     * 保存游戏记录
     */
    public boolean saveGameRecord(GameRecord record) {
        if (!initialized) {
            System.err.println("数据库未初始化，无法保存记录");
            return false;
        }

        String sql = """
            INSERT INTO game_records 
            (difficulty, game_time, game_result, mine_count, rows_count, cols_count) 
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, record.getDifficulty());
            pstmt.setLong(2, record.getGameTime());
            pstmt.setString(3, record.getGameResult());
            pstmt.setInt(4, record.getMineCount());
            pstmt.setInt(5, record.getRowsCount());
            pstmt.setInt(6, record.getColsCount());

            int rows = pstmt.executeUpdate();
            System.out.println("游戏记录保存成功！影响行数: " + rows);
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("保存记录失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取所有游戏记录（按时间倒序）
     */
    public List<GameRecord> getAllGameRecords() {
        List<GameRecord> records = new ArrayList<>();

        if (!initialized) {
            System.err.println("数据库未初始化，无法获取记录");
            return records;
        }

        String sql = "SELECT * FROM game_records ORDER BY play_date DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("开始查询所有游戏记录...");
            int count = 0;

            while (rs.next()) {
                GameRecord record = new GameRecord();
                record.setId(rs.getInt("id"));
                record.setDifficulty(rs.getString("difficulty"));
                record.setGameTime(rs.getLong("game_time"));
                record.setGameResult(rs.getString("game_result"));
                record.setMineCount(rs.getInt("mine_count"));
                record.setRowsCount(rs.getInt("rows_count"));
                record.setColsCount(rs.getInt("cols_count"));
                record.setPlayDate(rs.getTimestamp("play_date"));

                records.add(record);
                count++;
                System.out.println("查询到记录: " + record.toString());
            }

            System.out.println("共查询到 " + count + " 条记录");

        } catch (SQLException e) {
            System.err.println("查询记录失败: " + e.getMessage());
            e.printStackTrace();
        }

        return records;
    }

    /**
     * 获取最佳成绩（最短时间获胜）
     * 这个方法之前缺少了，现在添加
     */
    public List<GameRecord> getBestRecords() {
        List<GameRecord> records = new ArrayList<>();

        if (!initialized) {
            System.err.println("数据库未初始化，无法获取最佳记录");
            return records;
        }

        String sql = """
            SELECT * FROM game_records 
            WHERE game_result = 'WIN' 
            ORDER BY game_time ASC 
            LIMIT 10
            """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("开始查询最佳记录...");
            int count = 0;

            while (rs.next()) {
                GameRecord record = new GameRecord();
                record.setId(rs.getInt("id"));
                record.setDifficulty(rs.getString("difficulty"));
                record.setGameTime(rs.getLong("game_time"));
                record.setGameResult(rs.getString("game_result"));
                record.setMineCount(rs.getInt("mine_count"));
                record.setRowsCount(rs.getInt("rows_count"));
                record.setColsCount(rs.getInt("cols_count"));
                record.setPlayDate(rs.getTimestamp("play_date"));

                records.add(record);
                count++;
                System.out.println("查询到最佳记录: " + record.toString());
            }

            System.out.println("共查询到 " + count + " 条最佳记录");

        } catch (SQLException e) {
            System.err.println("查询最佳记录失败: " + e.getMessage());
            e.printStackTrace();
        }

        return records;
    }

    /**
     * 获取统计信息
     */
    public String getStatistics() {
        if (!initialized) {
            return "数据库未初始化";
        }

        StringBuilder stats = new StringBuilder();
        try {
            // 总游戏次数
            String totalSql = "SELECT COUNT(*) as total FROM game_records";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(totalSql)) {
                if (rs.next()) {
                    stats.append("总游戏次数: ").append(rs.getInt("total")).append("\n");
                }
            }

            // 获胜次数
            String winSql = "SELECT COUNT(*) as wins FROM game_records WHERE game_result = 'WIN'";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(winSql)) {
                if (rs.next()) {
                    stats.append("获胜次数: ").append(rs.getInt("wins")).append("\n");
                }
            }

            // 最佳时间
            String bestSql = "SELECT MIN(game_time) as best_time FROM game_records WHERE game_result = 'WIN'";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(bestSql)) {
                if (rs.next()) {
                    long bestTime = rs.getLong("best_time");
                    if (!rs.wasNull() && bestTime > 0) {
                        long seconds = bestTime / 1000;
                        long minutes = seconds / 60;
                        long remainingSeconds = seconds % 60;
                        stats.append("最佳时间: ").append(String.format("%02d:%02d", minutes, remainingSeconds));
                    } else {
                        stats.append("最佳时间: 暂无");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("获取统计信息失败: " + e.getMessage());
            e.printStackTrace();
            stats.append("获取统计信息失败: ").append(e.getMessage());
        }

        return stats.toString();
    }

    /**
     * 按难度分组统计
     */
    public String getDifficultyStatistics() {
        if (!initialized) {
            return "数据库未初始化";
        }

        StringBuilder stats = new StringBuilder();
        try {
            String sql = """
                SELECT difficulty, 
                       COUNT(*) as total,
                       SUM(CASE WHEN game_result = 'WIN' THEN 1 ELSE 0 END) as wins
                FROM game_records 
                GROUP BY difficulty
                ORDER BY difficulty
                """;

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String difficulty = rs.getString("difficulty");
                    int total = rs.getInt("total");
                    int wins = rs.getInt("wins");
                    double winRate = total > 0 ? (wins * 100.0 / total) : 0;

                    stats.append(String.format("%s: %d次游戏，%d次获胜 (%.1f%%)\n",
                            difficulty, total, wins, winRate));
                }
            }
        } catch (SQLException e) {
            System.err.println("获取难度统计失败: " + e.getMessage());
            e.printStackTrace();
        }

        return stats.toString();
    }

    /**
     * 检查数据库连接状态
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && initialized;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * 获取数据库路径
     */
    public String getDatabasePath() {
        return DB_PATH;
    }

    /**
     * 获取数据库连接
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * 关闭数据库连接
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("数据库连接已关闭");
                initialized = false;
            }
        } catch (SQLException e) {
            System.err.println("关闭连接失败: " + e.getMessage());
        }
    }
}