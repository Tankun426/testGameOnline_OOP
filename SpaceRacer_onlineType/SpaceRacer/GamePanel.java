import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.*;

public class GamePanel extends JPanel implements KeyListener, Runnable {
    public static final int OFFLINE = 0, HOST = 1, CLIENT = 2;
    public static final int GAME_TIME = 180; // 3 นาที

    private MainGame game;
    private int mode;
    private GameServer server;
    private GameClient client;
    private int myId = 0;

    private CopyOnWriteArrayList<Ship> ships = new CopyOnWriteArrayList<Ship>();
    private CopyOnWriteArrayList<Meteor> meteors = new CopyOnWriteArrayList<Meteor>();
    private CopyOnWriteArrayList<Bullet> bullets = new CopyOnWriteArrayList<Bullet>();
    private CopyOnWriteArrayList<Explosion> explosions = new CopyOnWriteArrayList<Explosion>();

    private volatile int timeLeft = GAME_TIME;
    private volatile boolean started = false;
    private volatile boolean gameOver = false;
    private volatile boolean running = true;
    private volatile long startTime;
    private volatile String message = "";

    private boolean[] keys = new boolean[256];

    private int[][] keyMap = {
            { KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D },
            { KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT },
            { KeyEvent.VK_I, KeyEvent.VK_K, KeyEvent.VK_J, KeyEvent.VK_L }
    };

    private Image background = new ImageIcon(getClass().getResource("img/bg1.jpg")).getImage();
    private int[] starX = new int[100];
    private int[] starY = new int[100];
    private JButton startButton = new JButton("START GAME");
    private String myIP = GameServer.getMyIP();

    public GamePanel(MainGame game, int mode, int numberOfPlayers) {
        this.game = game;
        this.mode = mode;
        setPreferredSize(new Dimension(MainGame.GAME_WIDTH, MainGame.GAME_HEIGHT));
        setBackground(Color.BLACK);
        setLayout(null);
        setFocusable(true);
        addKeyListener(this);

        Random random = new Random();
        for (int i = 0; i < starX.length; i++) {
            starX[i] = random.nextInt(MainGame.GAME_WIDTH);
            starY[i] = random.nextInt(MainGame.GAME_HEIGHT);
        }

        if (mode == CLIENT) {
            return;
        }

        for (int i = 0; i < numberOfPlayers; i++) {
            addShip();
        }
        for (int i = 0; i < 10; i++) {
            meteors.add(new Meteor());
        }

        if (mode == OFFLINE) {
            startGame();
        } else {
            startButton.setBounds(350, 420, 200, 50);
            startButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    startGame();
                }
            });
            add(startButton);
        }

        new Thread(this).start();
    }

    private void startGame() {
        startTime = System.currentTimeMillis();
        started = true;
        startButton.setVisible(false);
        requestFocusInWindow();
    }

    public void run() {
        while (running) {
            if (started) {
                update();
            }
            if (mode == HOST && server != null) {
                server.broadcast(buildState());
            }
            if (gameOver) {
                running = false;
            }
            repaint();
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
    }

    private void update() {
        if (mode == OFFLINE) {
            for (int i = 0; i < ships.size(); i++) {
                int[] k = keyMap[i];
                ships.get(i).setInput(keys[k[0]], keys[k[1]], keys[k[2]], keys[k[3]]);
            }
        } else {
            ships.get(0).setInput(onlineKey(0), onlineKey(1), onlineKey(2), onlineKey(3));
        }

        for (Ship s : ships) {
            if (!s.isAlive())
                continue;
            s.move();
            if (s.wantToShoot()) {
                bullets.add(new Bullet(s.getX() + s.getWidth() / 2 - 3, s.getY(), s.getId()));
                s.resetShootDelay();
            }
        }

        for (Bullet b : bullets)
            b.move();
        for (Explosion e : explosions)
            e.move();
        for (Meteor m : meteors) {
            m.move();
            if (m.isOutOfScreen())
                m.reset();
        }

        for (Bullet b : bullets) {
            for (Meteor m : meteors) {
                if (b.isAlive() && b.getBounds().intersects(m.getBounds())) {
                    b.setAlive(false);
                    ships.get(b.getOwnerId()).addScore(m.getPoints());
                    explosions.add(new Explosion(m.getX(), m.getY(), m.getWidth()));
                    m.reset();
                }
            }
        }

        for (Ship s : ships) {
            for (Meteor m : meteors) {
                if (s.isAlive() && s.getBounds().intersects(m.getBounds())) {
                    s.loseLife();
                    explosions.add(new Explosion(m.getX(), m.getY(), m.getWidth()));
                    m.reset();
                }
            }
        }

        for (Bullet b : bullets) {
            if (!b.isAlive())
                bullets.remove(b);
        }
        for (Explosion e : explosions) {
            if (!e.isAlive())
                explosions.remove(e);
        }

        timeLeft = GAME_TIME - (int) ((System.currentTimeMillis() - startTime) / 1000);
        if (timeLeft < 0)
            timeLeft = 0;

        boolean someoneAlive = false;
        for (Ship s : ships) {
            if (s.isAlive())
                someoneAlive = true;
        }
        if (timeLeft == 0 || !someoneAlive) {
            gameOver = true;
        }
    }

    private String buildState() {
        StringBuilder s = new StringBuilder(512);
        s.append("TIME ").append(timeLeft).append('\n');
        if (!started)
            s.append("WAIT\n");
        if (gameOver)
            s.append("OVER\n");
        for (Ship sh : ships) {
            s.append("SHIP ").append(sh.getId()).append(' ').append(sh.getX()).append(' ').append(sh.getY()).append(' ')
                    .append(sh.getScore()).append(' ').append(sh.getLives()).append('\n');
        }
        for (Meteor m : meteors) {
            s.append("METEOR ").append(m.getX()).append(' ').append(m.getY()).append(' ')
                    .append(m.getWidth()).append(' ').append(m.getPicture()).append('\n');
        }
        for (Bullet b : bullets) {
            s.append("BULLET ").append(b.getX()).append(' ').append(b.getY()).append(' ')
                    .append(b.getOwnerId()).append('\n');
        }
        for (Explosion e : explosions) {
            s.append("BOOM ").append(e.getX()).append(' ').append(e.getY()).append(' ')
                    .append(e.getWidth()).append('\n');
        }
        s.append("END\n");
        return s.toString();
    }

    public void setGameState(CopyOnWriteArrayList<Ship> ships, CopyOnWriteArrayList<Meteor> meteors,
            CopyOnWriteArrayList<Bullet> bullets, CopyOnWriteArrayList<Explosion> explosions,
            int timeLeft, boolean started, boolean gameOver) {
        this.ships = ships;
        this.meteors = meteors;
        this.bullets = bullets;
        this.explosions = explosions;
        this.timeLeft = timeLeft;
        this.started = started;
        this.gameOver = gameOver;
        repaint();
    }

    private boolean onlineKey(int action) {
        return keys[keyMap[0][action]] || keys[keyMap[1][action]];
    }

    private void sendKeysToHost() {
        String msg = "KEY";
        for (int i = 0; i < 4; i++) {
            msg += onlineKey(i) ? " 1" : " 0";
        }
        client.send(msg);
    }

    public synchronized int addShip() {
        int id = ships.size();
        ships.add(new Ship(id, 125 + id * 300, MainGame.GAME_HEIGHT - 80));
        return id;
    }

    public Ship getShip(int id) {
        return ships.get(id);
    }

    public int getPlayerCount() {
        return ships.size();
    }

    public boolean isStarted() {
        return started;
    }

    public void setServer(GameServer server) {
        this.server = server;
    }

    public void setClient(GameClient client) {
        this.client = client;
    }

    public void setMyId(int id) {
        myId = id;
    }

    public void showMessage(String text) {
        if (message.equals("")) {
            message = text;
        }
        repaint();
    }

    private void backToMenu() {
        running = false;
        if (server != null)
            server.close();
        if (client != null)
            client.close();
        game.showMenu();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (background.getWidth(null) > 0) {
            g.drawImage(background, 0, 0, MainGame.GAME_WIDTH, MainGame.GAME_HEIGHT, null);
        } else {
            g.setColor(Color.WHITE);
            for (int i = 0; i < starX.length; i++) {
                g.fillRect(starX[i], starY[i], 2, 2);
            }
        }

        for (Meteor m : meteors)
            m.draw(g);
        for (Bullet b : bullets)
            b.draw(g);
        for (Explosion e : explosions)
            e.draw(g);
        for (Ship s : ships)
            s.draw(g);

        g.setFont(new Font("Arial", Font.BOLD, 14));
        if (mode != OFFLINE) {
            for (Ship s : ships) {
                if (s.getId() == myId && s.isAlive()) {
                    g.setColor(Color.WHITE);
                    g.drawString("YOU", s.getX() + 10, s.getY() - 5);
                }
            }
        }

        g.setFont(new Font("Arial", Font.BOLD, 18));
        int row = 0;
        for (Ship s : ships) {
            g.setColor(s.getColor());
            String text = s.getName() + " : " + s.getScore() + "   Lives: " + s.getLives();
            if (!s.isAlive()) {
                text = s.getName() + " : " + s.getScore() + "   DEAD";
            }
            g.drawString(text, 15, 25 + row * 25);
            row++;
        }

        int min = timeLeft / 60;
        int sec = timeLeft % 60;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Time: " + min + ":" + (sec < 10 ? "0" : "") + sec, MainGame.GAME_WIDTH - 150, 30);

        g.setFont(new Font("Arial", Font.BOLD, 26));

        if (!started && message.equals("")) {
            g.setColor(Color.YELLOW);
            if (mode == HOST && server != null) {
                g.drawString("Your IP : " + myIP + "    Port : " + server.getPort(), 220, 250);
                g.drawString("Players in room : " + ships.size() + " / 3", 300, 300);
                g.drawString("Press START when everyone is ready", 220, 350);
            } else if (mode == CLIENT) {
                g.drawString("Connected!  Waiting for host to start...", 200, 300);
            }
        }

        if (!message.equals("")) {
            g.setColor(Color.RED);
            g.drawString(message, 200, 300);
            g.setColor(Color.WHITE);
            g.drawString("Press ESC to return to menu", 270, 350);
        }

        if (gameOver) {
            drawResult(g);
        }
    }

    private void drawResult(Graphics g) {
    Ship[] rank = ships.toArray(new Ship[0]);

    for (int i = 0; i < rank.length - 1; i++) {
        for (int j = 0; j < rank.length - 1 - i; j++) {
            if (rank[j].getScore() < rank[j + 1].getScore()) {
                Ship temp = rank[j];
                rank[j] = rank[j + 1];
                rank[j + 1] = temp;
            }
        }
    }

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON
    );

    g2.setColor(new Color(40, 40, 40, 220));
    g2.fillRect(80, 90, 740, 460);

    g2.setColor(Color.BLACK);
    g2.setStroke(new BasicStroke(5));
    g2.drawRect(80, 90, 740, 460);

    g2.setFont(new Font("Arial", Font.BOLD, 38));
    g2.setColor(Color.YELLOW);
    drawCenteredText(g2, "GAME OVER", 450, 155);

    int[] xPositions = { 375, 160, 590 };
    int[] topPositions = { 285, 355, 385 };
    int podiumWidth = 135;
    int podiumBottom = 510;

    for (int i = 0; i < rank.length && i < 3; i++) {
        int x = xPositions[i];
        int top = topPositions[i];
        int height = podiumBottom - top;
        boolean isWinner = (i == 0);

        g2.setColor(Color.WHITE);
        g2.fillRect(x, top, podiumWidth, height);

        g2.setColor(isWinner ? new Color(135, 60, 255) : Color.BLUE);
        g2.setStroke(new BasicStroke(isWinner ? 4 : 2));
        g2.drawRect(x, top, podiumWidth, height);

        g2.drawImage(rank[i].image, x + 43, top - 65, 50, 50, null);

        g2.setFont(new Font("Arial", Font.PLAIN, 17));
        g2.setColor(Color.BLACK);
        drawCenteredText(g2, rank[i].getName(), x + podiumWidth / 2, top + 32);

        g2.setFont(new Font("Arial", Font.PLAIN, 38));
        drawCenteredText(g2, (i + 1) + ".", x + podiumWidth / 2, top + 105);

        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        drawCenteredText(
            g2,
            "Score: " + rank[i].getScore(),
            x + podiumWidth / 2,
            podiumBottom - 12
        );
    }

    g2.setFont(new Font("Arial", Font.PLAIN, 18));
    g2.setColor(Color.YELLOW);
    drawCenteredText(g2, "Press ESC to return to menu", 450, 590);

    g2.dispose();
}

private void drawCenteredText(Graphics2D g, String text, int centerX, int y) {
    FontMetrics metrics = g.getFontMetrics();
    int x = centerX - metrics.stringWidth(text) / 2;
    g.drawString(text, x, y);
}

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            backToMenu();
            return;
        }
        if (e.getKeyCode() < keys.length) {
            keys[e.getKeyCode()] = true;
        }
        if (mode == CLIENT) {
            sendKeysToHost();
        }
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() < keys.length) {
            keys[e.getKeyCode()] = false;
        }
        if (mode == CLIENT) {
            sendKeysToHost();
        }
    }

    public void keyTyped(KeyEvent e) {
    }
}
