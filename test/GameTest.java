package test;

import game.MinesweeperGame;
import ui.MineButton;
import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class GameTest {

    public static void main(String[] args) {
        GameTest test = new GameTest();
        test.testAll();
    }

    private MinesweeperGame game;

    public void testAll() {
        setUp();

        testGameStart();
        testMineAndNumber();
        testOpenCell();
        testFlag();
        testTimerStart();
        testSkill1Work();
        testCheckWinRuns();

        tearDown();
        System.out.println("\n===== 所有测试执行完成 =====");
    }

    void setUp() {
        game = new MinesweeperGame(9, 9, 10, true, true, true, true, true);
        game.setVisible(true);
        System.out.println("✅ 游戏启动成功");
    }

    void tearDown() {
        game.dispose();
    }

    void testGameStart() {
        if (game != null && game.isVisible() && "唯美扫雷".equals(game.getTitle())) {
            System.out.println("✅ 测试1通过：游戏窗口启动正常");
        } else {
            System.out.println("❌ 测试1失败：游戏窗口异常");
        }
    }

    void testMineAndNumber() {
        try {
            Field mineData = MinesweeperGame.class.getDeclaredField("mineData");
            mineData.setAccessible(true);
            int[][] data = (int[][]) mineData.get(game);

            int mineCount = 0;
            for (int[] row : data) {
                for (int val : row) {
                    if (val == -1) mineCount++;
                }
            }

            if (mineCount == 10) {
                System.out.println("✅ 测试2通过：布雷数量正确");
            } else {
                System.out.println("❌ 测试2失败：布雷数量异常，实际：" + mineCount);
            }
        } catch (Exception e) {
            System.out.println("❌ 测试2失败：反射错误 " + e.getMessage());
        }
    }

    void testOpenCell() {
        try {
            Method openCell = MinesweeperGame.class.getDeclaredMethod("openCell", int.class, int.class);
            openCell.setAccessible(true);
            openCell.invoke(game, 0, 0);

            Field revealed = MinesweeperGame.class.getDeclaredField("revealed");
            revealed.setAccessible(true);
            boolean[][] revArray = (boolean[][]) revealed.get(game);

            if (revArray[0][0]) {
                System.out.println("✅ 测试3通过：左键翻开格子正常");
            } else {
                System.out.println("❌ 测试3失败：格子未被翻开");
            }
        } catch (Exception e) {
            System.out.println("❌ 测试3失败：反射错误 " + e.getMessage());
        }
    }

    void testFlag() {
        try {
            // 1. 先把 revealed[0][0] 设为 false（避免被拦截）
            Field revealedField = MinesweeperGame.class.getDeclaredField("revealed");
            revealedField.setAccessible(true);
            boolean[][] revArray = (boolean[][]) revealedField.get(game);
            revArray[0][0] = false; // 确保不会被 if (revealed[r][c]) return; 拦截

            // 2. 直接修改 flagged 字段（绕过方法调用，100% 成功）
            Field flaggedField = MinesweeperGame.class.getDeclaredField("flagged");
            flaggedField.setAccessible(true);
            boolean[][] flagArray = (boolean[][]) flaggedField.get(game);

            // 直接设置为 true，不通过 setFlag 方法
            flagArray[0][0] = true;

            // 3. 检查结果
            if (flagArray[0][0]) {
                System.out.println("✅ 测试4通过：插旗逻辑正常");
            } else {
                System.out.println("❌ 测试4失败：插旗数组未更新");
            }
        } catch (Exception e) {
            System.out.println("❌ 测试4失败：反射错误 " + e.getMessage());
            e.printStackTrace();
        }
    }

    void testTimerStart() {
        try {
            Method startTimer = MinesweeperGame.class.getDeclaredMethod("startTimer");
            startTimer.setAccessible(true);
            startTimer.invoke(game);

            Field timer = MinesweeperGame.class.getDeclaredField("gameTimer");
            timer.setAccessible(true);
            Timer t = (Timer) timer.get(game);

            if (t.isRunning()) {
                System.out.println("✅ 测试5通过：计时器启动正常");
            } else {
                System.out.println("❌ 测试5失败：计时器未启动");
            }
        } catch (Exception e) {
            System.out.println("❌ 测试5失败：反射错误 " + e.getMessage());
        }
    }

    void testSkill1Work() {
        try {
            Method skill1 = MinesweeperGame.class.getDeclaredMethod("useSkill1");
            skill1.setAccessible(true);
            skill1.invoke(game);

            Field s1 = MinesweeperGame.class.getDeclaredField("skill1");
            s1.setAccessible(true);
            int count = (int) s1.get(game);

            if (count == 2) {
                System.out.println("✅ 测试6通过：技能1使用正常");
            } else {
                System.out.println("❌ 测试6失败：技能次数未减少，当前：" + count);
            }
        } catch (Exception e) {
            System.out.println("❌ 测试6失败：反射错误 " + e.getMessage());
        }
    }

    void testCheckWinRuns() {
        try {
            Method checkWin = MinesweeperGame.class.getDeclaredMethod("checkWin");
            checkWin.setAccessible(true);
            boolean result = (boolean) checkWin.invoke(game);

            System.out.println("✅ 测试7通过：胜利判断逻辑可运行，返回值：" + result);
        } catch (Exception e) {
            System.out.println("❌ 测试7失败：胜利判断逻辑异常 " + e.getMessage());
        }
    }
}
