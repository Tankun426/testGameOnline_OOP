import java.awt.*;

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

    public abstract void move();

    public void draw(Graphics g) {
        if (!alive) {
            return;
        }
        g.drawImage(image, x, y, width, height, null);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
}
