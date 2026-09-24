
package main;

import Inputs.Keyinputs;
import Inputs.mouseInputs;
import java.awt.Graphics;
import javax.swing.JPanel;
public class GamePanel extends JPanel {
 
    private final mouseInputs MouseInputs;
    private int xDelta = 15,yDelta = 15;
   
   public GamePanel() {
       setFocusable(true);
        MouseInputs = new mouseInputs(this);
        addKeyListener(new Keyinputs(this));
        addMouseListener(MouseInputs);
        addMouseMotionListener(MouseInputs);
        
    }
   public void setXdelta(int xDelta) {
        this.xDelta += xDelta;
        repaint();
    }
    public void setYdelta(int yDelta) {
        this.yDelta += yDelta;
        repaint();
    }
    public void setRectanglePosition(int x, int y) {
        this.xDelta = x;
        this.yDelta = y;
        repaint();
    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.fillRect(xDelta, yDelta, 20, 20);
    }
}
