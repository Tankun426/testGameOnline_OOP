
import java.io.IOException;
import javax.swing.*;

public class MainGame extends JFrame {

    public static final int GAME_WIDTH = 900;
    public static final int GAME_HEIGHT = 650;

    public MainGame() {
        setTitle("Space Racer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        showTitle();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // เอาหน้าเก่าออก ใส่หน้าใหม่เข้าไป
    public void changePanel(JPanel panel) {
        getContentPane().removeAll();
        add(panel);
        pack();
        revalidate();
        repaint();
        panel.requestFocusInWindow();
    }

    public void showTitle() {
        changePanel(new TitlePanel(this));
    }

    public void showMenu() {
        changePanel(new MenuPanel(this));
    }

    public void startOffline(int numberOfPlayers) {
        changePanel(new GamePanel(this, GamePanel.OFFLINE, numberOfPlayers));
    }

    public void startHost(int port) {
        try {
            GamePanel panel = new GamePanel(this, GamePanel.HOST, 1);
            GameServer server = new GameServer(port, panel);
            panel.setServer(server);
            server.start();
            changePanel(panel);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Cannot open port " + port + "\n(port may be in use)");
        }
    }

    public void joinGame(String ip, int port) {
        try {
            GameClient client = new GameClient(ip, port);
            GamePanel panel = new GamePanel(this, GamePanel.CLIENT, 0);
            client.setPanel(panel);
            panel.setClient(client);
            client.start();
            changePanel(panel);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Cannot connect to " + ip + " : " + port);
        }
    }

    public static void main(String[] args) {
        new MainGame();
    }
}
