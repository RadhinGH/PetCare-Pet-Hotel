package view_need;

import java.awt.*;

/**
 * Menggambar satu telapak kaki kucing dengan Graphics2D.
 * Dipakai ulang oleh pola latar (PawBackgroundPanel) dan ikon kecil (PawIcon).
 */
public final class PawPainter {

    private PawPainter() { }

    /**
     * @param g2      graphics tujuan
     * @param x, y    posisi kiri-atas telapak
     * @param size    lebar telapak dalam piksel
     * @param derajat rotasi telapak (0-360)
     * @param warna   warna telapak (boleh mengandung alpha)
     */
    public static void gambar(Graphics2D g2, int x, int y, int size,
                              double derajat, Color warna) {
        Graphics2D g = (Graphics2D) g2.create();
        g.setColor(warna);
        g.rotate(Math.toRadians(derajat), x + size / 2.0, y + size / 2.0);

        int jari = Math.max(2, (int) (size * 0.42));

        // telapak utama (bentuk oval lebar)
        g.fillOval(x, y + (int) (size * 0.42), size, (int) (size * 0.66));
        // tiga jari kaki di bagian atas
        g.fillOval(x, y, jari, jari);
        g.fillOval(x + size - jari, y, jari, jari);
        g.fillOval(x + size / 2 - jari / 2, y - (int) (size * 0.12), jari, jari);

        g.dispose();
    }
}
