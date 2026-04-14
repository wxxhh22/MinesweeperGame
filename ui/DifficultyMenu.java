package ui;

import game.MinesweeperGame;
import javax.swing.*;
import java.awt.*;

public class DifficultyMenu extends JFrame {
    public DifficultyMenu() {
        setTitle("💣 扫雷 - 选择难度");
        setSize(520, 480);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // 柔和背景
        JPanel bgPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint bgGrad = new GradientPaint(0, 0, new Color(245, 250, 255), 0, getHeight(), new Color(235, 245, 255));
                g2.setPaint(bgGrad);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bgPanel.setBounds(0, 0, 520, 480);
        add(bgPanel);
        bgPanel.setLayout(null);

        // 标题：缩小字号，更协调
        JLabel title = new JLabel("选择难度", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 36));
        title.setForeground(new Color(60, 70, 90));
        title.setBounds(0, 50, 520, 60);
        bgPanel.add(title);

        int btnW = 400, btnH = 60;
        int x = (520 - btnW) / 2;
        int y = 140, gap = 70;

        // 4个难度按钮：柔和渐变色+适配字体
        bgPanel.add(createBtn("初级 9×9 10雷", x, y, btnW, btnH,
                new Color(120, 220, 180), new Color(80, 200, 150), () -> start(9,9,10)));

        bgPanel.add(createBtn("中级 16×16 40雷", x, y+gap, btnW, btnH,
                new Color(80, 170, 230), new Color(50, 140, 210), () -> start(16,16,40)));

        bgPanel.add(createBtn("高级 16×30 99雷", x, y+gap*2, btnW, btnH,
                new Color(230, 130, 110), new Color(210, 90, 80), () -> start(16,30,99)));

        bgPanel.add(createBtn("自定义", x, y+gap*3, btnW, btnH,
                new Color(170, 130, 230), new Color(140, 100, 210), this::custom));
    }

    // 创建带柔和渐变色的圆角按钮
    private JButton createBtn(String text, int x, int y, int w, int h, Color startColor, Color endColor, Runnable action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // 垂直渐变：降低饱和度，更柔和
                GradientPaint grad = new GradientPaint(0, 0, startColor, 0, h, endColor);
                g2.setPaint(grad);
                g2.fillRoundRect(0, 0, w-1, h-1, 30, 30);
                super.paintComponent(g);
            }
        };

        btn.setBounds(x, y, w, h);
        // 字体优化：缩小字号，加粗更清晰
        btn.setFont(new Font("微软雅黑", Font.BOLD, 20));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());

        return btn;
    }

    private void start(int rows, int cols, int mines) {
        dispose();
        new SkillSelectMenu(rows, cols, mines).setVisible(true);
    }

    private void custom() {
        try {
            int rows = Integer.parseInt(JOptionPane.showInputDialog("请输入行数："));
            int cols = Integer.parseInt(JOptionPane.showInputDialog("请输入列数："));
            int mines = Integer.parseInt(JOptionPane.showInputDialog("请输入地雷数："));
            start(rows, cols, mines);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "输入无效，请输入数字！");
        }
    }
}
