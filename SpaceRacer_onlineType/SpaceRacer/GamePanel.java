import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.*;

// หน้าเล่นเกม ใช้ได้ 3 แบบ
//   OFFLINE : เล่นเครื่องเดียว 1-3 คน
//   HOST    : เครื่องนี้คำนวณเกมเอง แล้วส่งสถานะให้เพื่อน
//   CLIENT  : เครื่องนี้แค่ส่งปุ่มไปให้ Host และวาดตามที่ Host ส่งมา
public class GamePanel extends JPanel implements KeyListener, Runnable {
    public static final int OFFLINE = 0, HOST = 1, CLIENT = 2;
    public static final int GAME_TIME = 180; // 3 นาที

    private MainGame game;
    private int mode;
    private GameServer server;
    private GameClient client;
    private int myId = 0; // ยานของเครื่องนี้คือคนที่เท่าไหร่

    // ---------- ของในเกม ----------
    // CopyOnWriteArrayList = ArrayList ที่หลาย Thread ใช้พร้อมกันได้ไม่ error
    private CopyOnWriteArrayList<Ship> ships = new CopyOnWriteArrayList<Ship>();
    private CopyOnWriteArrayList<Meteor> meteors = new CopyOnWriteArrayList<Meteor>();
    private CopyOnWriteArrayList<Bullet> bullets = new CopyOnWriteArrayList<Bullet>();
    private CopyOnWriteArrayList<Explosion> explosions = new CopyOnWriteArrayList<Explosion>();

    // ---------- สถานะเกม (volatile = ทุก Thread เห็นค่าล่าสุด) ----------
    private volatile int timeLeft = GAME_TIME;
    private volatile boolean started = false;
    private volatile boolean gameOver = false;
    private volatile boolean running = true;
    private volatile long startTime;
    private volatile String message = "";

    // ---------- คีย์บอร์ด ----------
    private boolean[] keys = new boolean[256];
    // ชุดปุ่ม: { ขึ้น, ลง, ซ้าย, ขวา, ยิง }
    private int[][] keyMap = {
        { KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE },
        { KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER },
        { KeyEvent.VK_I, KeyEvent.VK_K, KeyEvent.VK_J, KeyEvent.VK_L, KeyEvent.VK_P }
    };

    private Image background = new ImageIcon("img/background.jpg").getImage();
    private int[] starX = new int[100];
    private int[] starY = new int[100];
    private JButton startButton = new JButton("START GAME");
    private String myIP = GameServer.getMyIP(); // IP ของเครื่องนี้ ไว้บอกเพื่อน

    public GamePanel(MainGame game, int mode, int numberOfPlayers) {
        this.game = game;
        this.mode = mode;
        setPreferredSize(new Dimension(MainGame.GAME_WIDTH, MainGame.GAME_HEIGHT));
        setBackground(Color.BLACK);
        setLayout(null);
        setFocusable(true);
        addKeyListener(this);

        // สุ่มตำแหน่งดาวพื้นหลัง
        Random random = new Random();
        for (int i = 0; i < starX.length; i++) {
            starX[i] = random.nextInt(MainGame.GAME_WIDTH);
            starY[i] = random.nextInt(MainGame.GAME_HEIGHT);
        }

        if (mode == CLIENT) {
            return; // Client ไม่ต้องสร้างอะไร รอข้อมูลจาก Host อย่างเดียว
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
            // Host: มีปุ่ม START ให้กดเมื่อเพื่อนเข้าครบ
            startButton.setBounds(350, 420, 200, 50);
            startButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    startGame();
                }
            });
            add(startButton);
        }

        new Thread(this).start(); // เริ่ม game loop
    }

    private void startGame() {
        startTime = System.currentTimeMillis();
        started = true;
        startButton.setVisible(false);
        requestFocusInWindow();
    }

    // ================= GAME LOOP (Thread) — ทำงานบน OFFLINE และ HOST =================
    public void run() {
        while (running) {
            if (started) {
                update();
            }
            if (mode == HOST && server != null) {
                server.broadcast(buildState());
            }
            if (gameOver) {
                running = false; // ส่งสถานะจบเกมไปแล้ว หยุด loop
            }
            repaint();
            try {
                Thread.sleep(20); // ประมาณ 50 ครั้ง/วินาที
            } catch (InterruptedException e) {
            }
        }
    }

    private void update() {
        // 1) เอาปุ่มที่กดไปใส่ให้ยาน
        if (mode == OFFLINE) {
            for (int i = 0; i < ships.size(); i++) {
                int[] k = keyMap[i];
                ships.get(i).setInput(keys[k[0]], keys[k[1]], keys[k[2]], keys[k[3]], keys[k[4]]);
            }
        } else {
            // Host = ยานคนที่ 0 (ยานคนอื่นได้ปุ่มมาจาก GameServer)
            ships.get(0).setInput(onlineKey(0), onlineKey(1), onlineKey(2), onlineKey(3), onlineKey(4));
        }

        // 2) ขยับยาน + ยิง
        for (Ship s : ships) {
            if (!s.isAlive()) continue;
            s.move();
            if (s.wantToShoot()) {
                bullets.add(new Bullet(s.getX() + s.getWidth() / 2 - 3, s.getY(), s.getId()));
                s.resetShootDelay();
            }
        }

        // 3) ขยับของอื่น ๆ
        for (Bullet b : bullets) b.move();
        for (Explosion e : explosions) e.move();
        for (Meteor m : meteors) {
            m.move();
            if (m.isOutOfScreen()) m.reset();
        }

        // 4) กระสุนชนอุกกาบาต -> ได้คะแนน
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

        // 5) อุกกาบาตชนยาน -> เสีย 1 ชีวิต
        for (Ship s : ships) {
            for (Meteor m : meteors) {
                if (s.isAlive() && s.getBounds().intersects(m.getBounds())) {
                    s.loseLife();
                    explosions.add(new Explosion(m.getX(), m.getY(), m.getWidth()));
                    m.reset();
                }
            }
        }

        // 6) ลบของที่ใช้แล้วออก
        for (Bullet b : bullets) {
            if (!b.isAlive()) bullets.remove(b);
        }
        for (Explosion e : explosions) {
            if (!e.isAlive()) explosions.remove(e);
        }

        // 7) นับเวลา + เช็คจบเกม (หมดเวลา หรือ ตายหมดทุกคน)
        timeLeft = GAME_TIME - (int) ((System.currentTimeMillis() - startTime) / 1000);
        if (timeLeft < 0) timeLeft = 0;

        boolean someoneAlive = false;
        for (Ship s : ships) {
            if (s.isAlive()) someoneAlive = true;
        }
        if (timeLeft == 0 || !someoneAlive) {
            gameOver = true;
        }
    }

    // ================= ส่วนของ Online =================

    // แปลงสถานะเกมเป็นข้อความ ส่งไปให้ Client (1 บรรทัด = ของ 1 ชิ้น)
    private String buildState() {
        String s = "TIME " + timeLeft + "\n";
        if (!started) s += "WAIT\n";
        if (gameOver) s += "OVER\n";
        for (Ship sh : ships) {
            s += "SHIP " + sh.getId() + " " + sh.getX() + " " + sh.getY() + " "
                    + sh.getScore() + " " + sh.getLives() + "\n";
        }
        for (Meteor m : meteors) {
            s += "METEOR " + m.getX() + " " + m.getY() + " " + m.getWidth() + " " + m.getPicture() + "\n";
        }
        for (Bullet b : bullets) {
            s += "BULLET " + b.getX() + " " + b.getY() + " " + b.getOwnerId() + "\n";
        }
        for (Explosion e : explosions) {
            s += "BOOM " + e.getX() + " " + e.getY() + " " + e.getWidth() + "\n";
        }
        s += "END\n";
        return s;
    }

    // Client: รับสถานะเกมจาก GameClient มาเก็บไว้วาด
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

    // เล่น Online ใช้ได้ทั้ง WASD+SPACE และ ลูกศร+ENTER
    private boolean onlineKey(int action) {
        return keys[keyMap[0][action]] || keys[keyMap[1][action]];
    }

    // Client: ส่งปุ่มที่กดอยู่ไปให้ Host เช่น "KEY 0 1 0 0 1"
    private void sendKeysToHost() {
        String msg = "KEY";
        for (int i = 0; i < 5; i++) {
            msg += onlineKey(i) ? " 1" : " 0";
        }
        client.send(msg);
    }

    // Server เรียกตอนมีเพื่อนเข้าห้อง: สร้างยานใหม่ คืนค่าเลขผู้เล่น
    public synchronized int addShip() {
        int id = ships.size();
        ships.add(new Ship(id, 125 + id * 300, MainGame.GAME_HEIGHT - 80));
        return id;
    }

    public Ship getShip(int id) { return ships.get(id); }
    public int getPlayerCount() { return ships.size(); }
    public boolean isStarted() { return started; }
    public void setServer(GameServer server) { this.server = server; }
    public void setClient(GameClient client) { this.client = client; }
    public void setMyId(int id) { myId = id; }

    public void showMessage(String text) {
        if (message.equals("")) { // เก็บข้อความแรกไว้ ไม่ให้ข้อความหลังทับ
            message = text;
        }
        repaint();
    }

    // กลับเมนู: ปิด loop และปิดการเชื่อมต่อ
    private void backToMenu() {
        running = false;
        if (server != null) server.close();
        if (client != null) client.close();
        game.showMenu();
    }

    // ================= วาด =================
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // พื้นหลัง
        if (background.getWidth(null) > 0) {
            g.drawImage(background, 0, 0, MainGame.GAME_WIDTH, MainGame.GAME_HEIGHT, null);
        } else {
            g.setColor(Color.WHITE);
            for (int i = 0; i < starX.length; i++) {
                g.fillRect(starX[i], starY[i], 2, 2);
            }
        }

        // ของในเกม (ทุกตัวเรียก draw() เหมือนกัน = Polymorphism)
        for (Meteor m : meteors) m.draw(g);
        for (Bullet b : bullets) b.draw(g);
        for (Explosion e : explosions) e.draw(g);
        for (Ship s : ships) s.draw(g);

        // ป้าย YOU บนยานตัวเอง (เฉพาะ Online)
        g.setFont(new Font("Arial", Font.BOLD, 14));
        if (mode != OFFLINE) {
            for (Ship s : ships) {
                if (s.getId() == myId && s.isAlive()) {
                    g.setColor(Color.WHITE);
                    g.drawString("YOU", s.getX() + 10, s.getY() - 5);
                }
            }
        }

        // คะแนน + ชีวิต
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

        // เวลา
        int min = timeLeft / 60;
        int sec = timeLeft % 60;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Time: " + min + ":" + (sec < 10 ? "0" : "") + sec, MainGame.GAME_WIDTH - 150, 30);

        g.setFont(new Font("Arial", Font.BOLD, 26));

        // ห้องรอ (ยังไม่เริ่มเกม)
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

        // ข้อความ error เช่น หลุดการเชื่อมต่อ
        if (!message.equals("")) {
            g.setColor(Color.RED);
            g.drawString(message, 200, 300);
            g.setColor(Color.WHITE);
            g.drawString("Press ESC to return to menu", 270, 350);
        }

        // จบเกม: แสดงอันดับ
        if (gameOver) {
            drawResult(g);
        }
    }

    // เรียงคะแนนมาก -> น้อย ด้วย bubble sort แล้วแสดงอันดับ
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

        g.setColor(new Color(0, 0, 0, 200)); // พื้นดำโปร่งแสง
        g.fillRect(150, 120, 600, 400);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString("GAME OVER", 300, 190);

        g.setFont(new Font("Arial", Font.BOLD, 30));
        for (int i = 0; i < rank.length; i++) {
            g.setColor(rank[i].getColor());
            g.drawString((i + 1) + ".  " + rank[i].getName() + "   :   " + rank[i].getScore(), 280, 270 + i * 60);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Press ESC to return to menu", 320, 490);
    }

    // ================= คีย์บอร์ด =================
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
