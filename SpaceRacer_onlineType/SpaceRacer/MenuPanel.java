import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

// หน้าเมนู: เลือกเล่น Offline (Solo/Duo/Trio) หรือ Online ผ่าน LAN (Host/Join)
public class MenuPanel extends JPanel {
    private Image logo = new ImageIcon(getClass().getResource("img/logo3.png")).getImage();
    private JTextField portField = new JTextField("5000");
    private JTextField ipField = new JTextField("192.168.1.");
    private Image background = new ImageIcon(getClass().getResource("/img/bg2.jpg")).getImage();
    private Image btsolo = new ImageIcon(getClass().getResource("img/solo.png")).getImage();
    Image S1 = btsolo.getScaledInstance(140, 70, Image.SCALE_SMOOTH);
   
    private Image btduo = new ImageIcon(getClass().getResource("img/duo.png")).getImage();

    private Image bttrio = new ImageIcon(getClass().getResource("img/trio.png")).getImage();
    Image S2 = btduo.getScaledInstance(140, 70, Image.SCALE_SMOOTH);
    Image S3 = bttrio.getScaledInstance(140, 70, Image.SCALE_SMOOTH);
    public MenuPanel(final MainGame game) {
        setPreferredSize(new Dimension(MainGame.GAME_WIDTH, MainGame.GAME_HEIGHT));
        setBackground(Color.BLACK);
        setLayout(null);

        // ---------- Offline ----------
        JButton soloButton = new JButton(new ImageIcon(S1));
        JButton duoButton = new JButton(new ImageIcon(S2));
        JButton trioButton = new JButton(new ImageIcon(S3));
        soloButton.setBounds(220, 230, 140, 70);
        soloButton.setContentAreaFilled(false);
        soloButton.setBorderPainted(false);
        duoButton.setBounds(380, 230, 140, 70);
        duoButton.setContentAreaFilled(false);
        duoButton.setBorderPainted(false);
        trioButton.setBounds(540, 230, 140, 70);
        trioButton.setContentAreaFilled(false);
        trioButton.setBorderPainted(false);

        soloButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                game.startOffline(1);
            }
        });
        duoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                game.startOffline(2);
            }
        });
        trioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                game.startOffline(3);
            }
        });
        add(soloButton);
        add(duoButton);
        add(trioButton);

        // ---------- Online (LAN) ----------
        portField.setBounds(380, 360, 140, 35);
        ipField.setBounds(380, 410, 140, 35);
        add(portField);
        add(ipField);

        JButton hostButton = new JButton("Host");
        JButton joinButton = new JButton("Join");
        hostButton.setBounds(540, 360, 140, 35);
        joinButton.setBounds(540, 410, 140, 35);

        hostButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int port = readPort();
                if (port > 0) {
                    game.startHost(port);
                }
            }
        });
        joinButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int port = readPort();
                if (port > 0) {
                    game.joinGame(ipField.getText().trim(), port);
                }
            }
        });
        add(hostButton);
        add(joinButton);
    }

    // อ่านเลข port จากช่องกรอก ถ้าไม่ใช่ตัวเลขให้เตือน
    private int readPort() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            if (port < 1024 || port > 65535) {
                throw new NumberFormatException();
            }
            return port;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Port must be a number 1024 - 65535");
            return -1;
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
  if (background.getWidth(null) > 0) {
            g.drawImage(background, 0, 0, MainGame.GAME_WIDTH, MainGame.GAME_HEIGHT, null);
        } else {
            
        }
        // ชื่อเกม
        if (logo.getWidth(null) > 0) {
            g.drawImage(logo, 250, 10, 400, 200, null);
        } else {
            g.setColor(Color.ORANGE);
            g.setFont(new Font("Arial", Font.BOLD, 70));
            g.drawString("SPACE RACER", 200, 130);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("OFFLINE  (one keyboard)", 220, 215);
        g.drawString("ONLINE  (same LAN)", 220, 345);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("Port :", 220, 384);
        g.drawString("Host IP :", 220, 434);

        // บอกปุ่ม
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(Color.CYAN);
        g.drawString("Player 1 : W A S D   Shoot = SPACE", 220, 510);
        g.setColor(Color.PINK);
        g.drawString("Player 2 : Arrow keys   Shoot = ENTER", 220, 535);
        g.setColor(Color.GREEN);
        g.drawString("Player 3 : I J K L   Shoot = P", 220, 560);
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("Online : use W A S D or Arrow keys,  Shoot = SPACE or ENTER", 220, 600);
    }
}
