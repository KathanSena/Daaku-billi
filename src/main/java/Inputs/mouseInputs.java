package Inputs;

import java.awt.event.MouseEvent;
import main.GamePanel;
public class mouseInputs implements java.awt.event.MouseListener, java.awt.event.MouseMotionListener {
   private GamePanel panel;
    public mouseInputs(GamePanel panel) {
        this.panel=panel;

    }



    public void mouseClicked(java.awt.event.MouseEvent e) {
    }


    public void mousePressed(java.awt.event.MouseEvent e) {
        panel.handleMouseClick(e.getX(), e.getY());
    }


    public void mouseReleased(java.awt.event.MouseEvent e) {
    }


    public void mouseEntered(java.awt.event.MouseEvent e) {
    }


    public void mouseExited(java.awt.event.MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Mouse movement is reserved for future menu hover effects.

    }

}
