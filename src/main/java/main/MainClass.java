package main;

public class MainClass {
    public static void main(String[] args) {
        Thread gameThread = new Thread(new Game());
        gameThread.start();
    }
}
