package game;

import ui.DifficultyMenu;
import ui.MineButton;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class MinesweeperGame extends JFrame {
    private int ROWS, COLS, MINES;
    private int[][] mineData;
    private MineButton[][] buttons;
    private boolean[][] revealed, flagged;
    private int flagCount = 0, timeCount = 0;
    private Timer gameTimer;
    private boolean isGameStarted = false;

    // 技能次数
    private int skill1 = 3, skill2 = 3, skill3 = 2, skill4 = 2, skill5 = 2;
    private boolean enableSkill1, enableSkill2, enableSkill3, enableSkill4, enableSkill5;
    private JLabel skillLabel1, skillLabel2, skillLabel3, skillLabel4, skillLabel5;
    private JLabel timeLabel, mineLabel;
    private JButton restartBtn;
    private Point scanPoint = null;

    private static final Color BG = new Color(242, 247, 255);
    private static final Color PANEL = new Color(225, 235, 245);
    private static final Color BTN_CLOSE = new Color(220, 230, 240);

    public MinesweeperGame(int rows, int cols, int mines,
                           boolean s1, boolean s2, boolean s3, boolean s4, boolean s5) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        this.ROWS = rows;
        this.COLS = cols;
        this.MINES = mines;
        this.enableSkill1 = s1;
        this.enableSkill2 = s2;
        this.enableSkill3 = s3;
        this.enableSkill4 = s4;
        this.enableSkill5 = s5;

        setTitle("唯美扫雷");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(BG);

        mineData = new int[ROWS][COLS];
        revealed = new boolean[ROWS][COLS];
        flagged = new boolean[ROWS][COLS];
        buttons = new MineButton[ROWS][COLS];

        initMines();
        add(createTop(), BorderLayout.NORTH);
        add(createBoard(), BorderLayout.CENTER);
        add(createSkillPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void initMines() {
        int count = 0;
        Random rnd = new Random();
        while (count < MINES) {
            int r = rnd.nextInt(ROWS);
            int c = rnd.nextInt(COLS);
            if (mineData[r][c] != -1) {
                mineData[r][c] = -1;
                count++;
            }
        }
        for (int i = 0; i < ROWS; i++)
            for (int j = 0; j < COLS; j++)
                if (mineData[i][j] != -1)
                    mineData[i][j] = countMines(i, j);
    }

    private int countMines(int r, int c) {
        int n = 0;
        for (int dr = -1; dr <= 1; dr++)
            for (int dc = -1; dc <= 1; dc++) {
                int nr = r + dr, nc = c + dc;
                if (nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS && mineData[nr][nc] == -1)
                    n++;
            }
        return n;
    }

    private JPanel createTop() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        // 返回菜单按钮
        JButton backBtn = new JButton("返回菜单") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(250, 140, 140));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        backBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        backBtn.setForeground(Color.WHITE);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setPreferredSize(new Dimension(120, 40));
        backBtn.addActionListener(e -> {
            if (gameTimer != null) gameTimer.stop();
            dispose();
            new DifficultyMenu().setVisible(true);
        });

        mineLabel = new JLabel("💣 " + MINES);
        mineLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));

        // 重新开始按钮（修复文字丢失）
        restartBtn = new JButton("重新开始") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 215, 0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        restartBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        restartBtn.setForeground(Color.BLACK);
        restartBtn.setContentAreaFilled(false);
        restartBtn.setBorderPainted(false);
        restartBtn.setFocusPainted(false);
        restartBtn.setPreferredSize(new Dimension(140, 40));
        restartBtn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { restartBtn.setText("😯"); }
            public void mouseReleased(MouseEvent e) { restart(); }
        });

        timeLabel = new JLabel("⏱ 0");
        timeLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(backBtn);
        left.add(mineLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(timeLabel);

        p.add(left, BorderLayout.WEST);
        p.add(restartBtn, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel createBoard() {
        JPanel p = new JPanel(new GridLayout(ROWS, COLS, 1, 1));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                MineButton btn = new MineButton();
                buttons[i][j] = btn;
                p.add(btn);

                int fi = i, fj = j;
                btn.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        if (!isGameStarted) { startTimer(); isGameStarted = true; }
                        if (SwingUtilities.isRightMouseButton(e)) {
                            setFlag(fi, fj);
                        } else {
                            if (scanPoint != null) {
                                scanPoint.move(fi, fj);
                                useSkill4Real();
                                scanPoint = null;
                            } else {
                                openCell(fi, fj);
                            }
                        }
                    }
                });
            }
        }
        return p;
    }

    // 连锁翻开逻辑
    private void openCell(int r, int c) {
        if (r < 0 || r >= ROWS || c < 0 || c >= COLS || revealed[r][c] || flagged[r][c]) return;

        revealed[r][c] = true;
        MineButton btn = buttons[r][c];
        btn.open();

        int n = mineData[r][c];
        if (n == -1) {
            gameTimer.stop();
            restartBtn.setText("💥");
            showAllMines();
            JOptionPane.showMessageDialog(this, "💣 游戏结束！");
            return;
        }

        if (n > 0) {
            btn.setText(String.valueOf(n));
            btn.setColor(n);
            checkWin();
            return;
        }

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                openCell(r + dr, c + dc);
            }
        }

        checkWin();
    }

    // 胜利判定（双条件）
    private boolean checkWin() {
        int revealedCount = 0;
        int correctFlagCount = 0;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (revealed[i][j]) revealedCount++;
                if (flagged[i][j] && mineData[i][j] == -1) correctFlagCount++;
            }
        }
        boolean win = (revealedCount == ROWS * COLS - MINES) && (correctFlagCount == MINES);
        if (win) {
            gameTimer.stop();
            restartBtn.setText("😎");
            JOptionPane.showMessageDialog(this, "🎉 恭喜通关！用时：" + timeCount + "秒");
        }
        return win;
    }

    private void showAllMines() {
        for (int i = 0; i < ROWS; i++)
            for (int j = 0; j < COLS; j++)
                if (mineData[i][j] == -1) {
                    buttons[i][j].open();
                    buttons[i][j].setText("💣");
                }
    }

    private void startTimer() {
        gameTimer = new Timer(1000, e -> {
            timeCount++;
            timeLabel.setText("⏱ " + timeCount);
        });
        gameTimer.start();
    }

    private void restart() {
        if (gameTimer != null) gameTimer.stop();
        dispose();
        new MinesweeperGame(ROWS, COLS, MINES,
                enableSkill1, enableSkill2, enableSkill3, enableSkill4, enableSkill5).setVisible(true);
    }

    private void setFlag(int r, int c) {
        if (revealed[r][c]) return;
        flagged[r][c] = !flagged[r][c];
        buttons[r][c].setText(flagged[r][c] ? "🚩" : "");
        mineLabel.setText("💣 " + (MINES - (flagged[r][c] ? ++flagCount : --flagCount)));
        checkWin();
    }

    // ======================
    // 技能面板（彻底修复次数更新）
    // ======================
    private JPanel createSkillPanel() {
        // 用final面板，避免重复创建
        final JPanel sp = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        sp.setBackground(PANEL);

        // 技能标签：带次数说明
        skillLabel1 = new JLabel("1️⃣ 数字标记(" + skill1 + ")");
        skillLabel2 = new JLabel("2️⃣ 安全探测(" + skill2 + ")");
        skillLabel3 = new JLabel("3️⃣ 排除地雷(" + skill3 + ")");
        skillLabel4 = new JLabel("4️⃣ 区域扫描(" + skill4 + ")");
        skillLabel5 = new JLabel("5️⃣ 自动插旗(" + skill5 + ")");

        Font skillFont = new Font("微软雅黑", Font.BOLD, 14);
        skillLabel1.setFont(skillFont);
        skillLabel2.setFont(skillFont);
        skillLabel3.setFont(skillFont);
        skillLabel4.setFont(skillFont);
        skillLabel5.setFont(skillFont);

        // 技能点击事件
        skillLabel1.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { useSkill1(); }
        });
        skillLabel2.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { useSkill2(); }
        });
        skillLabel3.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { useSkill3(); }
        });
        skillLabel4.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { useSkill4(); }
        });
        skillLabel5.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { useSkill5(); }
        });

        // 只添加启用的技能
        if (enableSkill1) sp.add(skillLabel1);
        if (enableSkill2) sp.add(skillLabel2);
        if (enableSkill3) sp.add(skillLabel3);
        if (enableSkill4) sp.add(skillLabel4);
        if (enableSkill5) sp.add(skillLabel5);

        return sp;
    }

    // ======================
    // 技能1：数字标记（效果：高亮所有非雷格子，3秒后恢复）
    // ======================
    private void useSkill1() {
        if (!enableSkill1 || skill1 <= 0) return;
        skill1--;
        updateSkillLabel(1);

        // 高亮非雷格子
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (!revealed[i][j] && !flagged[i][j] && mineData[i][j] != -1) {
                    buttons[i][j].setBackground(new Color(220, 240, 255));
                }
            }
        }
        // 3秒后恢复
        new Timer(3000, e -> {
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    if (!revealed[i][j] && !flagged[i][j]) {
                        buttons[i][j].setBackground(BTN_CLOSE);
                    }
                }
            }
        }).start();
    }

    // ======================
    // 技能2：安全探测（效果：随机标记1个安全格子，3秒后恢复）
    // ======================
    private void useSkill2() {
        if (!enableSkill2 || skill2 <= 0) return;
        skill2--;
        updateSkillLabel(2);

        ArrayList<Point> safeList = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (mineData[i][j] != -1 && !revealed[i][j] && !flagged[i][j]) {
                    safeList.add(new Point(i, j));
                }
            }
        }
        if (!safeList.isEmpty()) {
            Point p = safeList.get(new Random().nextInt(safeList.size()));
            buttons[p.x][p.y].setBackground(new Color(180, 230, 255));
            new Timer(3000, e -> {
                if (!revealed[p.x][p.y] && !flagged[p.x][p.y]) {
                    buttons[p.x][p.y].setBackground(BTN_CLOSE);
                }
            }).start();
        }
    }

    // ======================
    // 技能3：排除地雷（效果：随机标记1个安全格子，显示✓）
    // ======================
    private void useSkill3() {
        if (!enableSkill3 || skill3 <= 0) return;
        skill3--;
        updateSkillLabel(3);

        ArrayList<Point> safeList = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (mineData[i][j] != -1 && !revealed[i][j] && !flagged[i][j]) {
                    safeList.add(new Point(i, j));
                }
            }
        }
        if (!safeList.isEmpty()) {
            Point p = safeList.get(new Random().nextInt(safeList.size()));
            buttons[p.x][p.y].setText("✓");
        }
    }

    // ======================
    // 技能4：区域扫描（效果：点击格子，扫描周围8格，红=雷/黄=安全，3秒后恢复）
    // ======================
    private void useSkill4() {
        if (!enableSkill4 || skill4 <= 0) return;
        skill4--;
        updateSkillLabel(4);
        scanPoint = new Point(-1, -1);
        JOptionPane.showMessageDialog(this, "✅ 已激活区域扫描！\n点击任意格子，扫描周围8格");
    }

    private void useSkill4Real() {
        if (scanPoint == null) return;
        int r = scanPoint.x, c = scanPoint.y;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = r + dr, nc = c + dc;
                if (nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS && !revealed[nr][nc] && !flagged[nr][nc]) {
                    buttons[nr][nc].setBackground(mineData[nr][nc] == -1 ?
                            new Color(255, 180, 180) : new Color(255, 240, 180));
                }
            }
        }
        // 3秒后恢复
        new Timer(3000, e -> {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = r + dr, nc = c + dc;
                    if (nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS && !revealed[nr][nc] && !flagged[nr][nc]) {
                        buttons[nr][nc].setBackground(BTN_CLOSE);
                    }
                }
            }
        }).start();
    }

    // ======================
    // 技能5：自动插旗（效果：随机给1个未插旗的雷插旗）
    // ======================
    private void useSkill5() {
        if (!enableSkill5 || skill5 <= 0) return;
        skill5--;
        updateSkillLabel(5);

        ArrayList<Point> mineList = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (mineData[i][j] == -1 && !flagged[i][j]) {
                    mineList.add(new Point(i, j));
                }
            }
        }
        if (!mineList.isEmpty()) {
            Point p = mineList.get(new Random().nextInt(mineList.size()));
            setFlag(p.x, p.y);
        }
    }

    // ======================
    // 核心修复：更新技能次数 + 灰显不可用
    // ======================
    private void updateSkillLabel(int skillNum) {
        // 先更新标签文字
        switch (skillNum) {
            case 1 -> {
                skillLabel1.setText("1️⃣ 数字标记(" + skill1 + ")");
                if (skill1 <= 0) skillLabel1.setForeground(Color.GRAY);
            }
            case 2 -> {
                skillLabel2.setText("2️⃣ 安全探测(" + skill2 + ")");
                if (skill2 <= 0) skillLabel2.setForeground(Color.GRAY);
            }
            case 3 -> {
                skillLabel3.setText("3️⃣ 排除地雷(" + skill3 + ")");
                if (skill3 <= 0) skillLabel3.setForeground(Color.GRAY);
            }
            case 4 -> {
                skillLabel4.setText("4️⃣ 区域扫描(" + skill4 + ")");
                if (skill4 <= 0) skillLabel4.setForeground(Color.GRAY);
            }
            case 5 -> {
                skillLabel5.setText("5️⃣ 自动插旗(" + skill5 + ")");
                if (skill5 <= 0) skillLabel5.setForeground(Color.GRAY);
            }
        }
        // 强制刷新面板
        skillLabel1.repaint();
        skillLabel2.repaint();
        skillLabel3.repaint();
        skillLabel4.repaint();
        skillLabel5.repaint();
        getContentPane().revalidate();
        getContentPane().repaint();
    }
}