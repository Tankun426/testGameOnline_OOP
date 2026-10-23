import java.awt.*;
import java.util.Random;
import javax.swing.*;

// อุกกาบาต: สุ่มขนาด 3 แบบ และสุ่มความเร็ว 3 ระดับ
public class Meteor extends GameObject {
    private static Image[] pictures = new Image[10];
    private static Random random = new Random();

    // โหลดรูป meteor1.png - meteor10.png ครั้งเดียวตอนเริ่มโปรแกรม
    static {
        for (int i = 0; i < 10; i++) {
            pictures[i] = new ImageIcon("SpaceRacer_onlineType/SpaceRacer/img/meteor" + (i + 1) + ".png").getImage();
        }
    }

    private int picture;     // ใช้รูปที่เท่าไหร่ (0-9)
    private int speedLevel;  // ระดับความเร็ว 1-3

    // ใช้ตอนสร้างอุกกาบาตใหม่ (เครื่อง Host / Offline)
    public Meteor() {
        super(0, 0, 0, 0, null);
        reset();
    }

    // ใช้ตอนเครื่องลูก (Client) สร้างจากข้อมูลที่ Host ส่งมา
    public Meteor(int x, int y, int size, int picture) {
        super(x, y, size, size, pictures[picture]);
        this.picture = picture;
    }

    // สุ่มใหม่แล้วเริ่มตกจากข้างบน (คล้าย RainLetter)
    public void reset() {
        int type = random.nextInt(3);
        if (type == 0) {
            width = 35;   // เล็ก
        } else if (type == 1) {
            width = 55;   // กลาง
        } else {
            width = 80;   // ใหญ่
        }
        height = width;
        x = random.nextInt(MainGame.GAME_WIDTH - width);
        y = -height - random.nextInt(400);

        speedLevel = random.nextInt(3) + 1;  // สุ่มระดับ 1, 2, 3
        speed = speedLevel * 2;              // ระดับ 1 = 2, ระดับ 2 = 4, ระดับ 3 = 6

        picture = random.nextInt(10);
        image = pictures[picture];
        alive = true;
    }

    public void move() {
        y = y + speed;
    }

    public boolean isOutOfScreen() {
        return y > MainGame.GAME_HEIGHT;
    }

    // ลูกเล็กยิงยากกว่า ได้คะแนนมากกว่า
    public int getPoints() {
        if (width == 35) {
            return 30;
        } else if (width == 55) {
            return 20;
        } else {
            return 10;
        }
    }

    public int getPicture() {
        return picture;
    }

    protected void drawShape(Graphics g) {
        g.setColor(Color.GRAY);
        g.fillOval(x, y, width, height);
    }
}
