package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import model.BarisTagihan;
import model.Hewan;
import model.Layanan;
import util.AvaterLabel;
import util.FormatUang;
import util.ImageLoader;
import view_need.OutlineButton;
import view_need.PillButton;
import view_need.PinkDialog;
import view_need.RoundedPanel;

/**
 * Dialog rincian tagihan. Isinya diambil dari interface Tagihan:
 * rincianTagihan() dan hitungTotalBiaya(). Grooming dan Boarding sama-sama
 * dipanggil lewat cara yang sama, tetapi hasilnya berbeda (polymorphism).
 */
public class TagihanDialog extends PinkDialog {

    private final Layanan layanan;
    private final Window pemilikDialog;
    private final Runnable onBerubah;   // dipanggil setelah pembayaran berhasil (untuk refresh tabel)

    public TagihanDialog(Window pemilik, Layanan layanan) {
        this(pemilik, layanan, null);
    }

    public TagihanDialog(Window pemilik, Layanan layanan, Runnable onBerubah) {
        super(pemilik, "Tagihan \u2014 " + layanan.getKode(), 560, 540);
        this.layanan = layanan;
        this.pemilikDialog = pemilik;
        this.onBerubah = onBerubah;

        JPanel badan = new JPanel(new GridBagLayout());
        badan.setOpaque(false);
        badan.setBorder(BorderFactory.createEmptyBorder(18, 24, 0, 24));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridy = 0;
        g.insets = new Insets(0, 0, 10, 0);
        badan.add(bangunKartuHewan(), g);

        int baris = 1;
        for (BarisTagihan b : layanan.rincianTagihan()) {
            g.gridy = baris++;
            g.insets = new Insets(0, 0, 0, 0);
            badan.add(bangunBaris(b), g);
        }

        g.gridy = baris++;
        g.insets = new Insets(14, 0, 0, 0);
        badan.add(bangunTotal(), g);

        g.gridy = baris;
        g.weighty = 1.0;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(10, 0, 0, 0);
        String teksCatatan;
        Color warnaCatatan;
        if (layanan.isLunas()) {
            teksCatatan = "Status: LUNAS (" + layanan.getMetodePembayaran() + " \u2022 "
                    + layanan.getTanggalBayar() + ") \u2014 struk pembayaran siap diunduh.";
            warnaCatatan = Theme.SUKSES;
        } else if (layanan.bisaDibayar()) {
            teksCatatan = "Status: CHECK-OUT \u2022 Belum Bayar \u2014 klik Bayar Sekarang untuk melunasi tagihan.";
            warnaCatatan = Theme.PRIMARY_TUA;
        } else {
            teksCatatan = "Status: " + layanan.getStatus().getLabel().toUpperCase()
                    + " \u2022 Belum Bayar \u2014 pembayaran dilakukan saat Check-out.";
            warnaCatatan = Theme.TEKS_MUDA;
        }
        JLabel catatan = new JLabel("<html><i>" + teksCatatan + "</i></html>");
        catatan.setFont(Theme.font(Font.PLAIN, 11));
        catatan.setForeground(warnaCatatan);
        badan.add(catatan, g);

        getIsi().add(badan, BorderLayout.CENTER);
        getIsi().add(bangunTombol(), BorderLayout.SOUTH);
    }

    private RoundedPanel bangunKartuHewan() {
        Hewan h = layanan.getHewan();

        RoundedPanel kartu = new RoundedPanel(18);
        kartu.setBackground(new Color(0xFFF1F7));
        kartu.setBorderColor(Theme.BORDER_FIELD);
        kartu.setLayout(new BorderLayout(14, 0));
        kartu.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        kartu.add(new AvaterLabel(ImageLoader.kucing(), 64), BorderLayout.WEST);

        JLabel judul = new JLabel(h.getNama() + "  \u2022  " + layanan.getNamaLayanan());
        judul.setFont(Theme.font(Font.BOLD, 16));
        judul.setForeground(Theme.TEKS_TUA);

        JLabel info = new JLabel("<html>" + h.getJenis() + " (" + h.getDetail() + ")  \u2022  pemilik: "
                + h.getNamaPemilik() + "  \u2022  " + h.getTeleponPemilik() + "  \u2022  "
                + layanan.getTanggal().format(Layanan.FORMAT_TAMPIL) + "</html>");
        info.setFont(Theme.font(Font.PLAIN, 11));
        info.setForeground(Theme.TEKS_MUDA);

        JPanel teks = new JPanel(new BorderLayout(0, 4));
        teks.setOpaque(false);
        teks.add(judul, BorderLayout.NORTH);
        teks.add(info, BorderLayout.CENTER);
        kartu.add(teks, BorderLayout.CENTER);
        return kartu;
    }

    private JPanel bangunBaris(BarisTagihan b) {
        JLabel kiri = new JLabel(b.getLabel());
        kiri.setFont(Theme.font(Font.PLAIN, 13));
        kiri.setForeground(Theme.TEKS_TUA);

        JLabel kanan = new JLabel(b.getNilai(), SwingConstants.RIGHT);
        kanan.setFont(Theme.font(Font.BOLD, 13));
        kanan.setForeground(b.isPotongan() ? Theme.SUKSES : Theme.TEKS_TUA);

        JPanel baris = new JPanel(new BorderLayout());
        baris.setOpaque(false);
        baris.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xFBE3EE)),
                BorderFactory.createEmptyBorder(11, 4, 11, 4)));
        baris.add(kiri, BorderLayout.WEST);
        baris.add(kanan, BorderLayout.EAST);
        return baris;
    }

    private RoundedPanel bangunTotal() {
        JLabel kiri = new JLabel("TOTAL TAGIHAN");
        kiri.setFont(Theme.font(Font.BOLD, 13));
        kiri.setForeground(Theme.SUKSES);

        JLabel kanan = new JLabel(FormatUang.rupiah(layanan.hitungTotalBiaya()), SwingConstants.RIGHT);
        kanan.setFont(Theme.font(Font.BOLD, 26));
        kanan.setForeground(Theme.SUKSES);

        RoundedPanel kotak = new RoundedPanel(18);
        kotak.setBackground(Theme.SUKSES_MUDA);
        kotak.setBorderColor(new Color(0xA9E1C5));
        kotak.setLayout(new BorderLayout());
        kotak.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        kotak.add(kiri, BorderLayout.WEST);
        kotak.add(kanan, BorderLayout.EAST);
        return kotak;
    }

    private JPanel bangunTombol() {
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        tombol.setOpaque(false);
        tombol.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 16));

        if (layanan.bisaDibayar()) {
            // sudah Check-out tapi belum dibayar
            OutlineButton btnTutup = new OutlineButton("Tutup");
            btnTutup.setPreferredSize(new Dimension(100, 38));
            btnTutup.addActionListener(e -> dispose());

            PillButton btnBayar = new PillButton("Bayar Sekarang");
            btnBayar.setFont(Theme.font(Font.BOLD, 14));
            btnBayar.setPreferredSize(new Dimension(150, 38));
            btnBayar.addActionListener(e -> prosesBayar());

            tombol.add(btnTutup);
            tombol.add(btnBayar);
        } else if (layanan.isLunas()) {
            // sudah lunas -> struk boleh diunduh
            OutlineButton btnCetak = new OutlineButton("Download Struk");
            btnCetak.setPreferredSize(new Dimension(145, 38));
            btnCetak.addActionListener(e -> cetakStruk());

            PillButton btnTutup = new PillButton("Tutup");
            btnTutup.setFont(Theme.font(Font.BOLD, 14));
            btnTutup.setPreferredSize(new Dimension(110, 38));
            btnTutup.addActionListener(e -> dispose());

            tombol.add(btnCetak);
            tombol.add(btnTutup);
        } else {
            // masih Check-in / Perawatan -> belum bisa bayar
            PillButton btnTutup = new PillButton("Tutup");
            btnTutup.setFont(Theme.font(Font.BOLD, 14));
            btnTutup.setPreferredSize(new Dimension(110, 38));
            btnTutup.addActionListener(e -> dispose());
            tombol.add(btnTutup);
        }
        return tombol;
    }

    /** Membuka dialog pembayaran; kalau berhasil, langsung tampilkan struk LUNAS. */
    private void prosesBayar() {
        PembayaranDialog dialog = new PembayaranDialog(this, layanan);
        dialog.setVisible(true);
        if (!dialog.isBerhasil()) {
            return;
        }
        if (onBerubah != null) {
            onBerubah.run();
        }
        dispose();
        new StrukPreviewDialog(pemilikDialog, layanan).setVisible(true);
    }

    /** Membuka pratinjau struk visual yang siap diunduh sebagai gambar PNG. */
    private void cetakStruk() {
        new StrukPreviewDialog(this, layanan).setVisible(true);
    }
}
