package Inputs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import main.GamePanel;
import static utilities.constants.Directions.*;
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
       switch(e.getKeyCode()) {
           case KeyEvent.VK_W:
               panel.setDirections(UP);
               break;
           case KeyEvent.VK_S:
               panel.setDirections(DOWN);
               break;
           case KeyEvent.VK_A:
               panel.setDirections(LEFT);
               break;
           case KeyEvent.VK_D:
               panel.setDirections(RIGHT);
               break;
           default:
               break;
       }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_S:
            case KeyEvent.VK_A:
            case KeyEvent.VK_D:
                panel.setMoving(false);
                break;
            default:
                break;
        }

    }

}
