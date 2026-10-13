import java.awt.*;
import javax.swing.*;

// ยานอวกาศ + ข้อมูลผู้เล่น (ชื่อ คะแนน ชีวิต) รวมไว้ในคลาสเดียว
public class Ship extends GameObject {
    public static final Color[] COLORS = { Color.CYAN, Color.PINK, Color.GREEN };
    private static Image[] pictures = {
        new ImageIcon("img/ship1.png").getImage(),
        new ImageIcon("img/ship2.png").getImage(),
        new ImageIcon("img/ship3.png").getImage()
    };

    private int id;          // 0, 1, 2
    private int score = 0;
    private int lives = 3;
    private int shootDelay = 0; // กันยิงรัวเกินไป

    // ปุ่มที่ผู้เล่นคนนี้กดอยู่ (มาจากคีย์บอร์ด หรือมาจากเครื่องอื่นผ่าน LAN)
    private volatile boolean up, down, left, right, shoot;

    public Ship(int id, int x, int y) {
        super(x, y, 50, 50, pictures[id]);
        this.id = id;
        this.speed = 6;
    }

    public void setInput(boolean up, boolean down, boolean left, boolean right, boolean shoot) {
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
        this.shoot = shoot;
    }

    public void move() {
        if (up) y = y - speed;
        if (down) y = y + speed;
        if (left) x = x - speed;
        if (right) x = x + speed;

        // ห้ามออกนอกจอ
        if (x < 0) x = 0;
        if (x > MainGame.GAME_WIDTH - width) x = MainGame.GAME_WIDTH - width;
        if (y < 0) y = 0;
        if (y > MainGame.GAME_HEIGHT - height) y = MainGame.GAME_HEIGHT - height;

        if (shootDelay > 0) shootDelay--;
    }

    // ยิงอัตโนมัติทุก 12 เฟรม ขณะที่ยานยังมีชีวิต
    public boolean wantToShoot() {
        return alive && shootDelay == 0;
    }

    public void resetShootDelay() {
        shootDelay = 12;
    }

    public void addScore(int points) {
        score = score + points;
    }

    public void loseLife() {
        setLives(lives - 1);
    }

    public void setLives(int lives) {
        if (lives < 0) lives = 0;
        this.lives = lives;
        alive = lives > 0;
    }

    public void setScore(int score) { this.score = score; }
    public int getId() { return id; }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public String getName() { return "Player " + (id + 1); }
    public Color getColor() { return COLORS[id]; }

    // ไม่มีรูปยาน ให้วาดสามเหลี่ยมสีของผู้เล่นแทน
    protected void drawShape(Graphics g) {
        int[] px = { x + width / 2, x, x + width };
        int[] py = { y, y + height, y + height };
        g.setColor(getColor());
        g.fillPolygon(px, py, 3);
    }
}
