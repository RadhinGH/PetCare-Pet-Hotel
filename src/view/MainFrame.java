package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import model.User;
import util.AvaterLabel;
import util.ImageLoader;
import view_need.OutlineButton;
import view_need.PawBackgroundPanel;
import view_need.PawIcon;
import view_need.RoundedPanel;

/**
 * Jendela utama setelah login.
 * Kiri : sidebar (menu + info user).  Tengah : halaman (ditukar pakai CardLayout).
 * Bawah: status bar.
 */
public class MainFrame extends JFrame {

    private static final String HALAMAN_HEWAN = "Data Hewan";
    private static final String HALAMAN_LAYANAN = "Layanan & Penitipan";
    private static final String HALAMAN_LAPORAN = "Laporan Pendapatan";
    private static final String HALAMAN_HEWAN_PELANGGAN = "Hewan Saya";
    private static final String HALAMAN_LAYANAN_PELANGGAN = "Layanan Saya";

    private final User user;
    private final String namaMenuHewan;
    private final String namaMenuLayanan;
    private final HewanPanel hewanPanel;
    private final LayananPanel layananPanel;
    private final LaporanPanel laporanPanel;

    private final CardLayout susunanHalaman = new CardLayout();
    private final JPanel areaHalaman = new JPanel(susunanHalaman);
    private final JLabel lblStatusKiri = new JLabel("Siap digunakan");

    // nama menu -> kotak menu di sidebar (LinkedHashMap menjaga urutan)
    private final Map<String, RoundedPanel> daftarMenu = new LinkedHashMap<>();

    public MainFrame(User user) {
        this.user = user;
        this.namaMenuHewan = user.isPelanggan() ? HALAMAN_HEWAN_PELANGGAN : HALAMAN_HEWAN;
        this.namaMenuLayanan = user.isPelanggan() ? HALAMAN_LAYANAN_PELANGGAN : HALAMAN_LAYANAN;

        setTitle(user.isPelanggan()
                ? "PetCare \u2014 Portal Pelanggan"
                : "PetCare \u2014 Panel Admin");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1060, 680);
        setMinimumSize(new Dimension(940, 600));
        setLocationRelativeTo(null);

        hewanPanel = new HewanPanel(user);
        layananPanel = new LayananPanel(user, teks -> lblStatusKiri.setText(teks));
        laporanPanel = user.isAdmin() ? new LaporanPanel(teks -> lblStatusKiri.setText(teks)) : null;

        areaHalaman.add(hewanPanel, namaMenuHewan);
        areaHalaman.add(layananPanel, namaMenuLayanan);
        if (laporanPanel != null) {
            areaHalaman.add(laporanPanel, HALAMAN_LAPORAN);
        }

        add(bangunSidebar(user), BorderLayout.WEST);
        add(areaHalaman, BorderLayout.CENTER);
        add(bangunStatusBar(), BorderLayout.SOUTH);

        pilihMenu(namaMenuHewan);
    }

    // ----- sidebar -----

    private JPanel bangunSidebar(User user) {

        PawBackgroundPanel sidebar = user.isPelanggan()
                ? new PawBackgroundPanel(24, 77L, Theme.BG_PELANGGAN_ATAS, Theme.BG_PELANGGAN_BAWAH)
                : new PawBackgroundPanel(24, 77L);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(250, 10));
        sidebar.setBorder(new EmptyBorder(24, 18, 18, 18));

        // logo
        JLabel logo = new JLabel("PetCare");
        logo.setIcon(new PawIcon(26, Color.WHITE));
        logo.setIconTextGap(10);
        logo.setFont(Theme.font(Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logo);

        JLabel sub = new JLabel(user.isPelanggan() ? "PORTAL PELANGGAN" : "PET CARE & HOTEL");
        sub.setFont(Theme.font(Font.BOLD, 10));
        sub.setForeground(new Color(255, 255, 255, 210));
        sub.setBorder(new EmptyBorder(0, 36, 0, 0));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sub);
        sidebar.add(Box.createVerticalStrut(28));

        JLabel judulMenu = new JLabel("MENU");
        judulMenu.setFont(Theme.font(Font.BOLD, 35));
        judulMenu.setForeground(new Color(255, 255, 255, 210));
        judulMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(judulMenu);
        sidebar.add(Box.createVerticalStrut(8));

        String[] menuTampil = user.isPelanggan()
                ? new String[]{namaMenuHewan, namaMenuLayanan}
                : new String[]{namaMenuHewan, namaMenuLayanan, HALAMAN_LAPORAN};

        for (String nama : menuTampil) {
            RoundedPanel menu = buatItemMenu(nama);
            daftarMenu.put(nama, menu);
            sidebar.add(menu);
            sidebar.add(Box.createVerticalStrut(8));
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(bangunKartuUser(user));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(bangunTombolKeluar());
        return sidebar;
    }

    private RoundedPanel buatItemMenu(String nama) {
        JLabel teks = new JLabel(nama);
        teks.setIcon(new PawIcon(16, Theme.PRIMARY));
        teks.setIconTextGap(10);
        teks.setFont(Theme.font(Font.PLAIN, 14));

        RoundedPanel kotak = new RoundedPanel(14);
        kotak.setLayout(new BorderLayout());
        kotak.setBorder(new EmptyBorder(11, 14, 11, 14));
        kotak.add(teks, BorderLayout.CENTER);
        kotak.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        kotak.setAlignmentX(Component.LEFT_ALIGNMENT);
        kotak.setCursor(new Cursor(Cursor.HAND_CURSOR));
        kotak.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                pilihMenu(nama);
            }
        });
        return kotak;
    }

    /** Menampilkan halaman yang dipilih, menandai menunya, dan memuat ulang datanya. */
    private void pilihMenu(String nama) {
        susunanHalaman.show(areaHalaman, nama);

        for (Map.Entry<String, RoundedPanel> item : daftarMenu.entrySet()) {
            boolean aktif = item.getKey().equals(nama);
            RoundedPanel kotak = item.getValue();
            JLabel teks = (JLabel) kotak.getComponent(0);

            kotak.setBackground(aktif ? new Color(255, 255, 255, 235) : new Color(255, 255, 255, 60));
            teks.setForeground(aktif ? Theme.PRIMARY_TUA : Theme.TEKS_TUA);
            teks.setFont(Theme.font(aktif ? Font.BOLD : Font.PLAIN, 14));
            kotak.repaint();
        }

        if (nama.equals(namaMenuHewan)) {
            hewanPanel.muatData();
            lblStatusKiri.setText(user.isPelanggan()
                    ? "Menampilkan data hewan milik " + user.getNamaLengkap()
                    : "Menampilkan seluruh data hewan pelanggan");
        } else if (nama.equals(namaMenuLayanan)) {
            layananPanel.muatData();
        } else if (laporanPanel != null && nama.equals(HALAMAN_LAPORAN)) {
            laporanPanel.muatData();
        }
    }

    private RoundedPanel bangunKartuUser(User user) {
        String panggilan = user.getNamaLengkap().split("\\s+")[0];

        JLabel sapa = new JLabel("Hai, " + panggilan + "!");
        sapa.setFont(Theme.font(Font.BOLD, 14));
        sapa.setForeground(Theme.TEKS_TUA);
        JLabel peran = new JLabel(user.getPeran() + " \u2022 " + user.getUsername());
        peran.setFont(Theme.font(Font.PLAIN, 11));
        peran.setForeground(Theme.TEKS_MUDA);

        JPanel teks = new JPanel(new BorderLayout());
        teks.setOpaque(false);
        teks.add(sapa, BorderLayout.NORTH);
        teks.add(peran, BorderLayout.SOUTH);

        RoundedPanel kartu = new RoundedPanel(18);
        kartu.setBackground(Color.WHITE);
        kartu.setLayout(new BorderLayout(10, 0));
        kartu.setBorder(new EmptyBorder(10, 12, 10, 12));
        kartu.add(new AvaterLabel(ImageLoader.kucing(), 44), BorderLayout.WEST);
        kartu.add(teks, BorderLayout.CENTER);
        kartu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        kartu.setAlignmentX(Component.LEFT_ALIGNMENT);
        return kartu;
    }

    private OutlineButton bangunTombolKeluar() {
        OutlineButton btn = new OutlineButton("Keluar");
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        return btn;
    }

    // ----- status bar -----

    private JPanel bangunStatusBar() {
        lblStatusKiri.setFont(Theme.font(Font.PLAIN, 12));
        lblStatusKiri.setForeground(Theme.TEKS_TUA);

        String mode = user.isPelanggan() ? "Mode: Pelanggan" : "Mode: Admin";
        JLabel kanan = new JLabel(mode + " \u2014 SQLite pethotel.db terhubung", SwingConstants.RIGHT);
        kanan.setFont(Theme.font(Font.PLAIN, 12));
        kanan.setForeground(Theme.TEKS_MUDA);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0xFFE4EF));
        bar.setBorder(new EmptyBorder(7, 16, 7, 16));
        bar.add(lblStatusKiri, BorderLayout.WEST);
        bar.add(kanan, BorderLayout.EAST);
        return bar;
    }
}
