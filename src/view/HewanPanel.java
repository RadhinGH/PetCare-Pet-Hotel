package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import dao.HewanDAO;
import model.Hewan;
import model.User;
import util.AvaterLabel;
import util.ImageLoader;
import util.ToasterPopOut;
import view_need.KonfirmasiDialog;
import view_need.OutlineButton;
import view_need.PillButton;
import view_need.PinkTable;
import view_need.RoundedTextField;

/** Halaman "Data Hewan": tabel + cari + tambah / ubah / hapus (CRUD). */
public class HewanPanel extends JPanel {

    private static final String[] KOLOM = {"Nama", "Jenis", "Detail", "Umur", "Berat", "Pemilik", "Telepon"};

    private final User user;
    private final HewanDAO dao = new HewanDAO();
    private final List<Hewan> daftar = new ArrayList<>();   // isi tabel saat ini (urutan sama dengan baris)

    private final DefaultTableModel modelTabel = new DefaultTableModel(KOLOM, 0);
    private final PinkTable tabel = new PinkTable(modelTabel);
    private final RoundedTextField kotakCari = new RoundedTextField();
    private final JComboBox<String> pilihKategori = new JComboBox<>(
            new String[]{"Semua Jenis", "Kucing", "Anjing", "Reptil"});
    private final JComboBox<String> pilihUrutan = new JComboBox<>(
            new String[]{"Nama (A-Z)", "Jenis Hewan", "Jenis (Z-A)"});
    private final JLabel lblJumlah = new JLabel();

    private final CardLayout susunanIsi = new CardLayout();
    private final JPanel areaIsi = new JPanel(susunanIsi);   // kartu "tabel" atau "kosong"
    private final JLabel lblKosong = new JLabel("", SwingConstants.CENTER);

    public HewanPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.LATAR_KONTEN);
        setBorder(BorderFactory.createEmptyBorder(22, 26, 18, 26));

        add(bangunAtas(), BorderLayout.NORTH);
        add(bangunIsi(), BorderLayout.CENTER);
        add(bangunBawah(), BorderLayout.SOUTH);

        muatData();
    }

    // ----- tampilan -----

    private JPanel bangunAtas() {
        String teksJudul = user.isPelanggan() ? "Hewan Saya" : "Data Hewan";
        String teksSub = user.isPelanggan()
                ? "Kelola data peliharaan milik " + user.getNamaLengkap()
                : "Daftar seluruh peliharaan pelanggan yang terdaftar di PetCare";

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

        PillButton btnTambah = new PillButton("+ Tambah Hewan");
        btnTambah.setFont(Theme.font(Font.BOLD, 13));
        btnTambah.setPreferredSize(new Dimension(150, 38));
        btnTambah.addActionListener(e -> tambah());

        JPanel barisJudul = new JPanel(new BorderLayout());
        barisJudul.setOpaque(false);
        barisJudul.add(kiri, BorderLayout.WEST);
        barisJudul.add(btnTambah, BorderLayout.EAST);

        kotakCari.setPreferredSize(new Dimension(200, 36));
        kotakCari.setToolTipText("Cari nama hewan atau pemilik");
        kotakCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { muatData(); }
            @Override public void removeUpdate(DocumentEvent e)  { muatData(); }
            @Override public void changedUpdate(DocumentEvent e) { muatData(); }
        });

        pilihKategori.setFont(Theme.font(Font.PLAIN, 13));
        pilihKategori.setBackground(Color.WHITE);
        pilihKategori.setPreferredSize(new Dimension(135, 36));
        pilihKategori.addActionListener(e -> muatData());

        pilihUrutan.setFont(Theme.font(Font.PLAIN, 13));
        pilihUrutan.setBackground(Color.WHITE);
        pilihUrutan.setPreferredSize(new Dimension(140, 36));
        pilihUrutan.addActionListener(e -> muatData());

        JPanel barisFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        barisFilter.setOpaque(false);
        barisFilter.add(labelFilter("Cari:"));
        barisFilter.add(kotakCari);
        barisFilter.add(labelFilter("Kategori:"));
        barisFilter.add(pilihKategori);
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

        tabel.aturLebar(90, 70, 190, 60, 80, 150, 130);
        JScrollPane gulir = new JScrollPane(tabel);
        gulir.setBorder(BorderFactory.createLineBorder(Theme.BORDER_FIELD));
        gulir.getViewport().setBackground(Color.WHITE);
        areaIsi.add(gulir, "tabel");
        areaIsi.add(bangunKosong(), "kosong");
        return areaIsi;
    }

    /** Tampilan saat belum ada hewan (empty state). */
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
        JLabel ajak = new JLabel("daftarkan peliharaan pertamamu yuk!");
        ajak.setFont(Theme.font(Font.PLAIN, 12));
        ajak.setForeground(Theme.TEKS_MUDA);
        p.add(ajak, g);

        g.gridy = 3;
        PillButton btn = new PillButton("+ Daftarkan Peliharaan");
        btn.setFont(Theme.font(Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(200, 38));
        btn.addActionListener(e -> tambah());
        p.add(btn, g);
        return p;
    }

    private JPanel bangunBawah() {
        OutlineButton btnUbah = new OutlineButton("Ubah");
        btnUbah.setPreferredSize(new Dimension(90, 36));
        btnUbah.addActionListener(e -> ubah());

        OutlineButton btnHapus = new OutlineButton("Hapus");
        btnHapus.setPreferredSize(new Dimension(90, 36));
        btnHapus.addActionListener(e -> hapus());

        lblJumlah.setFont(Theme.font(Font.PLAIN, 12));
        lblJumlah.setForeground(Theme.TEKS_MUDA);

        JPanel kiri = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        kiri.setOpaque(false);
        kiri.add(btnUbah);
        kiri.add(btnHapus);

        JPanel bawah = new JPanel(new BorderLayout());
        bawah.setOpaque(false);
        bawah.add(kiri, BorderLayout.WEST);
        bawah.add(lblJumlah, BorderLayout.EAST);
        return bawah;
    }

    // ----- READ -----

    /** Mengisi ulang tabel dari database (memakai filter cari, kategori, urutan, dan pemilik). */
    public void muatData() {
        try {
            String kata = kotakCari.getText().trim();
            String kategori = (String) pilihKategori.getSelectedItem();
            String urutan = (String) pilihUrutan.getSelectedItem();
            Integer pemilikId = user.isPelanggan() ? user.getId() : null;

            daftar.clear();
            daftar.addAll(dao.cari(kata, kategori, urutan, pemilikId));

            modelTabel.setRowCount(0);     // kosongkan tabel
            for (Hewan h : daftar) {
                modelTabel.addRow(new Object[]{ // isi ulang baris demi baris
                        h.getNama(), h.getJenis(),
                        h.getDetail(), h.getUmur() + " th",
                        h.getBerat() + " kg", h.getNamaPemilik(),
                        h.getTeleponPemilik()});
            }

            String teksJumlah = daftar.size() + " hewan";
            if (kategori != null && !"Semua Jenis".equals(kategori)) {
                teksJumlah += " (" + kategori + ")";
            }
            lblJumlah.setText(teksJumlah);

            boolean adaFilter = !kata.isEmpty() || (kategori != null && !"Semua Jenis".equals(kategori));
            if (adaFilter) {
                lblKosong.setText("Zzz\u2026 tidak ada hewan yang cocok");
            } else if (user.isPelanggan()) {
                lblKosong.setText("Zzz\u2026 kamu belum mendaftarkan peliharaan");
            } else {
                lblKosong.setText("Zzz\u2026 belum ada peliharaan");
            }
            susunanIsi.show(areaIsi, daftar.isEmpty() ? "kosong" : "tabel");
        } catch (SQLException e) {
            ToasterPopOut.error(this, "Meow? Database bermasalah!", e.getMessage());
        }
    }

    // ----- CREATE / UPDATE / DELETE -----

    private Window jendela() {
        return SwingUtilities.getWindowAncestor(this);
    }

    private void tambah() {
        HewanFormDialog form = new HewanFormDialog(jendela(), null, user);
        form.setVisible(true); // dialog modal: menunggu user selesai

        Hewan baru = form.getHasil(); // null kalau user membatalkan
        if (baru == null) {
            return;
        }
        if (user.isPelanggan()) {
            baru.setPemilikUserId(user.getId());
        }
        try {
            dao.tambah(baru); // CREATE: INSERT ke database
            muatData();       // tabel dimuat ulang dari database
            ToasterPopOut.sukses(this, "Yeay! " +
                    baru.getNama() + " berhasil ditambahkan!", "data hewan tersimpan");
        } catch (SQLException e) {
            ToasterPopOut.error(this, "Meow? Gagal menyimpan!", e.getMessage());
        }
    }

    private void ubah() {
        Hewan dipilih = hewanDipilih();
        if (dipilih == null) {
            return;
        }
        HewanFormDialog form = new HewanFormDialog(jendela(), dipilih, user);
        form.setVisible(true);

        Hewan hasil = form.getHasil();
        if (hasil == null) {
            return;
        }
        try {
            dao.ubah(hasil);
            muatData();
            ToasterPopOut.sukses(this, "Yeay! Data " +
                    hasil.getNama() + " diperbarui!", "perubahan tersimpan");
        } catch (SQLException e) {
            ToasterPopOut.error(this, "Meow? Gagal memperbarui!",
                    e.getMessage());
        }
    }

    private void hapus() {
        Hewan dipilih = hewanDipilih(); // baris yang dipilih di JTable
        if (dipilih == null) {
            return;     // jika belum ada baris terpilih
        }
        boolean yakin = KonfirmasiDialog.tanya(jendela(),
                "Hapus Data",
                "Yakin mau hapus " + dipilih.getNama() + "?",
                "data &amp; riwayat layanannya nggak bisa kembali lho\u2026");
        if (!yakin) {
            return;         // user memilih Batal
        }
        try {
            dao.hapus(dipilih.getId());  // DELETE: hapus dari database
            muatData();
            ToasterPopOut.sukses(this, dipilih.getNama() +
                    " sudah dihapus",
                    "data hewan dan layanannya terhapus");
        } catch (SQLException e) {
            ToasterPopOut.error(this, "Meow? Gagal menghapus!", e.getMessage());
        }
    }

    /** Hewan pada baris yang sedang dipilih; null (+ toast) kalau belum ada yang dipilih. */
    private Hewan hewanDipilih() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            ToasterPopOut.error(this, "Meow? Pilih dulu hewannya!", "klik salah satu baris di tabel");
            return null;
        }
        return daftar.get(baris);
    }
}
