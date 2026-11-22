package util;

import view.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class AvaterLabel extends JComponent {

    private final Image gambar;
    private final int diameter;

    public AvaterLabel(Image gambar, int diameter) {
        this.gambar = gambar;
        this.diameter = diameter;
        setPreferredSize(new Dimension(diameter, diameter));
        setOpaque(false);

    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        //Lingkaran putih sebagai dasar
        g2.setColor(Color.WHITE);
        g2.fillOval(0,0,diameter-1,diameter-1);

        // gambar kucing. di-crop-in membulat (ini efek cover)
        if (gambar != null) {
            int iw = gambar.getWidth(this);
            int ih = gambar.getHeight(this);
            if (iw > 0 && ih > 0) {
                double skala = Math.max(diameter/(double) iw, diameter/(double) ih);
                int dw = (int) (iw * skala);
                int dh = (int) (ih * skala);
                g2.setClip(new Ellipse2D.Float(3,3, diameter-7, diameter-7));
                g2.drawImage(gambar,(diameter-dw) / 2,(diameter-dh) / 2, dw, dh, null);
                g2.setClip(null);
            }
        }

        g2.setColor(Theme.PRIMARY_MUDA);
        g2.setStroke(new BasicStroke(3f));
        g2.drawOval(1,1,diameter-3,diameter-3);

        g2.dispose();
    }
}
