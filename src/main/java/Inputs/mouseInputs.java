package Inputs;

import java.awt.event.MouseEvent;
import main.GamePanel;
public class mouseInputs implements java.awt.event.MouseListener, java.awt.event.MouseMotionListener {
   private GamePanel panel;
    public mouseInputs(GamePanel panel) {
        this.panel=panel;
    
    }
   
   
   
    public void mouseClicked(java.awt.event.MouseEvent e) {
        System.out.println("Mouse clicked at: " + e.getX() + ", " + e.getY());
    }

   
    public void mousePressed(java.awt.event.MouseEvent e) {
        System.out.println("Mouse pressed at: " + e.getX() + ", " + e.getY());
    }

    
    public void mouseReleased(java.awt.event.MouseEvent e) {
        System.out.println("Mouse released at: " + e.getX() + ", " + e.getY());
    }

   
    public void mouseEntered(java.awt.event.MouseEvent e) {
        System.out.println("Mouse entered the component.");
    }

    
    public void mouseExited(java.awt.event.MouseEvent e) {
        System.out.println("Mouse exited the component.");
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        panel.setRectanglePosition(e.getX(), e.getY());
    }

}
