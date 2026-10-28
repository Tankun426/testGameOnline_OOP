import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer extends Thread {
    private ServerSocket serverSocket;
    private GamePanel panel;
    private int port;
    private boolean running = true;

    private CopyOnWriteArrayList<Socket> sockets = new CopyOnWriteArrayList<Socket>();
    private CopyOnWriteArrayList<PrintWriter> outs = new CopyOnWriteArrayList<PrintWriter>();

    public GameServer(int port, GamePanel panel) throws IOException {
        this.port = port;
        this.panel = panel;
        serverSocket = new ServerSocket(port); // ถ้า port ถูกใช้อยู่ จะ throw IOException
    }

    public void run() {
        while (running) {
            try {
                Socket socket = serverSocket.accept(); // รอจนมีคนต่อเข้ามา
                // ปิด Nagle เพื่อส่งปุ่ม/สถานะเกมขนาดเล็กได้ทันที ไม่รอรวมแพ็กเก็ต
                socket.setTcpNoDelay(true);
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                // ห้องเต็ม หรือเกมเริ่มไปแล้ว -> ไม่รับ
                if (panel.isStarted() || panel.getPlayerCount() >= 3) {
                    out.println("FULL");
                    out.flush();
                    socket.close();
                    continue;
                }

                int id = panel.addShip();     // สร้างยานให้คนใหม่
                out.println("ID " + id);      // บอกเขาว่าเป็นผู้เล่นคนที่เท่าไหร่
                out.flush();
                sockets.add(socket);
                outs.add(out);
                listenToPlayer(socket, id);
            } catch (IOException e) {
                running = false; // serverSocket ถูกปิด
            }
        }
    }

    // Thread รับปุ่มเคลื่อนที่จากเพื่อน 1 คน ข้อความหน้าตาแบบ "KEY 0 1 0 0"
    private void listenToPlayer(final Socket socket, final int id) {
        Thread receiver = new Thread(new Runnable() {
            public void run() {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        String[] p = line.split(" ");
                        if (p[0].equals("KEY") && p.length == 5) {
                            panel.getShip(id).setInput(p[1].equals("1"), p[2].equals("1"),
                                    p[3].equals("1"), p[4].equals("1"));
                        }
                    }
                } catch (IOException e) {
                }
                // หลุดจากห้อง = ถือว่าตาย
                panel.getShip(id).setLives(0);
            }
        });
        receiver.start();
    }

    // ส่งสถานะเกมให้ทุกเครื่อง
    public void broadcast(String state) {
        for (PrintWriter out : outs) {
            out.print(state);
            out.flush();
        }
    }

    public void close() {
        running = false;
        try {
            serverSocket.close();
            for (Socket s : sockets) {
                s.close();
            }
        } catch (IOException e) {
        }
    }

    public int getPort() {
        return port;
    }

    // หา IP ของเครื่องนี้ในวง LAN (เช่น 192.168.1.5) เอาไว้บอกเพื่อน
    public static String getMyIP() {
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()) {
                Enumeration<InetAddress> addrs = nets.nextElement().getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress a = addrs.nextElement();
                    if (a instanceof Inet4Address && !a.isLoopbackAddress() && a.isSiteLocalAddress()) {
                        return a.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
        }
        return "127.0.0.1";
    }
}
