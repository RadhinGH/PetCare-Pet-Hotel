package view_need;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import util.AvaterLabel;
import util.ImageLoader;
import view.Theme;

/**
 * Dialog "Yakin mau hapus?" bermaskot kucing.
 * Pemakaian: if (KonfirmasiDialog.tanya(window, "Hapus Data", "Yakin?", "keterangan")) { ... }
 */
public class KonfirmasiDialog extends PinkDialog {

    private boolean jawabanYa = false;

    private KonfirmasiDialog(Window pemilik, String judul, String pertanyaan, String keterangan) {
        super(pemilik, judul, 440, 210);

        JPanel badan = new JPanel(new BorderLayout(16, 0));
        badan.setOpaque(false);
        badan.setBorder(BorderFactory.createEmptyBorder(22, 24, 8, 24));
        badan.add(new AvaterLabel(ImageLoader.kucing(), 72), BorderLayout.WEST);

        JLabel lTanya = new JLabel(pertanyaan);
        lTanya.setFont(Theme.font(Font.BOLD, 15));
        lTanya.setForeground(Theme.TEKS_TUA);
        JLabel lKet = new JLabel("<html>" + keterangan + "</html>");
        lKet.setFont(Theme.font(Font.PLAIN, 12));
        lKet.setForeground(Theme.TEKS_MUDA);

        JPanel teks = new JPanel(new GridLayout(2, 1, 0, 4));
        teks.setOpaque(false);
        teks.add(lTanya);
        teks.add(lKet);
        badan.add(teks, BorderLayout.CENTER);

        OutlineButton btnBatal = new OutlineButton("Batal");
        btnBatal.setPreferredSize(new Dimension(90, 36));
        btnBatal.addActionListener(e -> dispose());

        PillButton btnYa = new PillButton("Ya, hapus!");
        btnYa.setFont(Theme.font(Font.BOLD, 13));
        btnYa.setPreferredSize(new Dimension(120, 36));
        btnYa.addActionListener(e -> {
            jawabanYa = true;
            dispose();
        });

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        tombol.setOpaque(false);
        tombol.add(btnBatal);
        tombol.add(btnYa);

        getIsi().add(badan, BorderLayout.CENTER);
        getIsi().add(tombol, BorderLayout.SOUTH);
    }

    /** Menampilkan dialog dan menunggu jawaban. true = user menekan "Ya". */
    public static boolean tanya(Window pemilik, String judul, String pertanyaan, String keterangan) {
        KonfirmasiDialog dialog = new KonfirmasiDialog(pemilik, judul, pertanyaan, keterangan);
        dialog.setVisible(true);          // berhenti di sini sampai dialog ditutup (modal)
        return dialog.jawabanYa;
    }
}
