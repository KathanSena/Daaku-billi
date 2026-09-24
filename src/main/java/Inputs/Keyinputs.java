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
       switch(e.getKeyCode()) {
           case KeyEvent.VK_W:
               panel.setYdelta(-5);
               break;
           case KeyEvent.VK_S:
               panel.setYdelta(5);
               break;
           case KeyEvent.VK_A:
               panel.setXdelta(-5);
               break;
           case KeyEvent.VK_D:
               panel.setXdelta(5);
               break;
           default:
               break;
       }
    }

    @Override
    public void keyReleased(KeyEvent e) {
       
    }
    
}
