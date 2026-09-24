package main;

import javax.swing.JFrame;

public class GameWindow {
    private final JFrame frame = new JFrame();

    public GameWindow(GamePanel panel) {
        frame.setTitle("Daaku billi");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setSize(800, 600);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        panel.requestFocusInWindow();
    }
}
