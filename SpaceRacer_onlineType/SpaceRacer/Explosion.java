import java.awt.*;
import javax.swing.*;

// ภาพระเบิดตอนยิงโดน แสดงสักพักแล้วหายไป
public class Explosion extends GameObject {
    private static Image bombPic = new ImageIcon(
            Explosion.class.getResource("/img/bomb.gif")).getImage();
    private int time = 20;

    public Explosion(int x, int y, int size) {
        super(x, y, size, size, bombPic);
    }

    public void move() {
        time--;
        if (time <= 0) {
            alive = false;
        }
    }

}
