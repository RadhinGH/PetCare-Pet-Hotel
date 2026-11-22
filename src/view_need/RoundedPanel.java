package view_need;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/** Panel bersudut membulat, dipakai untuk kartu putih dan kotak-kotak kecil. */
public class RoundedPanel extends JPanel {

    private final int radius;
    private Color borderColor;

    public RoundedPanel(int radius) {
        this.radius = radius;
        setOpaque(false);
    }

    public void setBorderColor(Color warna) {
        this.borderColor = warna;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, w - 1, h - 1, radius, radius);
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);
        }
        g2.dispose();
    }
}
