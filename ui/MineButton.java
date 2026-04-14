package ui;

import javax.swing.*;
import java.awt.*;

public class MineButton extends JLabel {
    private static final int SIZE = 36;
    private static final Color CLOSE = new Color(220, 230, 240);
    private static final Color OPEN = Color.WHITE;

    public MineButton() {
        setHorizontalAlignment(SwingConstants.CENTER);
        setPreferredSize(new Dimension(SIZE, SIZE));
        setBackground(CLOSE);
        setOpaque(true);
        setFont(new Font("Segoe UI", Font.BOLD, 16));
    }

    public void open() {
        setBackground(OPEN);
    }

    public void setColor(int n) {
        switch (n) {
            case 1 -> setForeground(new Color(0, 80, 255));
            case 2 -> setForeground(new Color(30, 160, 50));
            case 3 -> setForeground(new Color(220, 40, 40));
            case 4 -> setForeground(new Color(120, 0, 180));
            case 5 -> setForeground(new Color(255, 140, 0));
            default -> setForeground(Color.BLACK);
        }
    }
}