package util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import view.Theme;
import view_need.RoundedPanel;

/**
 * Notifikasi kecil (toast) yang naik dari bawah jendela, lalu hilang sendiri.
 * Pemakaian: ToasterPopOut.sukses(this, "Judul", "keterangan");
 * Parameter pertama boleh komponen apa saja di dalam jendela tujuan.
 */
public final class ToasterPopOut {

    private static final int LEBAR = 396;
    private static final int TINGGI = 84;
    private static final int DURASI_TAMPIL_MS = 3000;


    private static RoundedPanel toastAktif; // supaya toast lama tidak bertumpuk

    private ToasterPopOut() { }

    public static void error(Component asal, String judul, String sub) {
        tampilkan(asal, judul, sub, Theme.WARNING);
    }

    public static void sukses(Component asal, String judul, String sub) {
        tampilkan(asal, judul, sub, Theme.SUKSES);
    }

    private static void tampilkan(Component asal, String judul, String sub, Color warnaJudul) {
        Window jendela = SwingUtilities.getWindowAncestor(asal);
        if (jendela == null && asal instanceof Window) {
            jendela = (Window) asal;
        }
        if (!(jendela instanceof RootPaneContainer) || !jendela.isShowing()) {
            return;
        }

        JLayeredPane lapisan = ((RootPaneContainer) jendela).getLayeredPane();
        if (toastAktif != null && toastAktif.getParent() != null) {
            toastAktif.getParent().remove(toastAktif);
        }

        // ----- isi kartu toast -----
        RoundedPanel kartu = new RoundedPanel(20);
        kartu.setBackground(Color.WHITE);
        kartu.setBorderColor(Theme.BORDER_FIELD);
        kartu.setLayout(new BorderLayout(12, 0));
        kartu.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 18));
        kartu.add(new AvaterLabel(ImageLoader.kucing(), 46), BorderLayout.WEST);

        JLabel lJudul = new JLabel(judul);
        lJudul.setFont(Theme.font(Font.BOLD, 13));
        lJudul.setForeground(warnaJudul);

        JLabel lSub = new JLabel(sub);
        lSub.setFont(Theme.font(Font.PLAIN, 11));
        lSub.setForeground(Theme.TEKS_MUDA);

        JPanel teks = new JPanel(new GridLayout(2, 1, 0, 2));
        teks.setOpaque(false);
        teks.add(lJudul);
        teks.add(lSub);
        kartu.add(teks, BorderLayout.CENTER);

        // ----- posisi: kanan-bawah, naik sedikit -----
        final int x = lapisan.getWidth() - LEBAR - 24;           // pojok kanan-bawah
        final int yAkhir = lapisan.getHeight() - TINGGI - 56;   // di atas status bar
        kartu.setSize(LEBAR, TINGGI);
        kartu.setLocation(x, yAkhir + 30);
        lapisan.add(kartu, JLayeredPane.POPUP_LAYER);
        toastAktif = kartu;

        // animasi naik
        Timer naik = new Timer(16, e -> {
            kartu.setLocation(x, Math.max(yAkhir, kartu.getY() - 3));
            if (kartu.getY() <= yAkhir) {
                ((Timer) e.getSource()).stop();
            }
        });
        naik.start();

        // hilang otomatis
        Timer tutup = new Timer(DURASI_TAMPIL_MS, e -> {
            if (kartu.getParent() != null) {
                kartu.getParent().remove(kartu);
                lapisan.repaint();
            }
        });
        tutup.setRepeats(false);
        tutup.start();
    }
}
