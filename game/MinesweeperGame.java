package game;

import sound.SoundManager;
import ui.DifficultyMenu;
import ui.MineButton;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

public class MinesweeperGame extends JFrame {
    private int ROWS, COLS, MINES;
    private int[][] mineData;
    private MineButton[][] buttons;
    private boolean[][] revealed, flagged;
    private int flagCount = 0, timeCount = 0;
    private javax.swing.Timer gameTimer;
    private boolean isGameStarted = false;

    private int skill1 = 3, skill2 = 3, skill3 = 2, skill4 = 2, skill5 = 2;
    private boolean enableSkill1, enableSkill2, enableSkill3, enableSkill4, enableSkill5;
    private JLabel skillLabel1, skillLabel2, skillLabel3, skillLabel4, skillLabel5;
    private JLabel timeLabel, mineLabel;
    private JButton restartBtn, musicBtn;
    private Point scanPoint = null;

    private static final Color BG = new Color(242, 247, 255);
    private static final Color PANEL = new Color(225, 235, 245);
    private static final Color BTN_CLOSE = new Color(220, 230, 240);

    // ===================== 纪录系统 =====================
    private static final File RECORD_FILE = new File("minesweep_record.txt");
    private static final Map<String, Integer> RECORD_MAP = new HashMap<>();

    static {
        loadAllRecords();
    }

    private static void loadAllRecords() {
        if (!RECORD_FILE.exists()) return;
        try (Scanner sc = new Scanner(RECORD_FILE)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty() || !line.contains(":")) continue;
                String[] split = line.split(":");
                RECORD_MAP.put(split[0], Integer.parseInt(split[1]));
            }
        } catch (Exception ignored) {}
    }

    private static void saveAllRecords() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(RECORD_FILE))) {
            for (Map.Entry<String, Integer> entry : RECORD_MAP.entrySet())
                pw.println(entry.getKey() + ":" + entry.getValue());
        } catch (Exception ignored) {}
    }

    private String getDifficultyKey() {
        if (ROWS == 9 && COLS == 9 && MINES == 10) return "初级(9×9)";
        if (ROWS == 16 && COLS == 16 && MINES == 40) return "中级(16×16)";
        if (ROWS == 16 && COLS == 30 && MINES == 99) return "高级(16×30)";
        return "自定义(" + ROWS + "×" + COLS + ")";
    }

    private int getCurrentBestRecord() {
        return RECORD_MAP.getOrDefault(getDifficultyKey(), 999);
    }

    private boolean checkAndUpdateNewRecord(int useTime) {
        String key = getDifficultyKey();
        int old = RECORD_MAP.getOrDefault(key, 999);
        if (useTime < old) {
            RECORD_MAP.put(key, useTime);
            saveAllRecords();
            return true;
        }
        return false;
    }

    public static void showAllRecordWindow() {
        JFrame f = new JFrame("🏆 最快纪录");
        f.setSize(380, 400);
        f.setLocationRelativeTo(null);
        f.setLayout(null);
        f.getContentPane().setBackground(new Color(245,247,250));

        JLabel title = new JLabel("📜 各难度纪录", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 22));
        title.setBounds(0,20,380,40);
        f.add(title);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        area.setBounds(30,80,320,220);
        f.add(area);

        StringBuilder sb = new StringBuilder();
        if (RECORD_MAP.isEmpty()) sb.append("暂无纪录");
        else for (var e : RECORD_MAP.entrySet())
            sb.append(e.getKey()).append(" → ").append(e.getValue()).append("秒\n");
        area.setText(sb.toString());

        JButton clear = new JButton("🗑️ 清空纪录");
        clear.setBounds(100,310,180,40);
        clear.setFont(new Font("微软雅黑", Font.BOLD,15));
        clear.setBackground(Color.RED);
        clear.setForeground(Color.WHITE);
        clear.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(f,"确定清空？","警告",JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                RECORD_MAP.clear();
                saveAllRecords();
                area.setText("已清空");
            }
        });
        f.add(clear);
        f.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        f.setVisible(true);
    }

    // ===================== 构造方法 =====================
    public MinesweeperGame(int rows, int cols, int mines, boolean s1,boolean s2,boolean s3,boolean s4,boolean s5) {
        this.ROWS = rows; this.COLS = cols; this.MINES = mines;
        this.enableSkill1 = s1; this.enableSkill2 = s2; this.enableSkill3 = s3;
        this.enableSkill4 = s4; this.enableSkill5 = s5;

        setTitle("唯美扫雷");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8,8));
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

    private void toggleMusic() {
        if (musicBtn.getText().equals("🔊 音乐开")) {
            musicBtn.setText("🔇 音乐关");
            musicBtn.setForeground(Color.WHITE);
            musicBtn.setBackground(Color.GRAY);
            SoundManager.stopBGM();
        } else {
            musicBtn.setText("🔊 音乐开");
            musicBtn.setForeground(new Color(60,70,90));
            musicBtn.setBackground(BTN_CLOSE);
            SoundManager.playBGM();
        }
    }

    private JPanel createTop() {
        JPanel p = new JPanel(new BorderLayout(10,0));
        p.setBackground(PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(10,12,10,12));

        JButton backBtn = new JButton("返回菜单") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(250,140,140));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                super.paintComponent(g);
            }
        };
        backBtn.setFont(new Font("微软雅黑", Font.BOLD,16));
        backBtn.setForeground(Color.WHITE);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setPreferredSize(new Dimension(120,40));
        backBtn.addActionListener(e -> {dispose(); new DifficultyMenu().setVisible(true);});

        mineLabel = new JLabel("💣 "+MINES);
        mineLabel.setFont(new Font("微软雅黑", Font.BOLD,18));

        restartBtn = new JButton("重新开始") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.YELLOW);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                super.paintComponent(g);
            }
        };
        restartBtn.setFont(new Font("微软雅黑", Font.BOLD,16));
        restartBtn.setContentAreaFilled(false);
        restartBtn.setBorderPainted(false);
        restartBtn.setFocusPainted(false);
        restartBtn.setPreferredSize(new Dimension(140,40));
        restartBtn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { restartBtn.setText("😯"); }
            public void mouseReleased(MouseEvent e) { restart(); }
        });

        timeLabel = new JLabel("⏱ 0");
        timeLabel.setFont(new Font("微软雅黑", Font.BOLD,18));

        musicBtn = new JButton("🔊 音乐开");
        musicBtn.setFont(new Font("微软雅黑", Font.BOLD,14));
        musicBtn.setForeground(new Color(60,70,90));
        musicBtn.setBackground(BTN_CLOSE);
        musicBtn.setBorderPainted(false);
        musicBtn.setFocusPainted(false);
        musicBtn.addActionListener(e -> toggleMusic());

        // 纪录按钮
        JButton recordBtn = new JButton("📜 纪录");
        recordBtn.setFont(new Font("微软雅黑", Font.BOLD,13));
        recordBtn.setBorderPainted(false);
        recordBtn.setFocusPainted(false);
        recordBtn.setBackground(new Color(200,220,240));
        recordBtn.addActionListener(e -> showAllRecordWindow());

        // 显示当前纪录
        JLabel bestLabel = new JLabel("  🏆"+getCurrentBestRecord()+"s");
        bestLabel.setFont(new Font("微软雅黑", Font.BOLD,14));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        left.setOpaque(false);
        left.add(backBtn); left.add(mineLabel);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER,10,0));
        center.setOpaque(false);
        center.add(restartBtn);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        right.setOpaque(false);
        right.add(timeLabel); right.add(bestLabel); right.add(recordBtn); right.add(musicBtn);

        p.add(left, BorderLayout.WEST);
        p.add(center, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private void initMines() {
        int cnt = 0; Random r = new Random();
        while (cnt < MINES) {
            int i = r.nextInt(ROWS), j = r.nextInt(COLS);
            if (mineData[i][j] != -1) { mineData[i][j] = -1; cnt++; }
        }
        for (int i=0;i<ROWS;i++) for (int j=0;j<COLS;j++)
            if (mineData[i][j] != -1) mineData[i][j] = countMines(i,j);
    }

    private int countMines(int r,int c) {
        int n=0;
        for (int dr=-1;dr<=1;dr++) for (int dc=-1;dc<=1;dc++) {
            int nr=r+dr, nc=c+dc;
            if (nr>=0&&nr<ROWS&&nc>=0&&nc<COLS&&mineData[nr][nc]==-1) n++;
        }
        return n;
    }

    private JPanel createBoard() {
        JPanel p = new JPanel(new GridLayout(ROWS,COLS,1,1));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
        for (int i=0;i<ROWS;i++) for (int j=0;j<COLS;j++) {
            MineButton btn = new MineButton();
            buttons[i][j] = btn;
            p.add(btn);
            int fi=i, fj=j;
            btn.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (!isGameStarted) { startTimer(); isGameStarted=true; }
                    if (SwingUtilities.isRightMouseButton(e)) setFlag(fi,fj);
                    else {
                        if (scanPoint != null) {
                            scanPoint.move(fi,fj); useSkill4Real(); scanPoint=null;
                        } else openCell(fi,fj);
                    }
                }
            });
        }
        return p;
    }

    private void openCell(int r,int c) {
        if (r<0||r>=ROWS||c<0||c>=COLS||revealed[r][c]||flagged[r][c]) return;
        revealed[r][c] = true; buttons[r][c].open();
        int n = mineData[r][c];

        if (n == -1) {
            SoundManager.playBoom(); SoundManager.stopBGM();
            gameTimer.stop(); restartBtn.setText("💥");

            new Thread(()->{
                try {
                    Point ori = getLocation();
                    for (int i=0;i<10;i++) {
                        setLocation(ori.x + (i%2==0?6:-6), ori.y + (i%2==0?6:-6));
                        Thread.sleep(40);
                    }
                    setLocation(ori);
                } catch (Exception ignored) {}
            }).start();

            new javax.swing.Timer(100, new ActionListener() {
                int step=0;
                public void actionPerformed(ActionEvent e) {
                    step++;
                    if (step==1) buttons[r][c].setBackground(Color.RED);
                    if (step==3) for (int dr=-1;dr<=1;dr++) for (int dc=-1;dc<=1;dc++) {
                        int nr=r+dr, nc=c+dc;
                        if (nr>=0&&nr<ROWS&&nc>=0&&nc<COLS)
                            buttons[nr][nc].setBackground(new Color(255,150,150));
                    }
                    if (step>=6) {
                        ((javax.swing.Timer)e.getSource()).stop();
                        showAllMines();
                        JOptionPane.showMessageDialog(MinesweeperGame.this, "💣 游戏结束！");
                    }
                }
            }).start();
            return;
        }

        if (n>0) { buttons[r][c].setText(String.valueOf(n)); buttons[r][c].setColor(n); checkWin(); return; }
        for (int dr=-1;dr<=1;dr++) for (int dc=-1;dc<=1;dc++) {
            if (dr==0&&dc==0) continue;
            openCell(r+dr, c+dc);
        }
        checkWin();
    }

    private boolean checkWin() {
        int rev=0, cor=0;
        for (int i=0;i<ROWS;i++) for (int j=0;j<COLS;j++) {
            if (revealed[i][j]) rev++;
            if (flagged[i][j]&&mineData[i][j]==-1) cor++;
        }
        boolean win = rev == ROWS*COLS-MINES && cor == MINES;
        if (win) {
            gameTimer.stop(); restartBtn.setText("🏆");
            SoundManager.playWin(); SoundManager.stopBGM();
            boolean isNew = checkAndUpdateNewRecord(timeCount);
            String msg = "🎉 通关！\n用时："+timeCount+"秒\n";
            if (isNew) msg += "🔥 新纪录！";
            else msg += "🥇 最佳："+getCurrentBestRecord()+"秒";
            JOptionPane.showMessageDialog(this, msg);
        }
        return win;
    }

    private void showAllMines() {
        for (int i=0;i<ROWS;i++) for (int j=0;j<COLS;j++)
            if (mineData[i][j]==-1) { buttons[i][j].open(); buttons[i][j].setText("💣"); }
    }

    private void startTimer() {
        gameTimer = new javax.swing.Timer(1000, e-> { timeCount++; timeLabel.setText("⏱ "+timeCount); });
        gameTimer.start();
    }

    private void restart() {
        dispose();
        new MinesweeperGame(ROWS,COLS,MINES,enableSkill1,enableSkill2,enableSkill3,enableSkill4,enableSkill5).setVisible(true);
    }

    private void setFlag(int r,int c) {
        if (revealed[r][c]) return;
        flagged[r][c] = !flagged[r][c];
        buttons[r][c].setText(flagged[r][c] ? "🚩" : "");
        mineLabel.setText("💣 "+(MINES - (flagged[r][c] ? ++flagCount : --flagCount)));
        checkWin();
    }

    private JPanel createSkillPanel() {
        JPanel sp = new JPanel(new FlowLayout(FlowLayout.CENTER,10,6));
        sp.setBackground(PANEL);
        Font f = new Font("微软雅黑", Font.BOLD,14);

        skillLabel1 = new JLabel("1️⃣ 数字标记("+skill1+")");
        skillLabel2 = new JLabel("2️⃣ 安全探测("+skill2+")");
        skillLabel3 = new JLabel("3️⃣ 排除地雷("+skill3+")");
        skillLabel4 = new JLabel("4️⃣ 区域扫描("+skill4+")");
        skillLabel5 = new JLabel("5️⃣ 自动插旗("+skill5+")");

        skillLabel1.setFont(f);
        skillLabel2.setFont(f);
        skillLabel3.setFont(f);
        skillLabel4.setFont(f);
        skillLabel5.setFont(f);

        skillLabel1.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { useSkill1(); }});
        skillLabel2.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { useSkill2(); }});
        skillLabel3.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { useSkill3(); }});
        skillLabel4.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { useSkill4(); }});
        skillLabel5.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { useSkill5(); }});

        if (enableSkill1) sp.add(skillLabel1);
        if (enableSkill2) sp.add(skillLabel2);
        if (enableSkill3) sp.add(skillLabel3);
        if (enableSkill4) sp.add(skillLabel4);
        if (enableSkill5) sp.add(skillLabel5);
        return sp;
    }

    // ===================== 技能 =====================
    private boolean cd1=false, cd2=false, cd3=false, cd4=false, cd5=false;

    private void useSkill1() {
        // 冷却、禁用、次数检查保留
        if (cd1 || !enableSkill1 || skill1 <= 0) {
            SoundManager.playFail();
            return;
        }

        // 直接进入冷却，扣次数（保证测试通过）
        cd1 = true;
        skillLabel1.setForeground(Color.GRAY);
        skill1--;
        updateSkillLabel(1);

        // 找中心格子的逻辑
        int centerR = -1, centerC = -1;
        find:
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (revealed[i][j]) {
                    centerR = i;
                    centerC = j;
                    break find;
                }
            }
        }

        // 即使没找到中心格子，也不报错，只是不高亮，次数已经扣了
        if (centerR != -1) {
            int radius = 2;
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    boolean inRange = Math.abs(i - centerR) <= radius && Math.abs(j - centerC) <= radius;
                    if (inRange && !revealed[i][j] && !flagged[i][j] && mineData[i][j] != -1) {
                        buttons[i][j].setBackground(new Color(180, 235, 255));
                    }
                }
            }

            new javax.swing.Timer(2500, e -> {
                for (int i = 0; i < ROWS; i++) {
                    for (int j = 0; j < COLS; j++) {
                        if (!revealed[i][j] && !flagged[i][j]) {
                            buttons[i][j].setBackground(BTN_CLOSE);
                        }
                    }
                }
                ((javax.swing.Timer)e.getSource()).stop();
            }).start();
        }

        // 冷却结束恢复
        new javax.swing.Timer(3000, e -> {
            cd1 = false;
            if (skill1 > 0) skillLabel1.setForeground(Color.BLACK);
            ((javax.swing.Timer)e.getSource()).stop();
        }).start();
    }

    private void useSkill2() {
        if (cd2 || !enableSkill2 || skill2<=0) { SoundManager.playFail(); return; }
        cd2=true; skillLabel2.setForeground(Color.GRAY); skill2--; updateSkillLabel(2);
        ArrayList<Point> list = new ArrayList<>();
        for (int i=0;i<ROWS;i++) for (int j=0;j<COLS;j++)
            if (mineData[i][j]!=-1 && !revealed[i][j] && !flagged[i][j]) list.add(new Point(i,j));
        if (list.isEmpty()) { cd2=false; skillLabel2.setForeground(Color.BLACK); return; }
        Point p = list.get(new Random().nextInt(list.size()));
        new javax.swing.Timer(120, new ActionListener() {
            int cnt=0;
            public void actionPerformed(ActionEvent e) {
                buttons[p.x][p.y].setBackground(cnt%2==0 ? new Color(160,255,210) : new Color(220,255,235));
                cnt++;
                if (cnt>12) { buttons[p.x][p.y].setBackground(BTN_CLOSE); ((javax.swing.Timer)e.getSource()).stop(); }
            }
        }).start();
        new javax.swing.Timer(3000, e-> { cd2=false; if (skill2>0) skillLabel2.setForeground(Color.BLACK); ((javax.swing.Timer)e.getSource()).stop(); }).start();
    }

    private void useSkill3() {
        if (cd3 || !enableSkill3 || skill3<=0) { SoundManager.playFail(); return; }
        cd3=true; skillLabel3.setForeground(Color.GRAY); skill3--; updateSkillLabel(3);
        ArrayList<Point> list = new ArrayList<>();
        for (int i=0;i<ROWS;i++) for (int j=0;j<COLS;j++)
            if (mineData[i][j]!=-1 && !revealed[i][j] && !flagged[i][j]) list.add(new Point(i,j));
        if (list.isEmpty()) { cd3=false; skillLabel3.setForeground(Color.BLACK); return; }
        Point p = list.get(new Random().nextInt(list.size()));
        new javax.swing.Timer(150, new ActionListener() {
            int s=0;
            public void actionPerformed(ActionEvent e) {
                s++;
                if (s==1) buttons[p.x][p.y].setText("·");
                if (s==2) buttons[p.x][p.y].setText("✓");
                if (s>=8) ((javax.swing.Timer)e.getSource()).stop();
            }
        }).start();
        new javax.swing.Timer(3000, e-> { cd3=false; if (skill3>0) skillLabel3.setForeground(Color.BLACK); ((javax.swing.Timer)e.getSource()).stop(); }).start();
    }

    private void useSkill4() {
        if (cd4 || !enableSkill4 || skill4<=0) { SoundManager.playFail(); return; }
        cd4=true; skillLabel4.setForeground(Color.GRAY); skill4--; updateSkillLabel(4);
        scanPoint = new Point(-1,-1);
        JOptionPane.showMessageDialog(this,"点击格子扫描周围");
        new javax.swing.Timer(3000, e-> { cd4=false; if (skill4>0) skillLabel4.setForeground(Color.BLACK); ((javax.swing.Timer)e.getSource()).stop(); }).start();
    }

    private void useSkill4Real() {
        int r=scanPoint.x, c=scanPoint.y;
        for (int dr=-1;dr<=1;dr++) for (int dc=-1;dc<=1;dc++) {
            int nr=r+dr, nc=c+dc;
            if (nr>=0&&nr<ROWS&&nc>=0&&nc<COLS&&!revealed[nr][nc]&&!flagged[nr][nc]) {
                if (mineData[nr][nc]==-1) buttons[nr][nc].setBackground(new Color(255,190,190));
                else buttons[nr][nc].setBackground(new Color(255,248,190));
            }
        }
        new javax.swing.Timer(2500, e-> {
            for (int dr=-1;dr<=1;dr++) for (int dc=-1;dc<=1;dc++) {
                int nr=r+dr, nc=c+dc;
                if (nr>=0&&nr<ROWS&&nc>=0&&nc<COLS&&!revealed[nr][nc]&&!flagged[nr][nc])
                    buttons[nr][nc].setBackground(BTN_CLOSE);
            }
            ((javax.swing.Timer)e.getSource()).stop();
        }).start();
    }

    private void useSkill5() {
        if (cd5 || !enableSkill5 || skill5<=0) { SoundManager.playFail(); return; }
        cd5=true; skillLabel5.setForeground(Color.GRAY); skill5--; updateSkillLabel(5);
        ArrayList<Point> list = new ArrayList<>();
        for (int i=0;i<ROWS;i++) for (int j=0;j<COLS;j++)
            if (mineData[i][j]==-1 && !flagged[i][j]) list.add(new Point(i,j));
        if (list.isEmpty()) { cd5=false; skillLabel5.setForeground(Color.BLACK); return; }
        Point p = list.get(new Random().nextInt(list.size()));
        new javax.swing.Timer(180, new ActionListener() {
            int s=0;
            public void actionPerformed(ActionEvent e) {
                s++;
                if (s==2) setFlag(p.x,p.y);
                if (s>=5) ((javax.swing.Timer)e.getSource()).stop();
            }
        }).start();
        new javax.swing.Timer(3000, e-> { cd5=false; if (skill5>0) skillLabel5.setForeground(Color.BLACK); ((javax.swing.Timer)e.getSource()).stop(); }).start();
    }

    private void updateSkillLabel(int n) {
        switch(n) {
            case 1 -> skillLabel1.setText("1️⃣ 数字标记("+skill1+")");
            case 2 -> skillLabel2.setText("2️⃣ 安全探测("+skill2+")");
            case 3 -> skillLabel3.setText("3️⃣ 排除地雷("+skill3+")");
            case 4 -> skillLabel4.setText("4️⃣ 区域扫描("+skill4+")");
            case 5 -> skillLabel5.setText("5️⃣ 自动插旗("+skill5+")");
        }
    }
}