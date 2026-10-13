import java.awt.*;

// คลาสแม่ (abstract) ของทุกอย่างที่อยู่ในเกม: ยาน, กระสุน, อุกกาบาต, ระเบิด
public abstract class GameObject {
    protected int x, y;
    protected int width, height;
    protected int speed;
    protected Image image;
    protected boolean alive = true;

    public GameObject(int x, int y, int width, int height, Image image) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
    }

    // ลูกแต่ละคลาสต้องเขียนเองว่าจะเคลื่อนที่ยังไง (Polymorphism)
    public abstract void move();

    public void draw(Graphics g) {
        if (!alive) {
            return;
        }
        if (image != null && image.getWidth(null) > 0) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            drawShape(g); // ถ้าไม่มีไฟล์รูป ให้วาดรูปทรงแทน
        }
    }

    // รูปทรงสำรอง (คลาสลูก override ได้)
    protected void drawShape(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillOval(x, y, width, height);
    }

    // กรอบสี่เหลี่ยมไว้เช็คการชน
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
