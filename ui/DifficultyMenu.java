package ui;

import game.MinesweeperGame;
import sound.SoundManager;
import javax.swing.*;
import java.awt.*;

public class DifficultyMenu extends JFrame {
    private boolean musicOn = true;
    private JButton musicBtn;

    public DifficultyMenu() {
        setTitle("💣 扫雷 - 选择难度");
        setSize(520, 480);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint bgGrad = new GradientPaint(0, 0, new Color(245, 250, 255), 0, getHeight(), new Color(235, 245, 255));
                g2.setPaint(bgGrad);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBounds(0, 0, 520, 480);
        add(mainPanel);

        JLabel title = new JLabel("选择难度", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 36));
        title.setForeground(new Color(60, 70, 90));
        title.setBounds(0, 50, 520, 60);
        mainPanel.add(title);

        // 音乐按钮：深色文字 + 半透明背景，在浅色背景上清晰可见
        musicBtn = new JButton("🔊 音乐开");
        musicBtn.setBounds(400, 10, 100, 40);
        musicBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        musicBtn.setForeground(new Color(60, 70, 90)); // 深色文字
        musicBtn.setOpaque(true);
        musicBtn.setBackground(new Color(220, 230, 240, 150)); // 半透明背景
        musicBtn.setBorderPainted(false);
        musicBtn.setFocusPainted(false);
        musicBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        musicBtn.addActionListener(e -> toggleMusic());
        mainPanel.add(musicBtn);

        int btnW = 400, btnH = 60;
        int x = (520 - btnW) / 2;
        int y = 140, gap = 70;

        mainPanel.add(createBtn("初级 9×9 10雷", x, y, btnW, btnH,
                new Color(120, 220, 180), new Color(80, 200, 150), () -> start(9,9,10)));

        mainPanel.add(createBtn("中级 16×16 40雷", x, y+gap, btnW, btnH,
                new Color(80, 170, 230), new Color(50, 140, 210), () -> start(16,16,40)));

        mainPanel.add(createBtn("高级 16×30 99雷", x, y+gap*2, btnW, btnH,
                new Color(230, 130, 110), new Color(210, 90, 80), () -> start(16,30,99)));

        mainPanel.add(createBtn("自定义", x, y+gap*3, btnW, btnH,
                new Color(170, 130, 230), new Color(140, 100, 210), this::custom));

        SoundManager.playBGM();
    }

    private void toggleMusic() {
        musicOn = !musicOn;
        if (musicOn) {
            musicBtn.setText("🔊 音乐开");
            musicBtn.setForeground(new Color(60, 70, 90));
            musicBtn.setBackground(new Color(220, 230, 240, 150));
            SoundManager.playBGM();
        } else {
            musicBtn.setText("🔇 音乐关");
            musicBtn.setForeground(Color.WHITE);
            musicBtn.setBackground(new Color(180, 180, 180, 150));
            SoundManager.stopBGM();
        }
    }

    private JButton createBtn(String text, int x, int y, int w, int h, Color startColor, Color endColor, Runnable action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint grad = new GradientPaint(0, 0, startColor, 0, h, endColor);
                g2.setPaint(grad);
                g2.fillRoundRect(0, 0, w-1, h-1, 30, 30);
                super.paintComponent(g);
            }
        };
        btn.setBounds(x, y, w, h);
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