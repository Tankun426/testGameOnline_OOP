import java.awt.*;

// กระสุน จำไว้ว่าเป็นของผู้เล่นคนไหน (ownerId) จะได้บวกคะแนนถูกคน
public class Bullet extends GameObject {
    private int ownerId;

    public Bullet(int x, int y, int ownerId) {
        super(x, y, 6, 15, null);
        this.ownerId = ownerId;
        this.speed = 12;
    }

    public void move() {
        y = y - speed; // วิ่งขึ้นข้างบน
        if (y + height < 0) {
            alive = false; // พ้นจอแล้ว
        }
    }

    protected void drawShape(Graphics g) {
        g.setColor(Ship.COLORS[ownerId]);
        g.fillRect(x, y, width, height);
    }

    public int getOwnerId() {
        return ownerId;
    }
}
