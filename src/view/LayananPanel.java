package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import dao.HewanDAO;
import dao.LayananDAO;
import model.Hewan;
import model.Layanan;
import model.StatusPembayaran;
import model.StatusPenitipan;
import model.User;
import util.AvaterLabel;
import util.FormatUang;
import util.ImageLoader;
import util.ToasterPopOut;
import view_need.KonfirmasiDialog;
import view_need.OutlineButton;
import view_need.PillButton;
import view_need.PinkTable;
import view_need.RoundedTextField;

/**
 * Halaman "Layanan & Penitipan": daftar layanan, tracking status
 * (Check-in -> Perawatan -> Check-out), dan tagihan.
 */
public class LayananPanel extends JPanel {

    private static final String[] KOLOM = {"Kode", "Nama", "Hewan", "Layanan", "Tanggal", "Detail",
            "Status", "Pembayaran", "Total"};
    private static final int KOLOM_STATUS = 6;
    private static final int KOLOM_PEMBAYARAN = 7;

    private final User user;
    private final LayananDAO dao = new LayananDAO();
    private final List<Layanan> daftar = new ArrayList<>();
    private final Consumer<String> pesanStatus;   // dikirim ke status bar di MainFrame

    private final DefaultTableModel modelTabel = new DefaultTableModel(KOLOM, 0);
    private final PinkTable tabel = new PinkTable(modelTabel);

    // pencarian, filter, dan urutan (khusus admin)
    private final RoundedTextField kotakCari = new RoundedTextField();
    private final JComboBox<String> pilihFilter = new JComboBox<>(new String[]{
            "Semua", "Grooming", "Boarding", "Check-in", "Perawatan", "Check-out", "Belum Bayar", "Lunas"});
    private final JComboBox<String> pilihUrutan = new JComboBox<>(new String[]{
            "Terbaru", "Terlama", "Kode (A-Z)", "Nama Hewan (A-Z)", "Jenis Hewan", "Status",
            "Total Tertinggi", "Total Terendah"});
    private final JLabel lblKosong = new JLabel("Zzz\u2026 belum ada layanan");

    private final CardLayout susunanIsi = new CardLayout();
    private final JPanel areaIsi = new JPanel(susunanIsi);

    public LayananPanel(User user, Consumer<String> pesanStatus) {
        this.user = user;
        this.pesanStatus = pesanStatus;

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.LATAR_KONTEN);
        setBorder(BorderFactory.createEmptyBorder(22, 26, 18, 26));

        add(bangunAtas(), BorderLayout.NORTH);
        add(bangunIsi(), BorderLayout.CENTER);
        add(bangunBawah(), BorderLayout.SOUTH);

        // setiap baris dipilih -> status bar menjelaskan langkah selanjutnya
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                perbaruiStatusBar();
            }
        });

        muatData();
    }

    // ----- tampilan -----

    private JPanel bangunAtas() {
        String teksJudul = user.isPelanggan() ? "Layanan Saya" : "Layanan & Penitipan";
        String teksSub = user.isPelanggan()
                ? "Pesan layanan, pantau status perawatan, dan unduh struk tagihan Anda"
                : "Grooming, boarding, tracking status, dan tagihan";
        String teksBtn = user.isPelanggan() ? "+ Pesan Layanan" : "+ Layanan Baru";

        JLabel judul = new JLabel(teksJudul);
        judul.setFont(Theme.font(Font.BOLD, 22));
        judul.setForeground(Theme.PRIMARY_TUA);
        JLabel sub = new JLabel(teksSub);
        sub.setFont(Theme.font(Font.PLAIN, 12));
        sub.setForeground(Theme.TEKS_MUDA);

        JPanel kiri = new JPanel(new BorderLayout());
        kiri.setOpaque(false);
        kiri.add(judul, BorderLayout.NORTH);
        kiri.add(sub, BorderLayout.SOUTH);

        PillButton btnBaru = new PillButton(teksBtn);
        btnBaru.setFont(Theme.font(Font.BOLD, 13));
        btnBaru.setPreferredSize(new Dimension(155, 38));
        btnBaru.addActionListener(e -> tambah());

        JPanel kanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        kanan.setOpaque(false);
        kanan.add(btnBaru);

        JPanel barisJudul = new JPanel(new BorderLayout());
        barisJudul.setOpaque(false);
        barisJudul.add(kiri, BorderLayout.WEST);
        barisJudul.add(kanan, BorderLayout.EAST);

        if (user.isPelanggan()) {
            return barisJudul;
        }

        // admin: tambahan baris cari + filter + urutan
        kotakCari.setPreferredSize(new Dimension(175, 36));
        kotakCari.setToolTipText("Cari kode, nama/jenis hewan, atau pemilik");
        kotakCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { muatData(); }
            @Override public void removeUpdate(DocumentEvent e)  { muatData(); }
            @Override public void changedUpdate(DocumentEvent e) { muatData(); }
        });

        pilihFilter.setFont(Theme.font(Font.PLAIN, 13));
        pilihFilter.setBackground(Color.WHITE);
        pilihFilter.setPreferredSize(new Dimension(135, 36));
        pilihFilter.addActionListener(e -> muatData());

        pilihUrutan.setFont(Theme.font(Font.PLAIN, 13));
        pilihUrutan.setBackground(Color.WHITE);
        pilihUrutan.setPreferredSize(new Dimension(155, 36));
        pilihUrutan.addActionListener(e -> muatData());

        JPanel barisFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barisFilter.setOpaque(false);
        barisFilter.add(labelFilter("Cari:"));
        barisFilter.add(kotakCari);
        barisFilter.add(labelFilter("Filter:"));
        barisFilter.add(pilihFilter);
        barisFilter.add(labelFilter("Urutkan:"));
        barisFilter.add(pilihUrutan);

        JPanel atas = new JPanel(new BorderLayout(0, 10));
        atas.setOpaque(false);
        atas.add(barisJudul, BorderLayout.NORTH);
        atas.add(barisFilter, BorderLayout.SOUTH);
        return atas;
    }

    private JLabel labelFilter(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(Theme.font(Font.BOLD, 12));
        l.setForeground(Theme.TEKS_MUDA);
        return l;
    }

    private JPanel bangunIsi() {
        areaIsi.setOpaque(false);

        // kolom Status digambar sebagai "badge" berwarna
        tabel.getColumnModel().getColumn(KOLOM_STATUS).setCellRenderer(new StatusBadgeRenderer());
        tabel.getColumnModel().getColumn(KOLOM_PEMBAYARAN).setCellRenderer(new PembayaranBadgeRenderer());

        tabel.aturLebar(76, 78, 64, 105, 82, 82, 94, 98, 80);
        JScrollPane gulir = new JScrollPane(tabel);
        gulir.setBorder(BorderFactory.createLineBorder(Theme.BORDER_FIELD));
        gulir.getViewport().setBackground(Color.WHITE);
        areaIsi.add(gulir, "tabel");
        areaIsi.add(bangunKosong(), "kosong");
        return areaIsi;
    }

    private JPanel bangunKosong() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(Theme.BORDER_FIELD));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.insets = new java.awt.Insets(0, 0, 8, 0);
        p.add(new AvaterLabel(ImageLoader.kucing(), 110), g);

        g.gridy = 1;
        lblKosong.setFont(Theme.font(Font.BOLD, 15));
        lblKosong.setForeground(new Color(0x7C5CC4));
        p.add(lblKosong, g);

        g.gridy = 2;
        PillButton btn = new PillButton(user.isPelanggan() ? "+ Pesan Layanan" : "+ Layanan Baru");
        btn.setFont(Theme.font(Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(170, 38));
        btn.addActionListener(e -> tambah());
        p.add(btn, g);
        return p;
    }

    private JPanel bangunBawah() {
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bawah.setOpaque(false);

        if (user.isPelanggan()) {
            PillButton btnBayar = new PillButton("Bayar Sekarang");
            btnBayar.setFont(Theme.font(Font.BOLD, 13));
            btnBayar.setPreferredSize(new Dimension(150, 36));
            btnBayar.addActionListener(e -> bayar());

            OutlineButton btnTagihan = new OutlineButton("Lihat Tagihan & Struk");
            btnTagihan.setPreferredSize(new Dimension(180, 36));
            btnTagihan.addActionListener(e -> tampilkanTagihan());
            bawah.add(btnBayar);
            bawah.add(btnTagihan);
            return bawah;
        }

        OutlineButton btnStatus = new OutlineButton("Update Status");
        btnStatus.setPreferredSize(new Dimension(130, 36));
        btnStatus.addActionListener(e -> updateStatus());

        OutlineButton btnBayar = new OutlineButton("Bayar / Lunasi");
        btnBayar.setPreferredSize(new Dimension(130, 36));
        btnBayar.addActionListener(e -> bayar());

        OutlineButton btnTagihan = new OutlineButton("Tagihan & Struk");
        btnTagihan.setPreferredSize(new Dimension(140, 36));
        btnTagihan.addActionListener(e -> tampilkanTagihan());

        OutlineButton btnHapus = new OutlineButton("Hapus");
        btnHapus.setPreferredSize(new Dimension(90, 36));
        btnHapus.addActionListener(e -> hapus());

        bawah.add(btnStatus);
        bawah.add(btnBayar);
        bawah.add(btnTagihan);
        bawah.add(btnHapus);
        return bawah;
    }

    // ----- READ -----

    public void muatData() {
        try {
            boolean adaFilter = false;
            daftar.clear();
            if (user.isPelanggan()) {
                daftar.addAll(dao.semuaLayananMilik(user.getId()));
            } else {
                String kata = kotakCari.getText().trim();
                String filter = (String) pilihFilter.getSelectedItem();
                String urutan = (String) pilihUrutan.getSelectedItem();
                adaFilter = !kata.isEmpty() || !"Semua".equals(filter);
                daftar.addAll(dao.cari(kata, filter, urutan));
            }

            modelTabel.setRowCount(0);
            for (Layanan l : daftar) {
                modelTabel.addRow(new Object[]{
                        l.getKode(), l.getHewan().getNama(), l.getHewan().getJenis(),
                        l.getNamaLayanan(), l.getTanggal().format(Layanan.FORMAT_TAMPIL),
                        l.getDetail(), l.getStatus(), l.getStatusPembayaran(),
                        FormatUang.rupiah(l.hitungTotalBiaya())});
            }
            lblKosong.setText(adaFilter ? "Zzz\u2026 tidak ada layanan yang cocok"
                                        : "Zzz\u2026 belum ada layanan");
            susunanIsi.show(areaIsi, daftar.isEmpty() ? "kosong" : "tabel");
            perbaruiStatusBar();
        } catch (SQLException e) {
            ToasterPopOut.error(this, "Meow? Database bermasalah!", e.getMessage());
        }
    }

    /** Teks di status bar bawah: jumlah layanan + langkah status untuk baris terpilih. */
    private void perbaruiStatusBar() {
        String teks = daftar.size() + " layanan";
        int baris = tabel.getSelectedRow();
        if (baris >= 0 && baris < daftar.size()) {
            Layanan l = daftar.get(baris);
            if (l.isLunas()) {
                teks += "  \u2022  " + l.getKode() + " \u2014 LUNAS (" + l.getMetodePembayaran()
                        + "), struk siap diunduh";
            } else if (l.bisaDibayar()) {
                teks += "  \u2022  " + l.getKode()
                        + " \u2014 Check-out (Menunggu Pembayaran), klik Bayar untuk melunasi";
            } else if (user.isPelanggan()) {
                teks += "  \u2022  " + l.getKode() + " \u2014 Status: "
                        + l.getStatus().getLabel().toUpperCase() + " (bayar saat Check-out)";
            } else {
                StatusPenitipan berikut = l.getStatus().berikutnya();
                teks += "  \u2022  " + l.getKode() + " dipilih \u2014 ";
                teks += berikut == null
                        ? "sudah " + l.getStatus().getLabel().toUpperCase()
                        : "Update Status: " + l.getStatus().getLabel().toUpperCase()
                          + " \u2192 " + berikut.getLabel().toUpperCase();
            }
        }
        pesanStatus.accept(teks);
    }

    // ----- CREATE / UPDATE / DELETE -----

    private Window jendela() {
        return SwingUtilities.getWindowAncestor(this);
    }

    private void tambah() {
        List<Hewan> semuaHewan;
        try {
            HewanDAO hewanDao = new HewanDAO();
            semuaHewan = user.isPelanggan()
                    ? hewanDao.semuaHewanMilik(user.getId())
                    : hewanDao.semuaHewan();
        } catch (SQLException e) {
            ToasterPopOut.error(this, "Meow? Database bermasalah!", e.getMessage());
            return;
        }
        if (semuaHewan.isEmpty()) {
            String menuHewan = user.isPelanggan() ? "Hewan Saya" : "Data Hewan";
            ToasterPopOut.error(this, "Meow? Belum ada hewan!", "daftarkan hewan dulu di menu " + menuHewan);
            return;
        }

        LayananFormDialog form = new LayananFormDialog(jendela(), semuaHewan);
        form.setVisible(true);

        Layanan baru = form.getHasil();
        if (baru == null) {
            return;
        }
        try {
            dao.tambah(baru);
            muatData();
            ToasterPopOut.sukses(this, "Yeay! Layanan " + baru.getKode() + " dibuat!",
                    baru.getHewan().getNama() + " \u2022 " + baru.getNamaLayanan());
        } catch (SQLException e) {
            ToasterPopOut.error(this, "Meow? Gagal menyimpan!", e.getMessage());
        }
    }

    private void updateStatus() {
        Layanan l = layananDipilih();
        if (l == null) {
            return;
        }
        StatusPenitipan lama = l.getStatus();
        StatusPenitipan baru = lama.berikutnya();
        if (baru == null) {
            ToasterPopOut.error(this, "Meow? Sudah Check-out!", "status tidak bisa dimajukan lagi");
            return;
        }
        try {
            l.setStatus(baru);
            dao.ubahStatus(l);

            muatData();
            // pilih kembali baris yang sama (bisa saja sudah berpindah/hilang karena filter atau urutan)
            for (int i = 0; i < daftar.size(); i++) {
                if (daftar.get(i).getId() == l.getId()) {
                    tabel.setRowSelectionInterval(i, i);
                    break;
                }
            }
            ToasterPopOut.sukses(this, "Yeay! Status " + l.getKode() + " diperbarui!",
                    baru == StatusPenitipan.CHECK_OUT
                            ? "Perawatan \u2192 Check-out (menunggu pembayaran)"
                            : lama.getLabel() + " \u2192 " + baru.getLabel());
        } catch (SQLException e) {
            l.setStatus(lama); // batalkan perubahan di memori
            ToasterPopOut.error(this, "Meow? Gagal memperbarui status!", e.getMessage());
        }
    }

    /** Membuka dialog pembayaran untuk layanan terpilih (hanya bila sudah Check-out & belum lunas). */
    private void bayar() {
        Layanan l = layananDipilih();
        if (l == null) {
            return;
        }
        if (l.isLunas()) {
            ToasterPopOut.error(this, "Tagihan sudah lunas!",
                    l.getKode() + " sudah dibayar (" + l.getMetodePembayaran() + ")");
            return;
        }
        if (!l.bisaDibayar()) {
            ToasterPopOut.error(this, "Belum bisa dibayar!", "pembayaran dilakukan saat status sudah Check-out");
            return;
        }

        PembayaranDialog dialog = new PembayaranDialog(jendela(), l);
        dialog.setVisible(true);
        if (dialog.isBerhasil()) {
            muatData();
            ToasterPopOut.sukses(this, "Pembayaran " + l.getKode() + " LUNAS!",
                    "Metode: " + l.getMetodePembayaran() + " \u2022 masuk laporan pendapatan");
            new StrukPreviewDialog(jendela(), l).setVisible(true);
        }
    }

    private void tampilkanTagihan() {
        Layanan l = layananDipilih();
        if (l != null) {
            new TagihanDialog(jendela(), l, this::muatData).setVisible(true);
        }
    }

    private void hapus() {
        Layanan l = layananDipilih();
        if (l == null) {
            return;
        }
        boolean yakin = KonfirmasiDialog.tanya(jendela(), "Hapus Layanan",
                "Yakin mau hapus " + l.getKode() + "?", "riwayat layanan ini nggak bisa kembali lho\u2026");
        if (!yakin) {
            return;
        }
        try {
            dao.hapus(l.getId());
            muatData();
            ToasterPopOut.sukses(this, l.getKode() + " sudah dihapus", "layanan terhapus");
        } catch (SQLException e) {
            ToasterPopOut.error(this, "Meow? Gagal menghapus!", e.getMessage());
        }
    }

    private Layanan layananDipilih() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            ToasterPopOut.error(this, "Meow? Pilih dulu layanannya!", "klik salah satu baris di tabel");
            return null;
        }
        return daftar.get(baris);
    }

    // ----- badge status berwarna -----

    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object nilai, boolean dipilih,
                                                       boolean fokus, int baris, int kolom) {
            StatusPenitipan status = (StatusPenitipan) nilai;
            Color latar;
            Color teks;
            switch (status) {
                case CHECK_IN:
                    latar = new Color(0xE3EEFF);
                    teks = new Color(0x3B6FD6);
                    break;
                case PERAWATAN:
                    latar = new Color(0xFFF1D6);
                    teks = new Color(0xB7791F);
                    break;
                default:
                    latar = Theme.SUKSES_MUDA;
                    teks = Theme.SUKSES;
            }

            Color alasBaris = dipilih ? Theme.BARIS_DIPILIH
                    : (baris % 2 == 0 ? Color.WHITE : Theme.BARIS_GANJIL);
            return new BadgeLabel(status.getLabel(), latar, teks, alasBaris);
        }
    }

    /** Badge kolom Pembayaran: hijau = Lunas, merah muda = Belum Bayar. */
    private static class PembayaranBadgeRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object nilai, boolean dipilih,
                                                       boolean fokus, int baris, int kolom) {
            StatusPembayaran sp = (StatusPembayaran) nilai;
            Color latar = sp == StatusPembayaran.LUNAS ? Theme.SUKSES_MUDA : new Color(0xFFE8EE);
            Color teks = sp == StatusPembayaran.LUNAS ? Theme.SUKSES : Theme.ERROR;

            Color alasBaris = dipilih ? Theme.BARIS_DIPILIH
                    : (baris % 2 == 0 ? Color.WHITE : Theme.BARIS_GANJIL);
            return new BadgeLabel(sp.getLabel(), latar, teks, alasBaris);
        }
    }

    /** Label berbentuk pil. */
    private static class BadgeLabel extends JLabel {
        private final Color latar;
        private final Color alasBaris;

        BadgeLabel(String teks, Color latar, Color warnaTeks, Color alasBaris) {
            super(teks, SwingConstants.CENTER);
            this.latar = latar;
            this.alasBaris = alasBaris;
            setForeground(warnaTeks);
            setFont(Theme.font(Font.BOLD, 11));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(alasBaris);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(latar);
            int tinggi = 24;
            int lebar = Math.min(getWidth() - 16, 100);
            g2.fillRoundRect((getWidth() - lebar) / 2, (getHeight() - tinggi) / 2, lebar, tinggi, tinggi, tinggi);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
