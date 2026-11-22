package view_need;

import javax.swing.*;
import java.awt.*;

/** Ikon telapak kecil untuk ditempel di JLabel / JButton (pengganti emoji). */
public class PawIcon implements Icon {

    private final int size;
    private final Color warna;

    public PawIcon(int size, Color warna) {
        this.size = size;
        this.warna = warna;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        PawPainter.gambar(g2, x, y, size, 0, warna);
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
