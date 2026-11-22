package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import model.Layanan;
import util.StrukGenerator;
import util.ToasterPopOut;
import view_need.OutlineButton;
import view_need.PillButton;
import view_need.PinkDialog;

/**
 * Dialog pratinjau struk visual dengan tombol unduh sebagai gambar PNG.
 */
public class StrukPreviewDialog extends PinkDialog {

    private final Layanan layanan;
    private final BufferedImage gambarStruk;

    public StrukPreviewDialog(Window pemilik, Layanan layanan) {
        super(pemilik, "Struk Visual \u2014 " + layanan.getKode(), 548, 660);
        this.layanan = layanan;
        this.gambarStruk = StrukGenerator.buatGambarStruk(layanan);

        JLabel pratinjau = new JLabel(new ImageIcon(gambarStruk), SwingConstants.CENTER);
        pratinjau.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane gulir = new JScrollPane(pratinjau);
        gulir.setBorder(BorderFactory.createEmptyBorder());
        gulir.getViewport().setBackground(Color.WHITE);
        gulir.getVerticalScrollBar().setUnitIncrement(14);

        getIsi().add(gulir, BorderLayout.CENTER);
        getIsi().add(bangunTombol(), BorderLayout.SOUTH);
    }

    private JPanel bangunTombol() {
        OutlineButton btnTutup = new OutlineButton("Tutup");
        btnTutup.setPreferredSize(new Dimension(95, 38));
        btnTutup.addActionListener(e -> dispose());

        PillButton btnDownload = new PillButton("Download Struk (.png)");
        btnDownload.setFont(Theme.font(Font.BOLD, 13));
        btnDownload.setPreferredSize(new Dimension(190, 38));
        btnDownload.addActionListener(e -> downloadPng());

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        tombol.setOpaque(false);
        tombol.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 16));
        tombol.add(btnTutup);
        tombol.add(btnDownload);
        return tombol;
    }

    private void downloadPng() {
        JFileChooser pemilih = new JFileChooser();
        pemilih.setDialogTitle("Simpan Struk Visual");
        pemilih.setFileFilter(new FileNameExtensionFilter("Gambar PNG (*.png)", "png"));

        Path folderDownloads = Paths.get(System.getProperty("user.home"), "Downloads");
        File folderAwal = Files.isDirectory(folderDownloads)
                ? folderDownloads.toFile()
                : new File(System.getProperty("user.home"));
        pemilih.setCurrentDirectory(folderAwal);
        pemilih.setSelectedFile(new File(folderAwal, "Struk-" + layanan.getKode() + ".png"));

        if (pemilih.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = pemilih.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".png")) {
            file = new File(file.getParentFile(), file.getName() + ".png");
        }

        try {
            ImageIO.write(gambarStruk, "png", file);
            ToasterPopOut.sukses(this, "Struk berhasil diunduh!", file.getName());
        } catch (IOException e) {
            ToasterPopOut.error(this, "Meow? Gagal mengunduh struk!", e.getMessage());
        }
    }
}
