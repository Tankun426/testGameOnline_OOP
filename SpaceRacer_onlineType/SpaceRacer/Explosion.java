import java.awt.*;
import javax.swing.*;

// ภาพระเบิดตอนยิงโดน แสดงสักพักแล้วหายไป
public class Explosion extends GameObject {
    private static Image bombPic = new ImageIcon("SpaceRacer_onlineType/SpaceRacer/img/bomb.gif").getImage();
    private int time = 20; // แสดง 20 รอบของ game loop

    public Explosion(int x, int y, int size) {
        super(x, y, size, size, bombPic);
    }

    public void move() {
        time--;
        if (time <= 0) {
            alive = false;
        }
    }

    protected void drawShape(Graphics g) {
        g.setColor(Color.ORANGE);
        g.fillOval(x, y, width, height);
    }
}
