package main;

public class Game implements Runnable {
    private final GamePanel panel;
    private final int FPS = 120;

    public Game() {
        panel = new GamePanel();
        new GameWindow(panel);
        panel.requestFocusInWindow();
    }

    @Override
    public void run() {
        double timePerTick = 1_000_000_000.0 / FPS;
        long lastTime = System.nanoTime();
        long now;
        int frames = 0;
        long lastCheck = System.currentTimeMillis();

        while (true) {
            now = System.nanoTime();

            if (now - lastTime >= timePerTick) {
                panel.repaint();
                lastTime = now;
                frames++;
            }

            if (System.currentTimeMillis() - lastCheck >= 1000) {
                lastCheck = System.currentTimeMillis();
                System.out.println("FPS: " + frames);
                frames = 0;
            }
        }
    }
}
