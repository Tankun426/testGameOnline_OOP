import java.awt.*;
import javax.swing.*;

public class Bullet extends GameObject {
    private int ownerId;
    private static Image BulletPic = new ImageIcon(
            Bullet.class.getResource("/img/WaLenti Cristiano Ronaldinho .jpg")).getImage();

    public Bullet(int x, int y, int ownerId) {
        super(x, y, 20, 20, BulletPic);
        this.ownerId = ownerId;
        this.speed = 12;
    }

    public void move() {
        y = y - speed;
        if (y + height < 0) {
            alive = false;
        }
    }

    public int getOwnerId() {
        return ownerId;
    }
}
