package ui;

import game.MinesweeperGame;
import javax.swing.*;
import java.awt.*;

public class SkillSelectMenu extends JFrame {
    private final int rows;
    private final int cols;
    private final int mines;

    private JCheckBox cb1, cb2, cb3, cb4, cb5;

    public SkillSelectMenu(int rows, int cols, int mines) {
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;

        setTitle("🎯 选择本局技能");
        setSize(420, 380);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 247, 250));
        setLayout(null);

        JLabel title = new JLabel("请选择你要使用的技能", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 24));
        title.setBounds(0, 20, 420, 50);
        add(title);

        cb1 = new JCheckBox(" 1️⃣ 数字标记");
        cb2 = new JCheckBox(" 2️⃣ 安全探测");
        cb3 = new JCheckBox(" 3️⃣ 排除地雷");
        cb4 = new JCheckBox(" 4️⃣ 区域扫描");
        cb5 = new JCheckBox(" 5️⃣ 自动插旗");

        cb1.setSelected(true);
        cb2.setSelected(true);
        cb3.setSelected(true);
        cb4.setSelected(true);
        cb5.setSelected(true);

        Font cbFont = new Font("微软雅黑", Font.PLAIN, 16);
        Color bg = new Color(245, 247, 250);

        JCheckBox[] boxes = {cb1, cb2, cb3, cb4, cb5};
        int y = 90;
        for (JCheckBox cb : boxes) {
            cb.setFont(cbFont);
            cb.setBackground(bg);
            cb.setFocusPainted(false);
            cb.setBounds(60, y, 300, 35);
            add(cb);
            y += 40;
        }

        JButton startBtn = new JButton("✅ 开始游戏");
        startBtn.setBounds(110, 300, 200, 40);
        startBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        startBtn.setBackground(new Color(80, 180, 120));
        startBtn.setForeground(Color.WHITE);
        startBtn.setFocusPainted(false);
        startBtn.addActionListener(e -> startGame());
        add(startBtn);
    }

    private void startGame() {
        dispose();
        new MinesweeperGame(
                rows, cols, mines,
                cb1.isSelected(),
                cb2.isSelected(),
                cb3.isSelected(),
                cb4.isSelected(),
                cb5.isSelected()
        ).setVisible(true);
    }
}
