package view_need;

import view.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Latar pink gradasi + pola telapak kaki kucing.
 * Pola digambar manual lewat override paintComponent() (sesuai spesifikasi
 * "paintComponent() custom"), posisi telapak dibuat deterministik dengan seed
 * supaya tidak berubah-ubah setiap repaint.
 */
public class PawBackgroundPanel extends JPanel {

    /** Data satu telapak: posisi fraksional, ukuran, rotasi, warna. */
    private static final class Telapak {
        final double fx;
        final double fy;
        final int size;
        final double sudut;
        final Color warna;

        Telapak(double fx, double fy, int size, double sudut, Color warna) {
            this.fx = fx;
            this.fy = fy;
            this.size = size;
            this.sudut = sudut;
            this.warna = warna;
        }
    }

    private final List<Telapak> daftarTelapak = new ArrayList<>();
    private final Color warnaAtas;
    private final Color warnaBawah;

    public PawBackgroundPanel() {
        this(34, 12345L);
    }

    public PawBackgroundPanel(int jumlah, long seed) {
        this(jumlah, seed, Theme.BG_ATAS, Theme.BG_BAWAH);
    }

    public PawBackgroundPanel(int jumlah, long seed, Color warnaAtas, Color warnaBawah) {
        this.warnaAtas = warnaAtas;
        this.warnaBawah = warnaBawah;
        setOpaque(false);
        Random acak = new Random(seed);
        for (int i = 0; i < jumlah; i++) {
            double fx = 0.04 + acak.nextDouble() * 0.92;
            double fy = 0.03 + acak.nextDouble() * 0.94;
            int size = 24 + acak.nextInt(38);
            double sudut = acak.nextDouble() * 360;

            Color warna;
            int pilih = acak.nextInt(10);
            if (pilih < 6) {
                warna = Theme.PAW_PUTIH;      // telapak putih transparan
            } else if (pilih < 8) {
                warna = Theme.PAW_PINK;       // telapak pink lebih gelap
            } else {
                warna = Theme.PAW_UNGU;       // sesekali telapak ungu
            }
            daftarTelapak.add(new Telapak(fx, fy, size, sudut, warna));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // gradasi warna dari atas ke bawah
        g2.setPaint(new GradientPaint(0, 0, warnaAtas, 0, h, warnaBawah));
        g2.fillRect(0, 0, w, h);

        // pola telapak
        for (Telapak t : daftarTelapak) {
            int x = (int) (t.fx * w) - t.size / 2;
            int y = (int) (t.fy * h) - t.size / 2;
            PawPainter.gambar(g2, x, y, t.size, t.sudut, t.warna);
        }

        g2.dispose();
    }
}
