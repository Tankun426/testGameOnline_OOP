import java.awt.*;
import java.util.Random;
import javax.swing.*;

public class Meteor extends GameObject {
    private static Image[] pictures = new Image[10];
    private static Random random = new Random();
    static {
        for (int i = 0; i < 10; i++) {
            pictures[i] = new ImageIcon(
                    Meteor.class.getResource("img/newMeteo" + (i + 1) + ".png")).getImage();
        }
    }

    private int picture;

    public Meteor() {
        super(0, 0, 0, 0, null);
        reset();
    }

    public Meteor(int x, int y, int size, int picture) {
        super(x, y, size, size, pictures[picture]);
        this.picture = picture;
    }

    public void reset() {
        width = random.nextInt(61) + 20;

        height = width;
        x = random.nextInt(MainGame.GAME_WIDTH - width);
        y = -height - random.nextInt(400);

        int speedLevel = random.nextInt(3) + 1;
        speed = speedLevel * 2;

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

    public int getPoints() {
        if (width <= 35) {
            return 30;
        } else if (width <= 55 && width >= 36 ) {
            return 20;
        } else {
            return 10;
        }
    }

    public int getPicture() {
        return picture;
    }
}
