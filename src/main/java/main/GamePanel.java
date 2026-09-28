package main;

import Inputs.Keyinputs;
import Inputs.mouseInputs;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import static utilities.constants.Directions.*;
import static utilities.constants.playerConstants.*;

public class GamePanel extends JPanel {
    private final mouseInputs MouseInputs;
    private int xDelta = 15, yDelta = 15;
    private BufferedImage img;

    private int aniTick, aniIndex, aniSpeed = 20;
    private BufferedImage[][] animations;
    private int playerAction; // Default action
    private int playerDirection = -1; // Default direction
    private boolean moving = false;


    public GamePanel() {
        setFocusable(true);
        setBackground(Color.BLACK);
        MouseInputs = new mouseInputs(this);
        addKeyListener(new Keyinputs(this));
        addMouseListener(MouseInputs);
        addMouseMotionListener(MouseInputs);
        setPanelSize();

        try {
            importImg();
            loadAnimation();
        } catch (Exception e) {
            System.err.println("Image load failed: " + e.getMessage());
        }
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

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        updateAnimationTick();
        if (img != null) {
            g.drawImage(animations[playerAction][aniIndex], xDelta, yDelta,256, 160, null);
        }
        setAnimation();
        updatePos();
    }

    private final void setPanelSize() {
        Dimension size = new Dimension(1280, 800);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
    }

    private void importImg() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/res/player_sprites.png")) {
            if (is == null) {
                throw new IOException("Resource not found: /res/player_sprites.png");
            }
            img = ImageIO.read(is);
    }
}

    private void loadAnimation() {
        animations = new BufferedImage[9][6];
        for (int j = 0; j < animations.length; j++)
            for (int i = 0; i < animations[j].length; i++)
                animations[j][i] = img.getSubimage(i * 64, j * 40, 64, 40);
    }
    private void updateAnimationTick() {


    aniTick++;

        if (aniTick >= aniSpeed) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= GetCatAmount(playerAction)) {
                aniIndex = 0;
            }
        }





    }



        public void setDirections(int direction) {
            this.playerDirection = direction;
            this.moving=true;
}
public void setMoving(boolean moving) {
    this.moving = moving;


}
private void setAnimation(){

    if(moving){
        playerAction=RUNNING;
    }else{
        playerAction=IDLE;
    }
}


private void updatePos(){
if(moving){
    switch(playerDirection){
        case LEFT:
            xDelta-=5;
            break;
        case RIGHT:
            xDelta+=5;
            break;
        case UP:
            yDelta-=5;
            break;
        case DOWN:
            yDelta+=5;
            break;
    }
}
}
}
