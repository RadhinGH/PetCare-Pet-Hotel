package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.text.Normalizer;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import model.Anjing;
import model.DataTidakValidException;
import model.Hewan;
import model.Kucing;
import model.Reptil;
import model.User;
import util.ToasterPopOut;
import view_need.FormBantu;
import view_need.OutlineButton;
import view_need.PillButton;
import view_need.RoundedTextField;

/**
 * Form untuk menambah / mengubah data hewan.
 * Kalau parameter "hewanLama" null -> mode TAMBAH, kalau ada isinya -> mode UBAH.
 * Hasilnya diambil lewat getHasil() (null kalau user membatalkan).
 */
public class HewanFormDialog extends view_need.PinkDialog {

    private final Hewan hewanLama;
    private final User user;
    private Hewan hasil;

    private final RoundedTextField kotakNama = new RoundedTextField();
    private final JComboBox<String> pilihJenis = new JComboBox<>(new String[]{"Kucing", "Anjing", "Reptil"});
    private final RoundedTextField kotakRas = new RoundedTextField();
    private final JCheckBox centangSteril = new JCheckBox("Sudah steril");
    private final JComboBox<String> pilihUkuran = new JComboBox<>(Anjing.DAFTAR_UKURAN);
    private final RoundedTextField kotakSuhu = new RoundedTextField();
    private final RoundedTextField kotakUmur = new RoundedTextField();
    private final RoundedTextField kotakBerat = new RoundedTextField();
    private final RoundedTextField kotakPemilik = new RoundedTextField();
    private final RoundedTextField kotakTelepon = new RoundedTextField();

    private final CardLayout susunanKhusus = new CardLayout();
    private final JPanel panelKhusus = new JPanel(susunanKhusus);

    public HewanFormDialog(Window pemilik, Hewan hewanLama) {
        this(pemilik, hewanLama, null);
    }

    public HewanFormDialog(Window pemilik, Hewan hewanLama, User user) {
        super(pemilik, hewanLama == null ? "Daftarkan Peliharaan" : "Ubah Data Hewan", 460, 600);
        this.hewanLama = hewanLama;
        this.user = user;

        getIsi().add(bangunForm(), BorderLayout.NORTH);
        getIsi().add(bangunTombol(), BorderLayout.SOUTH);

        pilihJenis.addActionListener(e -> tampilkanIsianKhusus());
        if (hewanLama != null) {
            isiDariHewan(hewanLama);
        }
        if (user != null && user.isPelanggan()) {
            if (hewanLama == null) {
                kotakPemilik.setText(user.getNamaLengkap());
            }
            kotakPemilik.setEditable(false);
            kotakPemilik.setToolTipText("Otomatis sesuai akun pelanggan");
        }
        tampilkanIsianKhusus();
    }

    public Hewan getHasil() {
        return hasil;
    }

    // ----- membangun tampilan -----

    private JPanel bangunForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(18, 26, 6, 26));

        pilihJenis.setFont(Theme.font(Font.PLAIN, 13));
        pilihUkuran.setFont(Theme.font(Font.PLAIN, 13));
        centangSteril.setOpaque(false);
        centangSteril.setFont(Theme.font(Font.PLAIN, 13));
        centangSteril.setForeground(Theme.TEKS_TUA);

        // tiga "kartu" isian khusus; yang tampil sesuai jenis hewan
        panelKhusus.setOpaque(false);
        panelKhusus.add(susun("RAS KUCING", kotakRas, centangSteril), "Kucing");
        panelKhusus.add(susun("UKURAN ANJING", pilihUkuran, null), "Anjing");
        panelKhusus.add(susun("SUHU KANDANG (\u00B0C)", kotakSuhu, null), "Reptil");
        pilihJenis.setBackground(java.awt.Color.WHITE);
        pilihUkuran.setBackground(java.awt.Color.WHITE);

        FormBantu.tambahBaris(form, 0, "NAMA HEWAN", kotakNama);
        FormBantu.tambahBaris(form, 1, "JENIS HEWAN", pilihJenis);
        FormBantu.tambahBaris(form, 2, null, panelKhusus);
        FormBantu.tambahBaris(form, 3, "UMUR (TAHUN)", kotakUmur);
        FormBantu.tambahBaris(form, 4, "BERAT (KG)", kotakBerat);
        FormBantu.tambahBaris(form, 5, "NAMA PEMILIK", kotakPemilik);
        FormBantu.tambahBaris(form, 6, "TELEPON PEMILIK", kotakTelepon);

        return form;
    }

    /** Satu kartu isian khusus: label kecil, lalu komponen utama (+ komponen tambahan di kanannya). */
    private JPanel susun(String judul, javax.swing.JComponent utama, javax.swing.JComponent tambahan) {
        JLabel label = new JLabel(judul);
        label.setFont(Theme.font(Font.BOLD, 11));
        label.setForeground(Theme.TEKS_MUDA);

        JPanel baris = new JPanel(new BorderLayout(10, 0));
        baris.setOpaque(false);
        baris.add(utama, BorderLayout.CENTER);
        if (tambahan != null) {
            baris.add(tambahan, BorderLayout.EAST);
        }

        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        p.add(label, BorderLayout.NORTH);
        p.add(baris, BorderLayout.CENTER);
        utama.setPreferredSize(new Dimension(10, 38));
        p.setPreferredSize(new Dimension(10, 58));
        return p;
    }

    private JPanel bangunTombol() {
        OutlineButton btnBatal = new OutlineButton("Batal");
        btnBatal.setPreferredSize(new Dimension(90, 38));
        btnBatal.addActionListener(e -> dispose());

        PillButton btnSimpan = new PillButton("Simpan");
        btnSimpan.setFont(Theme.font(Font.BOLD, 14));
        btnSimpan.setPreferredSize(new Dimension(130, 38));
        btnSimpan.addActionListener(e -> simpan());

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        tombol.setOpaque(false);
        tombol.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 16));
        tombol.add(btnBatal);
        tombol.add(btnSimpan);
        return tombol;
    }

    // ----- logika -----

    private void tampilkanIsianKhusus() {
        susunanKhusus.show(panelKhusus, (String) pilihJenis.getSelectedItem());
    }

    private void isiDariHewan(Hewan h) {
        kotakNama.setText(h.getNama());
        pilihJenis.setSelectedItem(h.getJenis());
        pilihJenis.setEnabled(false); // jenis tidak boleh diganti saat mengubah data
        kotakUmur.setText(String.valueOf(h.getUmur()));
        kotakBerat.setText(String.valueOf(h.getBerat()));
        kotakPemilik.setText(h.getNamaPemilik());
        kotakTelepon.setText(h.getTeleponPemilik());

        if (h instanceof Kucing) {
            kotakRas.setText(((Kucing) h).getRas());
            centangSteril.setSelected(((Kucing) h).isSteril());
        } else if (h instanceof Anjing) {
            pilihUkuran.setSelectedItem(((Anjing) h).getUkuran());
        } else if (h instanceof Reptil) {
            kotakSuhu.setText(String.valueOf(((Reptil) h).getSuhuKandang()));
        }
    }

    private void simpan() {
        try {
            hasil = bangunHewan();
            dispose();
        } catch (DataTidakValidException e) {
            ToasterPopOut.error(this, "Meow? Ada yang belum benar!", e.getMessage());
        } catch (NumberFormatException e) {
            ToasterPopOut.error(this, "Meow? Isi angka dengan benar!",
                    "umur / berat / suhu harus berupa angka");
        }
    }

    /** Membuat objek Kucing/Anjing/Reptil dari isi form. Melempar exception jika tidak valid. */
    private Hewan bangunHewan() throws DataTidakValidException {
        int id = hewanLama == null ? 0 : hewanLama.getId();
        String nama = kotakNama.getText();
        int umur = Integer.parseInt(kotakUmur.getText().trim());
        double berat = Double.parseDouble(kotakBerat.getText().trim().replace(',', '.'));
        String pemilik = kotakPemilik.getText();
        String telepon = kotakTelepon.getText();

        Hewan hasilHewan;
        switch ((String) pilihJenis.getSelectedItem()) {
            case "Kucing":
                hasilHewan = new Kucing(id, nama, umur, berat, pemilik, telepon,
                        kotakRas.getText(), centangSteril.isSelected());
                break;
            case "Anjing":
                hasilHewan = new Anjing(id, nama, umur, berat, pemilik, telepon,
                        (String) pilihUkuran.getSelectedItem());
                break;
            default:
                double suhu = Double.parseDouble(kotakSuhu.getText().trim().replace(',', '.'));
                hasilHewan = new Reptil(id, nama, umur, berat, pemilik, telepon, suhu);
                break;
        }
        if (user != null && user.isPelanggan()) {
            hasilHewan.setPemilikUserId(user.getId());
        } else if (hewanLama != null) {
            hasilHewan.setPemilikUserId(hewanLama.getPemilikUserId());
        }
        return hasilHewan;
    }
}
