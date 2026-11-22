package view_need;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import view.Theme;

/** Fungsi kecil untuk menyusun form "label di atas, isian di bawah". */
public final class FormBantu {

    private FormBantu() { }

    /** Menambah satu baris (label kecil + komponen isian) ke panel ber-GridBagLayout. */
    public static void tambahBaris(JPanel form, int baris, String label, JComponent isian) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;

        if (label != null) {
            JLabel l = new JLabel(label);
            l.setFont(Theme.font(Font.BOLD, 11));
            l.setForeground(Theme.TEKS_MUDA);
            g.gridy = baris * 2;
            g.insets = new Insets(baris == 0 ? 0 : 10, 0, 3, 0);
            form.add(l, g);
        }

        g.gridy = baris * 2 + 1;
        g.insets = new Insets(label == null ? 10 : 0, 0, 0, 0);
        if (isian instanceof RoundedTextField) {
            isian.setPreferredSize(new Dimension(10, 38));
        }
        form.add(isian, g);
    }
}
