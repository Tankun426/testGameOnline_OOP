import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

// หน้าเริ่มเกม: แสดงภาพ title และกดปุ่ม START เพื่อไปหน้าเมนู
public class TitlePanel extends JPanel {
    private final MainGame game;
    private final Image titleImage;

    public TitlePanel(MainGame game) {
        this.game = game;
        URL imageUrl = TitlePanel.class.getResource("/img/image.png");
        if (imageUrl == null) {
            throw new IllegalStateException("หาไฟล์ img/image.png ไม่เจอ");
        }
        titleImage = new ImageIcon(imageUrl).getImage();

        setPreferredSize(new Dimension(MainGame.GAME_WIDTH, MainGame.GAME_HEIGHT));
        setLayout(null);

        // ปุ่มใสทับบริเวณ START ที่อยู่ในภาพ
        JButton startButton = new JButton();
        startButton.setBounds(335, 515, 230, 95);
        startButton.setOpaque(false);
        startButton.setContentAreaFilled(false);
        startButton.setBorderPainted(false);
        startButton.setFocusPainted(false);
        startButton.setToolTipText("START");
        startButton.addActionListener(e -> this.game.showMenu());
        add(startButton);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(titleImage, 0, 0, getWidth(), getHeight(), this);
    }
}
