package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import model.BoardingService;
import model.DataTidakValidException;
import model.GroomingService;
import model.Hewan;
import model.Layanan;
import model.PaketGrooming;
import model.StatusPenitipan;
import util.FormatUang;
import util.ToasterPopOut;
import view_need.FormBantu;
import view_need.OutlineButton;
import view_need.PillButton;
import view_need.PinkDialog;
import view_need.RoundedTextField;

/** Form untuk membuat layanan baru (Grooming atau Boarding). Hasil lewat getHasil(). */
public class LayananFormDialog extends PinkDialog {

    private static final String GROOMING = "Grooming";
    private static final String BOARDING = "Boarding (Penitipan)";

    private Layanan hasil;

    private final JComboBox<Hewan> pilihHewan;
    private final JComboBox<String> pilihJenis = new JComboBox<>(new String[]{GROOMING, BOARDING});
    private final JComboBox<PaketGrooming> pilihPaket = new JComboBox<>(PaketGrooming.values());
    private final JSpinner spinHari = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));
    private final RoundedTextField kotakTanggal = new RoundedTextField();
    private final JLabel lblPerkiraan = new JLabel(" ");

    private final CardLayout susunanKhusus = new CardLayout();
    private final JPanel panelKhusus = new JPanel(susunanKhusus);

    public LayananFormDialog(Window pemilik, List<Hewan> daftarHewan) {
        super(pemilik, "Layanan Baru", 440, 450);
        pilihHewan = new JComboBox<>(daftarHewan.toArray(new Hewan[0]));

        getIsi().add(bangunForm(), BorderLayout.NORTH);
        getIsi().add(bangunTombol(), BorderLayout.SOUTH);

        kotakTanggal.setText(LocalDate.now().toString());

        // setiap pilihan berubah -> perkiraan biaya dihitung ulang
        pilihHewan.addActionListener(e -> perbaruiPerkiraan());
        pilihPaket.addActionListener(e -> perbaruiPerkiraan());
        spinHari.addChangeListener(e -> perbaruiPerkiraan());
        pilihJenis.addActionListener(e -> {
            susunanKhusus.show(panelKhusus, jenisKode());
            perbaruiPerkiraan();
        });
        perbaruiPerkiraan();
    }

    public Layanan getHasil() {
        return hasil;
    }

    private JPanel bangunForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(18, 26, 6, 26));

        for (javax.swing.JComponent c : new javax.swing.JComponent[]{pilihHewan, pilihJenis, pilihPaket, spinHari}) {
            c.setFont(Theme.font(Font.PLAIN, 13));
            c.setBackground(Color.WHITE);
            c.setPreferredSize(new Dimension(10, 38));
        }

        panelKhusus.setOpaque(false);
        panelKhusus.add(pilihPaket, "grooming");
        panelKhusus.add(spinHari, "boarding");
        panelKhusus.setPreferredSize(new Dimension(10, 38));

        lblPerkiraan.setFont(Theme.font(Font.BOLD, 15));
        lblPerkiraan.setForeground(Theme.SUKSES);

        FormBantu.tambahBaris(form, 0, "PILIH HEWAN", pilihHewan);
        FormBantu.tambahBaris(form, 1, "JENIS LAYANAN", pilihJenis);
        FormBantu.tambahBaris(form, 2, "PAKET GROOMING / JUMLAH HARI MENGINAP", panelKhusus);
        FormBantu.tambahBaris(form, 3, "TANGGAL (YYYY-MM-DD)", kotakTanggal);
        FormBantu.tambahBaris(form, 4, "PERKIRAAN BIAYA", lblPerkiraan);
        return form;
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

    private String jenisKode() {
        return GROOMING.equals(pilihJenis.getSelectedItem()) ? "grooming" : "boarding";
    }

    private void perbaruiPerkiraan() {
        try {
            lblPerkiraan.setForeground(Theme.SUKSES);
            lblPerkiraan.setText(FormatUang.rupiah(bangunLayanan().hitungTotalBiaya()));
        } catch (DataTidakValidException | DateTimeParseException e) {
            lblPerkiraan.setForeground(Theme.ERROR);
            lblPerkiraan.setText("-");
        }
    }

    private void simpan() {
        try {
            hasil = bangunLayanan();
            dispose();
        } catch (DataTidakValidException e) {
            ToasterPopOut.error(this, "Meow? Ada yang belum benar!", e.getMessage());
        } catch (DateTimeParseException e) {
            ToasterPopOut.error(this, "Meow? Format tanggal salah!", "tulis seperti 2026-11-20");
        }
    }

    /** Membuat GroomingService atau BoardingService dari isi form. */
    private Layanan bangunLayanan() throws DataTidakValidException {
        Hewan hewan = (Hewan) pilihHewan.getSelectedItem();
        if (hewan == null) {
            throw new DataTidakValidException("Belum ada hewan yang dipilih");
        }
        LocalDate tanggal = LocalDate.parse(kotakTanggal.getText().trim());

        if (GROOMING.equals(pilihJenis.getSelectedItem())) {
            return new GroomingService(0, "", hewan, tanggal, StatusPenitipan.CHECK_IN,
                    (PaketGrooming) pilihPaket.getSelectedItem());
        }
        return new BoardingService(0, "", hewan, tanggal, StatusPenitipan.CHECK_IN,
                (Integer) spinHari.getValue());
    }
}
