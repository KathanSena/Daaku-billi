package main;

import java.awt.Color;
import javax.swing.JFrame;

public class GameWindow {
    private final JFrame frame = new JFrame();

    public GameWindow(GamePanel panel) {
        frame.setBackground(Color.BLACK);
        frame.setTitle("Daaku billi");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setSize(1920, 1080);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        panel.requestFocusInWindow();
        frame.pack();

    }
}
