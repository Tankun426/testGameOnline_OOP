import java.awt.*;
import javax.swing.*;

// หน้าเมนู: เลือกเล่น Offline (Solo/Duo/Trio) หรือ Online ผ่าน LAN (Host/Join)
public class MenuPanel extends JPanel {
    private Image logo = new ImageIcon(getClass().getResource("img/logo3.png")).getImage();
    private JTextField portField = new JTextField("5000");
    private JTextField ipField = new JTextField("192.168.1.");
    private Image background = new ImageIcon(getClass().getResource("img/bg1.jpg")).getImage();
    private JButton modeSelector;
    private String selectedMode = "Offline";
    private JPanel offlinePanel;
    private JPanel onlinePanel;

    public MenuPanel(final MainGame game) {
        setPreferredSize(new Dimension(MainGame.GAME_WIDTH, MainGame.GAME_HEIGHT));
        setBackground(Color.BLACK);
        setLayout(null);

        modeSelector = new JButton("Select Mode");
        modeSelector.setBounds(370, 460, 160, 45);
        add(modeSelector);

        JPopupMenu modeMenu = new JPopupMenu();
        modeMenu.setBorder(BorderFactory.createLineBorder(new Color(40, 150, 220), 2));
        JPanel modeOptions = new JPanel(new GridLayout(2, 1, 0, 4));
        modeOptions.setBackground(new Color(12, 28, 55));
        modeOptions.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JButton offlineItem = modeOptionButton("✓  Offline");
        JButton onlineItem = modeOptionButton("Online");
        modeOptions.add(offlineItem);
        modeOptions.add(onlineItem);
        modeMenu.add(modeOptions);
        offlineItem.addActionListener(e -> {
            selectedMode = "Offline";
            offlineItem.setText("✓  Offline");
            onlineItem.setText("Online");
            modeMenu.setVisible(false);
            updateModePanel();
        });
        onlineItem.addActionListener(e -> {
            selectedMode = "Online";
            offlineItem.setText("Offline");
            onlineItem.setText("✓  Online");
            modeMenu.setVisible(false);
            updateModePanel();
        });
        modeSelector.addActionListener(e -> modeMenu.show(modeSelector, 0, modeSelector.getHeight()));

        offlinePanel = new JPanel(null);
        offlinePanel.setOpaque(false);
        offlinePanel.setBounds(170, 285, 560, 125);
        JButton soloButton = imageButton("solo.png", "Solo");
        JButton duoButton = imageButton("duo.png", "Duo");
        JButton trioButton = imageButton("trio.png", "Trio");
        soloButton.setBounds(0, 20, 140, 70);
        duoButton.setBounds(210, 20, 140, 70);
        trioButton.setBounds(420, 20, 140, 70);
        soloButton.addActionListener(e -> game.startOffline(1));
        duoButton.addActionListener(e -> game.startOffline(2));
        trioButton.addActionListener(e -> game.startOffline(3));
        offlinePanel.add(soloButton);
        offlinePanel.add(duoButton);
        offlinePanel.add(trioButton);
        add(offlinePanel);

        onlinePanel = new JPanel(null);
        onlinePanel.setOpaque(false);
        onlinePanel.setBounds(200, 285, 500, 170);
        JLabel portLabel = new JLabel("Port:");
        JLabel ipLabel = new JLabel("Host IP:");
        portLabel.setForeground(Color.WHITE);
        ipLabel.setForeground(Color.WHITE);
        portLabel.setBounds(0, 20, 90, 32);
        ipLabel.setBounds(0, 70, 90, 32);
        portField.setBounds(95, 20, 170, 32);
        ipField.setBounds(95, 70, 170, 32);

        JButton hostButton = new JButton("Host");
        JButton joinButton = new JButton("Join");
        hostButton.setBounds(300, 20, 150, 32);
        joinButton.setBounds(300, 70, 150, 32);
        hostButton.addActionListener(e -> {
            int port = readPort();
            if (port > 0) game.startHost(port);
        });
        joinButton.addActionListener(e -> {
            int port = readPort();
            if (port > 0) game.joinGame(ipField.getText().trim(), port);
        });
        onlinePanel.add(portLabel);
        onlinePanel.add(ipLabel);
        onlinePanel.add(portField);
        onlinePanel.add(ipField);
        onlinePanel.add(hostButton);
        onlinePanel.add(joinButton);
        add(onlinePanel);

        updateModePanel();
    }

    private JButton imageButton(String fileName, String altText) {
        java.net.URL imageUrl = getClass().getResource("img/" + fileName);
        JButton button = imageUrl == null ? new JButton(altText)
                : new JButton(new ImageIcon(new ImageIcon(imageUrl).getImage()
                        .getScaledInstance(140, 70, Image.SCALE_SMOOTH)));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        return button;
    }

    private JButton modeOptionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(20, 65, 105));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(145, 34));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(35, 115, 170));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(20, 65, 105));
            }
        });
        return button;
    }

    private void updateModePanel() {
        boolean offline = "Offline".equals(selectedMode);
        offlinePanel.setVisible(offline);
        onlinePanel.setVisible(!offline);
        repaint();
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

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null && background.getWidth(null) > 0) {
            g.drawImage(background, 0, 0, MainGame.GAME_WIDTH, MainGame.GAME_HEIGHT, null);
        }
        // ชื่อเกม
        if (logo.getWidth(null) > 0) {
            g.drawImage(logo, 250, 10, 400, 200, null);
        } else {
            g.setColor(Color.ORANGE);
            g.setFont(new Font("Arial", Font.BOLD, 70));
            g.drawString("SPACE RACER", 200, 130);
        }

        // แสดงคำแนะนำเฉพาะโหมดที่เลือก และเว้นระยะใต้ปุ่ม Select Mode
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        if ("Offline".equals(selectedMode)) {
            g.setColor(Color.CYAN);
            g.drawString("Player 1 : W A S D", 220, 550);
            g.setColor(Color.PINK);
            g.drawString("Player 2 : Arrow keys", 220, 575);
            g.setColor(Color.GREEN);
            g.drawString("Player 3 : I J K L", 220, 600);
        } else {
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("Online controls: W A S D or Arrow keys", 220, 575);
        }
    }
}
