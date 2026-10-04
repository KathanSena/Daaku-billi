package Inputs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import main.GamePanel;
public class Keyinputs implements KeyListener {
private GamePanel panel;
public Keyinputs(GamePanel panel) {
        this.panel = panel;
    }
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
       panel.setKey(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        panel.setKey(e.getKeyCode(), false);

    }

}
