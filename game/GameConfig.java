package game;

import java.awt.*;

/**
 * 游戏常量配置（实体类）
 */
public class GameConfig {
    // 基础参数
    public static final int ROWS = 9;
    public static final int COLS = 9;
    public static final int MINES = 10;
    public static final int CELL_SIZE = 30;

    // 格子状态
    public static final int COVERED = 0;
    public static final int UNCOVERED = 1;
    public static final int FLAGGED = 2;

    // 数字颜色
    public static Color getNumberColor(int num) {
        return switch (num) {
            case 1 -> Color.BLUE;
            case 2 -> new Color(0, 128, 0);
            case 3 -> Color.RED;
            case 4 -> new Color(0, 0, 128);
            case 5 -> new Color(128, 0, 0);
            case 6 -> Color.CYAN;
            case 7 -> Color.BLACK;
            case 8 -> Color.GRAY;
            default -> Color.BLACK;
        };
    }
}
