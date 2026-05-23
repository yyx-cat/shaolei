package com.yy;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.List;

/**
 * JavaFX扫雷游戏主应用类
 * 创建游戏的主界面，包含游戏面板和控制按钮
 */
public class MineSweeperApp extends Application {
    private GameLogic gameLogic;//游戏逻辑
    private GridPane gamePanel;//游戏面板
    private Label statusLabel;//状态显示
    private Label timeLabel;//时间显示
    private Button restartButton;//重新开始
    private Button rulesButton;//规则按钮
    private Button aboutButton;//关于按钮
    private Button recordsButton;//记录按钮
    private Button statisticsButton;//统计按钮
    private ComboBox<String> difficultyComboBox;//难度选择
    private CheckBox smartModeCheckBox;//智能模式复选框
    private Stage primaryStage;//窗口
    private Timeline timer;//计时器

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // 初始化数据库（确保在UI创建前初始化）
        try {
            System.out.println("正在初始化数据库...");
            DatabaseManager dbManager = DatabaseManager.getInstance();
            System.out.println("数据库路径: " + dbManager.getDatabasePath());
            System.out.println("数据库连接状态: " + dbManager.isConnected());
        } catch (Exception e) {
            System.err.println("数据库初始化异常: " + e.getMessage());
            e.printStackTrace();

            // 显示错误提示
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("数据库错误");
            alert.setHeaderText("无法初始化数据库");
            alert.setContentText("数据库功能可能无法使用：\n" + e.getMessage());
            alert.showAndWait();
        }

        // 先创建基础UI组件
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        HBox topPanel = createTopPanel();
        HBox difficultyPanel = createDifficultyAndSmartModePanel();

        // 暂时创建空的游戏面板
        gamePanel = new GridPane();
        gamePanel.setPadding(new Insets(10));
        gamePanel.setHgap(2);
        gamePanel.setVgap(2);

        root.getChildren().addAll(topPanel, difficultyPanel, gamePanel);

        // 创建游戏逻辑
        GameConfig config = GameConfig.getBeginnerConfig();
        this.gameLogic = new GameLogic(config);

        // 设置智能模式事件处理器
        smartModeCheckBox.setOnAction(event -> {
            gameLogic.setSmartModeEnabled(smartModeCheckBox.isSelected());
        });

        // 初始化时也要设置
        gameLogic.setSmartModeEnabled(smartModeCheckBox.isSelected());

        gameLogic.addGameListener(new GameLogic.GameListener() {
            @Override
            public void onGameStateChanged(GameLogic.GameState state) {
                handleGameStateChanged(state);
            }

            @Override
            public void onMinesCountChanged(int remainingMines) {
                updateStatus(remainingMines);
            }
        });

        // 初始化游戏
        gameLogic.initGame();

        // 启动计时器
        startTimer();

        // 创建真正的游戏面板
        updateGamePanel();

        Scene scene = new Scene(root);
        stage.setTitle("扫雷游戏");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    /**
     * 启动计时器
     */
    private void startTimer() {
        if (timer != null) {
            timer.stop();
        }

        timer = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            if (gameLogic.getGameState() == GameLogic.GameState.PLAYING) {
                timeLabel.setText("时间: " + gameLogic.getFormattedElapsedTime());
            }
        }));

        timer.setCycleCount(Animation.INDEFINITE);
        timer.play();
    }

    /**
     * 创建顶部面板
     * @return 顶部面板
     */
    private HBox createTopPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(5));

        statusLabel = new Label("剩余地雷: 0");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        restartButton = new Button("重新开始");
        restartButton.setStyle("-fx-font-size: 14px;");
        restartButton.setOnAction(event -> restartGame());

        recordsButton = new Button("查看记录");
        recordsButton.setStyle("-fx-font-size: 14px;");
        recordsButton.setOnAction(event -> showRecordsDialog());

        statisticsButton = new Button("游戏统计");
        statisticsButton.setStyle("-fx-font-size: 14px;");
        statisticsButton.setOnAction(event -> showStatisticsDialog());

        rulesButton = new Button("规则");
        rulesButton.setStyle("-fx-font-size: 14px;");
        rulesButton.setOnAction(event -> showRules());

        aboutButton = new Button("关于");
        aboutButton.setStyle("-fx-font-size: 14px;");
        aboutButton.setOnAction(event -> showAbout());

        panel.getChildren().addAll(statusLabel, restartButton, recordsButton,
                statisticsButton, rulesButton, aboutButton);
        HBox.setHgrow(statusLabel, Priority.ALWAYS);

        return panel;
    }

    /**
     * 创建难度选择和智能模式面板
     * @return 难度选择和智能模式面板
     */
    private HBox createDifficultyAndSmartModePanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(5));

        Label difficultyLabel = new Label("难度:");
        difficultyLabel.setStyle("-fx-font-size: 14px;");

        difficultyComboBox = new ComboBox<>();
        difficultyComboBox.getItems().addAll("初级", "中级", "高级");
        difficultyComboBox.setValue("初级");
        difficultyComboBox.setStyle("-fx-font-size: 14px;");
        difficultyComboBox.setOnAction(event -> changeDifficulty());

        smartModeCheckBox = new CheckBox("智能模式");
        smartModeCheckBox.setStyle("-fx-font-size: 14px;");
        smartModeCheckBox.setSelected(false);

        timeLabel = new Label("时间: 00:00");
        timeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        panel.getChildren().addAll(difficultyLabel, difficultyComboBox, smartModeCheckBox, timeLabel);
        panel.setStyle("-fx-alignment: center;");
        HBox.setHgrow(timeLabel, Priority.ALWAYS);

        return panel;
    }

    /**
     * 更新游戏面板
     */
    private void updateGamePanel() {
        gamePanel.getChildren().clear();

        for (int i = 0; i < gameLogic.getConfig().getRows(); i++) {
            for (int j = 0; j < gameLogic.getConfig().getCols(); j++) {
                gamePanel.add(gameLogic.getCell(i, j), j, i);
            }
        }
    }

    /**
     * 处理游戏状态改变
     * @param state 新的游戏状态
     */
    private void handleGameStateChanged(GameLogic.GameState state) {
        if (state == GameLogic.GameState.WON) {
            String message = "恭喜你赢了！\n\n用时: " + gameLogic.getFormattedElapsedTime();
            showAlert(message, "游戏胜利");
            // 保存获胜记录
            gameLogic.saveGameRecord("WIN");
        } else if (state == GameLogic.GameState.LOST) {
            String message = "游戏结束，你踩到地雷了！\n\n用时: " + gameLogic.getFormattedElapsedTime();
            showAlert(message, "游戏失败");
            // 保存失败记录
            gameLogic.saveGameRecord("LOSE");
        }
    }

    /**
     * 显示提示框
     * @param message 提示消息
     * @param title 标题
     */
    private void showAlert(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 更新状态显示
     * @param remainingMines 剩余地雷数量
     */
    private void updateStatus(int remainingMines) {
        statusLabel.setText("剩余地雷: " + remainingMines);
    }

    /**
     * 重新开始游戏
     */
    private void restartGame() {
        gameLogic.restartGame();
        updateGamePanel();
        startTimer();
    }

    /**
     * 改变游戏难度
     */
    private void changeDifficulty() {
        String selectedDifficulty = difficultyComboBox.getValue();
        GameConfig newConfig;

        switch (selectedDifficulty) {
            case "初级":
                newConfig = GameConfig.getBeginnerConfig();
                break;
            case "中级":
                newConfig = GameConfig.getIntermediateConfig();
                break;
            case "高级":
                newConfig = GameConfig.getExpertConfig();
                break;
            default:
                newConfig = GameConfig.getBeginnerConfig();
        }

        // 保存智能模式状态
        boolean smartMode = smartModeCheckBox.isSelected();

        gameLogic = new GameLogic(newConfig);
        gameLogic.addGameListener(new GameLogic.GameListener() {
            @Override
            public void onGameStateChanged(GameLogic.GameState state) {
                handleGameStateChanged(state);
            }

            @Override
            public void onMinesCountChanged(int remainingMines) {
                updateStatus(remainingMines);
            }
        });

        // 恢复智能模式状态
        gameLogic.setSmartModeEnabled(smartMode);

        gameLogic.initGame();
        updateGamePanel();
        startTimer();
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
    }

    /**
     * 主方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * 显示游戏规则
     */
    private void showRules() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("游戏规则");
        alert.setHeaderText("扫雷游戏规则");

        String rules = "游戏目标：\n" +
                "  揭开所有不含地雷的格子，同时避免踩到地雷。\n\n" +
                "操作说明：\n" +
                "  • 左键点击：揭开格子\n" +
                "  • 右键点击：插旗标记/取消标记地雷（显示🚩图标）\n\n" +
                "游戏规则：\n" +
                "  1. 点击格子会显示周围地雷的数量\n" +
                "  2. 如果周围没有地雷，会自动揭开相邻格子\n" +
                "  3. 用右键标记你认为有地雷的格子\n" +
                "  4. 踩到地雷则游戏失败\n" +
                "  5. 揭开所有非雷格子则游戏胜利\n\n" +
                "智能模式：\n" +
                "  • 智能模式是扫雷游戏的帮助功能，可以提高游戏效率\n" +
                "  • 开启智能模式后，点击已揭示的数字格子可以快速操作\n" +
                "  • 当你标记的旗帜数量等于格子上的数字时，点击该格子\n" +
                "  • 系统会自动揭开周围所有未标记且未揭示的格子\n" +
                "  • 如果周围有未标记的地雷，游戏将失败\n" +
                "  • 如果标记数量不足，点击不会有任何反应\n" +
                "  • 智能模式适用于三种难度，可随时开启或关闭\n" +
                "  • 使用智能模式可以加快游戏进度，但需要谨慎判断\n\n" +
                "难度说明：\n" +
                "  • 初级：9×9 格子，10个地雷\n" +
                "  • 中级：14×17 格子，40个地雷\n" +
                "  • 高级：14×30 格子，80个地雷";

        TextArea textArea = new TextArea(rules);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-size: 13px; -fx-font-family: 'Microsoft YaHei', Arial;");
        textArea.setPrefSize(450, 420);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    /**
     * 显示关于信息
     */
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于");
        alert.setHeaderText("扫雷游戏");

        String about = "项目名称：JavaFX 扫雷游戏\n\n" +
                "版本：1.2.0\n\n" +
                "开发语言：Java\n" +
                "UI框架：JavaFX\n\n" +
                "功能特点：\n" +
                "  • 三种难度选择（初级、中级、高级）\n" +
                "  • 左键揭开格子，右键插旗标记\n" +
                "  • 智能模式：快速揭开周围格子\n" +
                "  • 自动揭示空白区域\n" +
                "  • 实时显示剩余地雷数\n" +
                "  • 实时显示游戏时长\n"+
                "  • 游戏胜利/失败提示\n" +
                "  • 游戏记录保存到数据库\n" +
                "  • 查看历史游戏记录\n" +
                "  • 游戏统计信息\n" +
                "  • 详细的规则说明\n\n" ;

        TextArea textArea = new TextArea(about);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-size: 13px; -fx-font-family: 'Microsoft YaHei', Arial;");
        textArea.setPrefSize(400, 320);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    /**
     * 显示游戏记录对话框
     */
    private void showRecordsDialog() {
        try {
            System.out.println("正在查询游戏记录...");
            DatabaseManager dbManager = DatabaseManager.getInstance();

            if (!dbManager.isConnected()) {
                showAlert("数据库未连接，无法查看记录", "错误");
                return;
            }

            List<GameRecord> allRecords = dbManager.getAllGameRecords();
            System.out.println("查询到 " + allRecords.size() + " 条记录");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("游戏记录");
            alert.setHeaderText("历史游戏记录");

            // 创建选项卡
            TabPane tabPane = new TabPane();

            // 所有记录
            Tab allRecordsTab = new Tab("所有记录");
            allRecordsTab.setClosable(false);

            if (allRecords.isEmpty()) {
                allRecordsTab.setContent(new Label("暂无游戏记录"));
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("所有游戏记录（按时间倒序）：\n\n");
                for (int i = 0; i < allRecords.size(); i++) {
                    GameRecord record = allRecords.get(i);
                    sb.append(String.format("%d. %s | 结果: %s | 用时: %s | 棋盘: %s | 地雷: %d | %s\n",
                            i + 1,
                            record.getDifficulty(),
                            record.getGameResult(),
                            record.getFormattedTime(),
                            record.getBoardSize(),
                            record.getMineCount(),
                            record.getFormattedDate()
                    ));
                }
                TextArea textArea = new TextArea(sb.toString());
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setPrefSize(650, 400);
                allRecordsTab.setContent(textArea);
            }

            // 最佳记录
            Tab bestRecordsTab = new Tab("最佳记录");
            bestRecordsTab.setClosable(false);

            List<GameRecord> bestRecords = dbManager.getBestRecords();
            if (bestRecords.isEmpty()) {
                bestRecordsTab.setContent(new Label("暂无获胜记录"));
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("最佳记录（获胜且用时最短）：\n\n");
                for (int i = 0; i < bestRecords.size(); i++) {
                    GameRecord record = bestRecords.get(i);
                    sb.append(String.format("%d. %s | 用时: %s | 棋盘: %s | 地雷: %d | %s\n",
                            i + 1,
                            record.getDifficulty(),
                            record.getFormattedTime(),
                            record.getBoardSize(),
                            record.getMineCount(),
                            record.getFormattedDate()
                    ));
                }
                TextArea textArea = new TextArea(sb.toString());
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setPrefSize(650, 400);
                bestRecordsTab.setContent(textArea);
            }

            tabPane.getTabs().addAll(allRecordsTab, bestRecordsTab);
            alert.getDialogPane().setContent(tabPane);
            alert.getDialogPane().setPrefSize(700, 500);
            alert.showAndWait();

        } catch (Exception e) {
            System.err.println("显示记录对话框异常: " + e.getMessage());
            e.printStackTrace();
            showAlert("查询记录失败: " + e.getMessage(), "错误");
        }
    }

    /**
     * 显示统计信息对话框
     */
    private void showStatisticsDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("游戏统计");
        alert.setHeaderText("游戏统计信息");

        String statistics = DatabaseManager.getInstance().getStatistics();
        if (statistics.isEmpty()) {
            statistics = "暂无统计信息";
        }

        TextArea textArea = new TextArea(statistics);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-size: 14px;");
        textArea.setPrefSize(300, 150);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }
}