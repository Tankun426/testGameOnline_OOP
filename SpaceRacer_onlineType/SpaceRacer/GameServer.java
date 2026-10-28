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
        serverSocket = new ServerSocket(port);
    }

    public void run() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                if (panel.isStarted() || panel.getPlayerCount() >= 3) {
                    out.println("FULL");
                    out.flush();
                    socket.close();
                    continue;
                }

                int id = panel.addShip(); 
                out.println("ID " + id);
                out.flush();
                sockets.add(socket);
                outs.add(out);
                listenToPlayer(socket, id);
            } catch (IOException e) {
                running = false; // serverSocket ถูกปิด
            }
        }
    }

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
                panel.getShip(id).setLives(0);
            }
        });
        receiver.start();
    }

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
