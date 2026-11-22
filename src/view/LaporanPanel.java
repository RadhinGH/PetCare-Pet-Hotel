package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import dao.LayananDAO;
import model.Layanan;
import util.FormatUang;
import util.ToasterPopOut;
import view_need.OutlineButton;
import view_need.PinkTable;
import view_need.RoundedPanel;

/**
 * Halaman "Laporan Pendapatan" (khusus Admin):
 * ringkasan total pendapatan, breakdown Grooming vs Boarding,
 * kontribusi per kategori hewan, dan rincian transaksi.
 */
public class LaporanPanel extends JPanel {

    private static final String[] KOLOM = {
            "Kode", "Tanggal", "Nama", "Hewan", "Pemilik", "Layanan", "Status", "Pembayaran", "Nominal"
    };

    private static final String FILTER_LUNAS = "Lunas (Pendapatan Masuk)";
    private static final String FILTER_SEMUA = "Semua Transaksi";
    private static final String FILTER_BELUM = "Belum Bayar";

    private final LayananDAO dao = new LayananDAO();
    private final List<Layanan> semuaData = new ArrayList<>();
    private final List<Layanan> dataTampil = new ArrayList<>();
    private final Consumer<String> pesanStatus;

    private final JComboBox<String> pilihPeriode = new JComboBox<>();
    private final JComboBox<String> pilihPembayaran = new JComboBox<>(
            new String[]{FILTER_LUNAS, FILTER_SEMUA, FILTER_BELUM});
    private boolean sedangMuat;

    // 4 kartu ringkasan
    private final JLabel valTotal = new JLabel("Rp0");
    private final JLabel ketTotal = new JLabel("0 transaksi lunas");
    private final JLabel valGrooming = new JLabel("Rp0");
    private final JLabel ketGrooming = new JLabel("0 layanan lunas");
    private final JLabel valBoarding = new JLabel("Rp0");
    private final JLabel ketBoarding = new JLabel("0 layanan lunas");
    private final JLabel valBelum = new JLabel("Rp0");
    private final JLabel ketBelum = new JLabel("0 belum masuk pendapatan");

    // kartu chart per kategori hewan (batang hijau gabungan per jenis hewan)
    private final KartuKategori kartuKategori = new KartuKategori();

    // tabel rincian
    private final DefaultTableModel modelTabel = new DefaultTableModel(KOLOM, 0);
    private final PinkTable tabel = new PinkTable(modelTabel);
    private final CardLayout susunanTabel = new CardLayout();
    private final JPanel areaTabel = new JPanel(susunanTabel);
    private final JLabel lblRingkasBawah = new JLabel();

    public LaporanPanel(Consumer<String> pesanStatus) {
        this.pesanStatus = pesanStatus;

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.LATAR_KONTEN);
        setBorder(BorderFactory.createEmptyBorder(22, 26, 18, 26));

        add(bangunAtas(), BorderLayout.NORTH);
        add(bangunIsi(), BorderLayout.CENTER);
        add(bangunBawah(), BorderLayout.SOUTH);

        muatData();
    }

    // ----- membangun tampilan -----

    private JPanel bangunAtas() {
        JLabel judul = new JLabel("Laporan Pendapatan");
        judul.setFont(Theme.font(Font.BOLD, 22));
        judul.setForeground(Theme.PRIMARY_TUA);
        JLabel sub = new JLabel("Ringkasan pemasukan dari layanan Grooming & Boarding");
        sub.setFont(Theme.font(Font.PLAIN, 12));
        sub.setForeground(Theme.TEKS_MUDA);

        JPanel kiri = new JPanel(new BorderLayout());
        kiri.setOpaque(false);
        kiri.add(judul, BorderLayout.NORTH);
        kiri.add(sub, BorderLayout.SOUTH);

        OutlineButton btnRefresh = new OutlineButton("Refresh");
        btnRefresh.setPreferredSize(new Dimension(105, 36));
        btnRefresh.addActionListener(e -> muatData());

        JPanel barisJudul = new JPanel(new BorderLayout());
        barisJudul.setOpaque(false);
        barisJudul.add(kiri, BorderLayout.WEST);
        barisJudul.add(btnRefresh, BorderLayout.EAST);

        pilihPeriode.setFont(Theme.font(Font.PLAIN, 13));
        pilihPeriode.setBackground(Color.WHITE);
        pilihPeriode.setPreferredSize(new Dimension(160, 34));
        pilihPeriode.addActionListener(e -> terapkanFilter());

        pilihPembayaran.setFont(Theme.font(Font.PLAIN, 13));
        pilihPembayaran.setBackground(Color.WHITE);
        pilihPembayaran.setPreferredSize(new Dimension(200, 34));
        pilihPembayaran.addActionListener(e -> terapkanFilter());

        JPanel barisFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        barisFilter.setOpaque(false);
        barisFilter.add(labelFilter("Periode:"));
        barisFilter.add(pilihPeriode);
        barisFilter.add(labelFilter("Tampilkan:"));
        barisFilter.add(pilihPembayaran);

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
        JPanel isi = new JPanel(new BorderLayout(0, 12));
        isi.setOpaque(false);
        isi.add(bangunRingkasanAtas(), BorderLayout.NORTH);
        isi.add(bangunAreaTabel(), BorderLayout.CENTER);
        return isi;
    }

    private JPanel bangunRingkasanAtas() {
        JPanel panelKartu = new JPanel(new GridLayout(1, 4, 12, 0));
        panelKartu.setOpaque(false);
        panelKartu.add(buatKartu("PENDAPATAN MASUK", valTotal, ketTotal,
                Theme.SUKSES_MUDA, new Color(0xA9E1C5), Theme.SUKSES));
        panelKartu.add(buatKartu("GROOMING (LUNAS)", valGrooming, ketGrooming,
                new Color(0xFFF1F7), Theme.BORDER_FIELD, Theme.PRIMARY_TUA));
        panelKartu.add(buatKartu("BOARDING (LUNAS)", valBoarding, ketBoarding,
                Theme.BIRU_MUDA, new Color(0xBBD7F8), Theme.BIRU_TUA));
        panelKartu.add(buatKartu("BELUM DIBAYAR", valBelum, ketBelum,
                new Color(0xFFF5E6), new Color(0xF3D19E), new Color(0xB7791F)));

        JPanel blokAtas = new JPanel(new BorderLayout(0, 12));
        blokAtas.setOpaque(false);
        blokAtas.add(panelKartu, BorderLayout.NORTH);
        blokAtas.add(kartuKategori, BorderLayout.CENTER);
        return blokAtas;
    }

    private RoundedPanel buatKartu(String judul, JLabel lblNilai, JLabel lblKet,
                                   Color latar, Color border, Color warnaTeks) {
        JLabel lJudul = new JLabel(judul);
        lJudul.setFont(Theme.font(Font.BOLD, 10));
        lJudul.setForeground(warnaTeks);

        lblNilai.setFont(Theme.font(Font.BOLD, 18));
        lblNilai.setForeground(warnaTeks);

        lblKet.setFont(Theme.font(Font.PLAIN, 11));
        lblKet.setForeground(Theme.TEKS_TUA);

        RoundedPanel kartu = new RoundedPanel(16);
        kartu.setBackground(latar);
        kartu.setBorderColor(border);
        kartu.setLayout(new GridLayout(3, 1, 0, 3));
        kartu.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        kartu.add(lJudul);
        kartu.add(lblNilai);
        kartu.add(lblKet);
        return kartu;
    }

    private JPanel bangunAreaTabel() {
        areaTabel.setOpaque(false);
        tabel.aturLebar(80, 85, 80, 65, 110, 115, 85, 120, 95);

        JScrollPane gulir = new JScrollPane(tabel);
        gulir.setBorder(BorderFactory.createLineBorder(Theme.BORDER_FIELD));
        gulir.getViewport().setBackground(Color.WHITE);

        JPanel kosong = new JPanel(new GridBagLayout());
        kosong.setBackground(Color.WHITE);
        kosong.setBorder(BorderFactory.createLineBorder(Theme.BORDER_FIELD));
        JLabel lKosong = new JLabel("Belum ada transaksi pada filter ini");
        lKosong.setFont(Theme.font(Font.BOLD, 14));
        lKosong.setForeground(Theme.TEKS_MUDA);
        kosong.add(lKosong);

        areaTabel.add(gulir, "tabel");
        areaTabel.add(kosong, "kosong");
        return areaTabel;
    }

    private JPanel bangunBawah() {
        OutlineButton btnStruk = new OutlineButton("Lihat Struk");
        btnStruk.setPreferredSize(new Dimension(125, 36));
        btnStruk.addActionListener(e -> lihatStruk());

        lblRingkasBawah.setFont(Theme.font(Font.BOLD, 12));
        lblRingkasBawah.setForeground(Theme.TEKS_TUA);

        JPanel bawah = new JPanel(new BorderLayout());
        bawah.setOpaque(false);
        bawah.add(btnStruk, BorderLayout.WEST);
        bawah.add(lblRingkasBawah, BorderLayout.EAST);
        return bawah;
    }

    // ----- data & ringkasan -----

    public void muatData() {
        try {
            semuaData.clear();
            semuaData.addAll(dao.semuaLayanan());
            isiOpsiPeriode();
            terapkanFilter();
        } catch (SQLException e) {
            ToasterPopOut.error(this, "Meow? Database bermasalah!", e.getMessage());
        }
    }

    private void isiOpsiPeriode() {
        Object pilihanLama = pilihPeriode.getSelectedItem();
        Set<String> bulan = new TreeSet<>(Comparator.reverseOrder());
        for (Layanan l : semuaData) {
            bulan.add(bulanLaporan(l)); // yyyy-MM
        }

        sedangMuat = true;
        pilihPeriode.removeAllItems();
        pilihPeriode.addItem("Semua Waktu");
        for (String b : bulan) {
            pilihPeriode.addItem(b);
        }
        if (pilihanLama != null) {
            pilihPeriode.setSelectedItem(pilihanLama);
        }
        if (pilihPeriode.getSelectedIndex() < 0) {
            pilihPeriode.setSelectedIndex(0);
        }
        sedangMuat = false;
    }

    private void terapkanFilter() {
        if (sedangMuat) {
            return;
        }
        tampilkanRingkasan();
    }

    /** Bulan (yyyy-MM) yang dipakai laporan: bulan pembayaran kalau sudah lunas, kalau belum bulan layanan. */
    private String bulanLaporan(Layanan l) {
        String bayar = l.getTanggalBayar();
        if (l.isLunas() && bayar != null && bayar.length() >= 7) {
            return bayar.substring(0, 7);
        }
        return l.getTanggal().toString().substring(0, 7);
    }

    private boolean cocokPeriode(Layanan l, String periode) {
        if (periode == null || "Semua Waktu".equals(periode)) {
            return true;
        }
        return bulanLaporan(l).equals(periode);
    }

    private boolean cocokPembayaran(Layanan l, String filter) {
        if (FILTER_LUNAS.equals(filter)) {
            return l.isLunas();
        }
        if (FILTER_BELUM.equals(filter)) {
            return !l.isLunas();
        }
        return true; // Semua Transaksi
    }

    /**
     * Pendapatan HANYA dihitung dari layanan yang sudah LUNAS.
     * Layanan yang belum dibayar ditampilkan terpisah di kartu "Belum Dibayar".
     */
    private void tampilkanRingkasan() {
        String periode = (String) pilihPeriode.getSelectedItem();
        String filterBayar = (String) pilihPembayaran.getSelectedItem();

        double totalPendapatan = 0;
        double totalGrooming = 0;
        double totalBoarding = 0;
        double totalBelumBayar = 0;
        int jmlLunas = 0;
        int jmlGrooming = 0;
        int jmlBoarding = 0;
        int jmlBelumBayar = 0;

        RingkasanKategori katKucing = new RingkasanKategori();
        RingkasanKategori katAnjing = new RingkasanKategori();
        RingkasanKategori katReptil = new RingkasanKategori();

        dataTampil.clear();
        modelTabel.setRowCount(0);
        for (Layanan l : semuaData) {
            if (!cocokPeriode(l, periode)) {
                continue;
            }
            double biaya = l.hitungTotalBiaya();
            String jenisHewan = l.getHewan().getJenis();

            if (l.isLunas()) {
                totalPendapatan += biaya;
                jmlLunas++;

                if ("GROOMING".equals(l.getJenisLayanan())) {
                    totalGrooming += biaya;
                    jmlGrooming++;
                } else {
                    totalBoarding += biaya;
                    jmlBoarding++;
                }

                if ("Kucing".equals(jenisHewan)) {
                    katKucing.tambah(biaya);
                } else if ("Anjing".equals(jenisHewan)) {
                    katAnjing.tambah(biaya);
                } else {
                    katReptil.tambah(biaya);
                }
            } else {
                totalBelumBayar += biaya;
                jmlBelumBayar++;
            }

            if (cocokPembayaran(l, filterBayar)) {
                dataTampil.add(l);
                modelTabel.addRow(new Object[]{
                        l.getKode(),
                        l.getTanggal().format(Layanan.FORMAT_TAMPIL),
                        l.getHewan().getNama(),
                        jenisHewan,
                        l.getHewan().getNamaPemilik(),
                        l.getNamaLayanan(),
                        l.getStatus().getLabel(),
                        l.isLunas() ? "Lunas (" + l.getMetodePembayaran() + ")" : "Belum Bayar",
                        FormatUang.rupiah(biaya)
                });
            }
        }

        valTotal.setText(FormatUang.rupiah(totalPendapatan));
        ketTotal.setText(jmlLunas + " transaksi lunas");

        valGrooming.setText(FormatUang.rupiah(totalGrooming));
        ketGrooming.setText(jmlGrooming + " layanan lunas");

        valBoarding.setText(FormatUang.rupiah(totalBoarding));
        ketBoarding.setText(jmlBoarding + " layanan lunas");

        valBelum.setText(FormatUang.rupiah(totalBelumBayar));
        ketBelum.setText(jmlBelumBayar + " belum masuk pendapatan");

        kartuKategori.perbarui(katKucing, katAnjing, katReptil, totalPendapatan);

        susunanTabel.show(areaTabel, dataTampil.isEmpty() ? "kosong" : "tabel");

        lblRingkasBawah.setText("Menampilkan " + dataTampil.size() + " transaksi");
        pesanStatus.accept("Laporan: pendapatan lunas " + FormatUang.rupiah(totalPendapatan)
                + " \u2022 belum dibayar " + FormatUang.rupiah(totalBelumBayar));
    }

    private void lihatStruk() {
        int baris = tabel.getSelectedRow();
        if (baris < 0 || baris >= dataTampil.size()) {
            ToasterPopOut.error(this, "Meow? Pilih dulu transaksinya!", "klik salah satu baris di tabel");
            return;
        }
        new TagihanDialog(SwingUtilities.getWindowAncestor(this), dataTampil.get(baris), this::muatData)
                .setVisible(true);
    }

    /** Menampung jumlah dan total pendapatan lunas untuk satu kategori hewan. */
    static class RingkasanKategori {
        int jumlah;
        double total;

        void tambah(double biaya) {
            jumlah++;
            total += biaya;
        }
    }

    /**
     * Kartu chart "Pendapatan Lunas per Kategori Hewan".
     * Setiap jenis hewan tampil satu bar hijau gabungan + nominal pendapatan dan persentase di kanan.
     */
    static class KartuKategori extends RoundedPanel {
        private final BarKategori barKucing = new BarKategori();
        private final BarKategori barAnjing = new BarKategori();
        private final BarKategori barReptil = new BarKategori();

        private final JLabel infoKucing = new JLabel("Rp0 (0%)", SwingConstants.RIGHT);
        private final JLabel infoAnjing = new JLabel("Rp0 (0%)", SwingConstants.RIGHT);
        private final JLabel infoReptil = new JLabel("Rp0 (0%)", SwingConstants.RIGHT);

        KartuKategori() {
            super(16);
            setBackground(Color.WHITE);
            setBorderColor(Theme.BORDER_FIELD);
            setLayout(new BorderLayout(0, 10));
            setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

            JLabel judul = new JLabel("Pendapatan Lunas per Kategori Hewan");
            judul.setFont(Theme.font(Font.BOLD, 13));
            judul.setForeground(Theme.TEKS_TUA);
            add(judul, BorderLayout.NORTH);

            JPanel daftarBar = new JPanel(new GridLayout(3, 1, 0, 8));
            daftarBar.setOpaque(false);
            daftarBar.add(buatBaris("Kucing", barKucing, infoKucing));
            daftarBar.add(buatBaris("Anjing", barAnjing, infoAnjing));
            daftarBar.add(buatBaris("Reptil", barReptil, infoReptil));
            add(daftarBar, BorderLayout.CENTER);
        }

        private JPanel buatBaris(String nama, BarKategori bar, JLabel info) {
            JLabel lNama = new JLabel(nama);
            lNama.setFont(Theme.font(Font.BOLD, 12));
            lNama.setForeground(Theme.TEKS_TUA);
            lNama.setPreferredSize(new Dimension(58, 18));

            info.setFont(Theme.font(Font.BOLD, 12));
            info.setForeground(Theme.TEKS_TUA);
            info.setPreferredSize(new Dimension(175, 18));

            JPanel baris = new JPanel(new BorderLayout(12, 0));
            baris.setOpaque(false);
            baris.add(lNama, BorderLayout.WEST);
            baris.add(bar, BorderLayout.CENTER);
            baris.add(info, BorderLayout.EAST);
            return baris;
        }

        void perbarui(RingkasanKategori kucing, RingkasanKategori anjing,
                      RingkasanKategori reptil, double totalPendapatan) {
            int[] persen = hitungPersenPas100(
                    new double[]{kucing.total, anjing.total, reptil.total}, totalPendapatan);

            perbaruiSatu(barKucing, infoKucing, "Kucing", kucing, persen[0], totalPendapatan);
            perbaruiSatu(barAnjing, infoAnjing, "Anjing", anjing, persen[1], totalPendapatan);
            perbaruiSatu(barReptil, infoReptil, "Reptil", reptil, persen[2], totalPendapatan);
        }

        private void perbaruiSatu(BarKategori bar, JLabel info, String nama,
                                 RingkasanKategori kat, int persen, double totalSemua) {
            bar.setPersen(totalSemua > 0 ? kat.total / totalSemua : 0);
            info.setText(FormatUang.rupiah(kat.total) + " (" + persen + "%)");
            bar.setToolTipText(nama + ": " + kat.jumlah + " layanan lunas \u2022 "
                    + FormatUang.rupiah(kat.total) + " (" + persen + "%)");
        }

        /** Membulatkan persentase tiap kategori agar jumlah totalnya selalu tepat 100%. */
        private static int[] hitungPersenPas100(double[] nilai, double total) {
            int[] hasil = new int[nilai.length];
            if (total <= 0) {
                return hasil;
            }
            double[] sisa = new double[nilai.length];
            int jumlah = 0;
            for (int i = 0; i < nilai.length; i++) {
                double tepat = nilai[i] * 100.0 / total;
                hasil[i] = (int) Math.floor(tepat);
                sisa[i] = tepat - hasil[i];
                jumlah += hasil[i];
            }
            int kurang = 100 - jumlah;
            for (int k = 0; k < kurang; k++) {
                int terbaik = -1;
                for (int i = 0; i < nilai.length; i++) {
                    if (nilai[i] > 0 && (terbaik < 0 || sisa[i] > sisa[terbaik])) {
                        terbaik = i;
                    }
                }
                if (terbaik >= 0) {
                    hasil[terbaik]++;
                    sisa[terbaik] = -1;
                }
            }
            return hasil;
        }
    }

    /** Batang persentase sederhana berwarna hijau dengan track abu-abu netral. */
    private static class BarKategori extends JPanel {
        private double persen;

        BarKategori() {
            setOpaque(false);
            setPreferredSize(new Dimension(120, 14));
        }

        void setPersen(double persen) {
            this.persen = Math.max(0, Math.min(1, persen));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = 12;
            int y = (getHeight() - h) / 2;

            g2.setColor(new Color(0xECEFF3));
            g2.fillRoundRect(0, y, w, h, h, h);

            if (persen > 0) {
                int lebarIsi = Math.min(w, Math.max(h, (int) Math.round(w * persen)));
                g2.setColor(Theme.SUKSES);
                g2.fillRoundRect(0, y, lebarIsi, h, h, h);
            }
            g2.dispose();
        }
    }
}
