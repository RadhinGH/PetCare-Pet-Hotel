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
import java.sql.SQLException;
import java.time.LocalDateTime;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import dao.LayananDAO;
import model.Layanan;
import util.FormatUang;
import util.ToasterPopOut;
import view_need.FormBantu;
import view_need.OutlineButton;
import view_need.PillButton;
import view_need.PinkDialog;
import view_need.RoundedPanel;
import view_need.RoundedTextField;

/**
 * Dialog pembayaran tagihan. Hanya dipakai untuk layanan yang sudah Check-out.
 * Setelah dikonfirmasi, layanan berstatus LUNAS dan baru dihitung sebagai pendapatan.
 */
public class PembayaranDialog extends PinkDialog {

    private static final String TUNAI = "Tunai";

    private final Layanan layanan;
    private final double totalTagihan;
    private boolean berhasil = false;

    private final JComboBox<String> pilihMetode = new JComboBox<>(
            new String[]{TUNAI, "QRIS", "Transfer Bank", "E-Wallet"});
    private final RoundedTextField kotakNominal = new RoundedTextField();
    private final JLabel lblKembalian = new JLabel(" ");

    public PembayaranDialog(Window pemilik, Layanan layanan) {
        super(pemilik, "Pembayaran \u2014 " + layanan.getKode(), 460, 500);
        this.layanan = layanan;
        this.totalTagihan = layanan.hitungTotalBiaya();

        getIsi().add(bangunIsi(), BorderLayout.NORTH);
        getIsi().add(bangunTombol(), BorderLayout.SOUTH);

        kotakNominal.setText(String.valueOf(Math.round(totalTagihan)));
        pilihMetode.addActionListener(e -> metodeBerubah());
        kotakNominal.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { hitungKembalian(); }
            @Override public void removeUpdate(DocumentEvent e)  { hitungKembalian(); }
            @Override public void changedUpdate(DocumentEvent e) { hitungKembalian(); }
        });
        metodeBerubah();
    }

    public boolean isBerhasil() {
        return berhasil;
    }

    // ----- tampilan -----

    private JPanel bangunIsi() {
        JPanel isi = new JPanel(new GridBagLayout());
        isi.setOpaque(false);
        isi.setBorder(BorderFactory.createEmptyBorder(18, 26, 6, 26));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridy = 0;
        g.insets = new Insets(0, 0, 10, 0);
        isi.add(bangunKartuRingkasan(), g);

        g.gridy = 1;
        g.insets = new Insets(0, 0, 12, 0);
        isi.add(bangunKotakTotal(), g);

        pilihMetode.setFont(Theme.font(Font.PLAIN, 13));
        pilihMetode.setBackground(Color.WHITE);
        pilihMetode.setPreferredSize(new Dimension(10, 38));

        lblKembalian.setFont(Theme.font(Font.BOLD, 14));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        FormBantu.tambahBaris(form, 0, "METODE PEMBAYARAN", pilihMetode);
        FormBantu.tambahBaris(form, 1, "NOMINAL BAYAR (RP)", kotakNominal);
        FormBantu.tambahBaris(form, 2, "KEMBALIAN", lblKembalian);

        g.gridy = 2;
        g.insets = new Insets(0, 0, 0, 0);
        isi.add(form, g);
        return isi;
    }

    private RoundedPanel bangunKartuRingkasan() {
        JLabel judul = new JLabel(layanan.getHewan().getNama() + "  \u2022  " + layanan.getNamaLayanan());
        judul.setFont(Theme.font(Font.BOLD, 14));
        judul.setForeground(Theme.TEKS_TUA);

        JLabel info = new JLabel(layanan.getDetail() + "  \u2022  pemilik: " + layanan.getHewan().getNamaPemilik());
        info.setFont(Theme.font(Font.PLAIN, 11));
        info.setForeground(Theme.TEKS_MUDA);

        RoundedPanel kartu = new RoundedPanel(16);
        kartu.setBackground(new Color(0xFFF1F7));
        kartu.setBorderColor(Theme.BORDER_FIELD);
        kartu.setLayout(new BorderLayout(0, 3));
        kartu.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        kartu.add(judul, BorderLayout.NORTH);
        kartu.add(info, BorderLayout.CENTER);
        return kartu;
    }

    private RoundedPanel bangunKotakTotal() {
        JLabel kiri = new JLabel("TOTAL TAGIHAN");
        kiri.setFont(Theme.font(Font.BOLD, 13));
        kiri.setForeground(Theme.SUKSES);

        JLabel kanan = new JLabel(FormatUang.rupiah(totalTagihan), SwingConstants.RIGHT);
        kanan.setFont(Theme.font(Font.BOLD, 24));
        kanan.setForeground(Theme.SUKSES);

        RoundedPanel kotak = new RoundedPanel(16);
        kotak.setBackground(Theme.SUKSES_MUDA);
        kotak.setBorderColor(new Color(0xA9E1C5));
        kotak.setLayout(new BorderLayout());
        kotak.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        kotak.add(kiri, BorderLayout.WEST);
        kotak.add(kanan, BorderLayout.EAST);
        return kotak;
    }

    private JPanel bangunTombol() {
        OutlineButton btnBatal = new OutlineButton("Batal");
        btnBatal.setPreferredSize(new Dimension(90, 38));
        btnBatal.addActionListener(e -> dispose());

        PillButton btnBayar = new PillButton("Konfirmasi & Bayar");
        btnBayar.setFont(Theme.font(Font.BOLD, 13));
        btnBayar.setPreferredSize(new Dimension(180, 38));
        btnBayar.addActionListener(e -> bayar());

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        tombol.setOpaque(false);
        tombol.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 16));
        tombol.add(btnBatal);
        tombol.add(btnBayar);
        return tombol;
    }

    // ----- logika -----

    /** Tunai: nominal boleh diubah. Metode lain: nominal otomatis pas dengan tagihan. */
    private void metodeBerubah() {
        boolean tunai = TUNAI.equals(pilihMetode.getSelectedItem());
        kotakNominal.setEditable(tunai);
        if (!tunai) {
            kotakNominal.setText(String.valueOf(Math.round(totalTagihan)));
        }
        hitungKembalian();
    }

    /** Mengubah teks nominal menjadi angka. Mengembalikan -1 kalau tidak valid. */
    private double bacaNominal() {
        String bersih = kotakNominal.getText().replace("Rp", "").replace(".", "")
                .replace(",", "").replace(" ", "");
        try {
            return Double.parseDouble(bersih);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void hitungKembalian() {
        if (!TUNAI.equals(pilihMetode.getSelectedItem())) {
            lblKembalian.setForeground(Theme.SUKSES);
            lblKembalian.setText("Rp0 (pembayaran pas non-tunai)");
            return;
        }
        double nominal = bacaNominal();
        if (nominal < 0) {
            lblKembalian.setForeground(Theme.ERROR);
            lblKembalian.setText("Masukkan angka yang valid");
        } else if (nominal < totalTagihan) {
            lblKembalian.setForeground(Theme.ERROR);
            lblKembalian.setText("Kurang " + FormatUang.rupiah(totalTagihan - nominal));
        } else {
            lblKembalian.setForeground(Theme.SUKSES);
            lblKembalian.setText(FormatUang.rupiah(nominal - totalTagihan));
        }
    }

    private void bayar() {
        if (!layanan.bisaDibayar()) {
            ToasterPopOut.error(this, "Meow? Belum bisa dibayar!", "tagihan harus Check-out dan belum lunas");
            return;
        }
        double nominal = bacaNominal();
        if (nominal < 0) {
            ToasterPopOut.error(this, "Meow? Nominal tidak valid!", "isi nominal dengan angka");
            return;
        }
        if (nominal < totalTagihan) {
            ToasterPopOut.error(this, "Meow? Uang kurang!",
                    "kurang " + FormatUang.rupiah(totalTagihan - nominal));
            return;
        }

        String metode = (String) pilihMetode.getSelectedItem();
        String waktu = LocalDateTime.now().format(Layanan.FORMAT_WAKTU_BAYAR);
        try {
            layanan.tandaiLunas(metode, nominal, waktu);
            new LayananDAO().simpanPembayaran(layanan);
            berhasil = true;
            dispose();
        } catch (SQLException e) {
            // simpan gagal -> kembalikan ke BELUM_BAYAR di memori
            layanan.setStatusPembayaran(model.StatusPembayaran.BELUM_BAYAR);
            layanan.setMetodePembayaran(null);
            layanan.setJumlahDibayar(0);
            layanan.setTanggalBayar(null);
            ToasterPopOut.error(this, "Meow? Gagal menyimpan pembayaran!", e.getMessage());
        }
    }
}
