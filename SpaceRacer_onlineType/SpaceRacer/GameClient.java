import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;

// Client (เครื่องที่กด Join) — แนวเดียวกับ ChatClient ใน ch08
// - ส่งปุ่มที่กดไปให้ Host
// - Thread นี้คอยรับสถานะเกมจาก Host แล้วส่งให้ GamePanel วาด
public class GameClient extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private GamePanel panel;

    public GameClient(String ip, int port) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(ip, port), 3000); // รอเชื่อมต่อไม่เกิน 3 วินาที
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void setPanel(GamePanel panel) {
        this.panel = panel;
    }

    public void send(String message) {
        out.println(message);
    }

    public void run() {
        // ของที่จะวาดใน 1 เฟรม
        CopyOnWriteArrayList<Ship> ships = new CopyOnWriteArrayList<Ship>();
        CopyOnWriteArrayList<Meteor> meteors = new CopyOnWriteArrayList<Meteor>();
        CopyOnWriteArrayList<Bullet> bullets = new CopyOnWriteArrayList<Bullet>();
        CopyOnWriteArrayList<Explosion> explosions = new CopyOnWriteArrayList<Explosion>();
        int time = 0;
        boolean started = true;
        boolean over = false;

        try {
            String line;
            while ((line = in.readLine()) != null) {
                String[] p = line.split(" ");
                if (p[0].equals("ID")) {
                    panel.setMyId(Integer.parseInt(p[1]));
                } else if (p[0].equals("FULL")) {
                    panel.showMessage("Room is full or game already started");
                } else if (p[0].equals("TIME")) {
                    time = Integer.parseInt(p[1]);
                } else if (p[0].equals("WAIT")) {
                    started = false;
                } else if (p[0].equals("OVER")) {
                    over = true;
                } else if (p[0].equals("SHIP")) {
                    Ship s = new Ship(Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
                    s.setScore(Integer.parseInt(p[4]));
                    s.setLives(Integer.parseInt(p[5]));
                    ships.add(s);
                } else if (p[0].equals("METEOR")) {
                    meteors.add(new Meteor(Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                            Integer.parseInt(p[3]), Integer.parseInt(p[4])));
                } else if (p[0].equals("BULLET")) {
                    bullets.add(new Bullet(Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3])));
                } else if (p[0].equals("BOOM")) {
                    explosions.add(new Explosion(Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3])));
                } else if (p[0].equals("END")) {
                    // ครบ 1 เฟรมแล้ว ส่งให้ GamePanel วาด แล้วเริ่มเก็บเฟรมใหม่
                    panel.setGameState(ships, meteors, bullets, explosions, time, started, over);
                    ships = new CopyOnWriteArrayList<Ship>();
                    meteors = new CopyOnWriteArrayList<Meteor>();
                    bullets = new CopyOnWriteArrayList<Bullet>();
                    explosions = new CopyOnWriteArrayList<Explosion>();
                    started = true;
                    over = false;
                }
            }
        } catch (IOException e) {
        }
        // readLine() ได้ null หรือ error = Host ปิดเกม/สายหลุด
        panel.showMessage("Disconnected from host");
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
        }
    }
}
