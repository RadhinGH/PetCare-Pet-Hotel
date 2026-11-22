package view_need;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import view.Theme;

/**
 * Dasar semua dialog PetCare: jendela modal tanpa bingkai bawaan,
 * dengan header pink bergradasi dan tombol tutup (x).
 * Subclass cukup mengisi getIsi() dengan komponennya.
 */
public class PinkDialog extends JDialog {

    private final JPanel isi = new JPanel(new BorderLayout());

    public PinkDialog(Window pemilik, String judul, int lebar, int tinggi) {
        super(pemilik, judul, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setSize(lebar, tinggi);
        setLocationRelativeTo(pemilik);

        JPanel rangka = new JPanel(new BorderLayout());
        rangka.setBorder(BorderFactory.createLineBorder(Theme.PRIMARY_MUDA, 2));
        rangka.add(bangunHeader(judul), BorderLayout.NORTH);

        isi.setBackground(Color.WHITE);
        rangka.add(isi, BorderLayout.CENTER);
        setContentPane(rangka);
    }

    /** Panel putih tempat subclass menaruh isi dialog. */
    protected JPanel getIsi() {
        return isi;
    }

    private JPanel bangunHeader(String judul) {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY_MUDA, getWidth(), 0, Theme.PRIMARY));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 12));
        header.setPreferredSize(new java.awt.Dimension(10, 40));

        JLabel lJudul = new JLabel(judul);
        lJudul.setFont(Theme.font(Font.BOLD, 14));
        lJudul.setForeground(Color.WHITE);
        header.add(lJudul, BorderLayout.CENTER);

        JLabel tutup = new JLabel("\u2715", SwingConstants.CENTER);
        tutup.setFont(Theme.font(Font.BOLD, 14));
        tutup.setForeground(Color.WHITE);
        tutup.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tutup.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
        });
        header.add(tutup, BorderLayout.EAST);
        return header;
    }
}
