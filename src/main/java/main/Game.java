package main;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Game implements Runnable {
    private final GamePanel panel;

    public Game() {
        panel = new GamePanel();
        new GameWindow(panel);
    }

    public void update() {
        panel.updateGame();
    }

    @Override
    public void run() {
        SwingUtilities.invokeLater(() -> {
            Timer timer = new Timer(16, event -> {
                update();
                panel.repaint();
            });
            timer.start();
            panel.requestFocusInWindow();
        });
    }
}
