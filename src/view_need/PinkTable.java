package view_need;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import view.Theme;

/** JTable bergaya pink: header berwarna, baris selang-seling, tidak bisa diedit. */
public class PinkTable extends JTable {

    public PinkTable(DefaultTableModel model) {
        super(model);
        setRowHeight(36);
        setFont(Theme.font(Font.PLAIN, 13));
        setForeground(Theme.TEKS_TUA);
        setShowGrid(false);
        setIntercellSpacing(new java.awt.Dimension(0, 0));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setSelectionBackground(Theme.BARIS_DIPILIH);
        setSelectionForeground(Theme.TEKS_TUA);
        setFillsViewportHeight(true);

        // teks sel: beri jarak kiri-kanan
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object nilai, boolean dipilih,
                                                           boolean fokus, int baris, int kolom) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, nilai, dipilih, false, baris, kolom);
                l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!dipilih) {
                    l.setBackground(baris % 2 == 0 ? Color.WHITE : Theme.BARIS_GANJIL);
                }
                return l;
            }
        });

        JTableHeader header = getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new java.awt.Dimension(10, 38));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object nilai, boolean dipilih,
                                                           boolean fokus, int baris, int kolom) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, nilai, false, false, baris, kolom);
                l.setBackground(Theme.PRIMARY);
                l.setForeground(Color.WHITE);
                l.setFont(Theme.font(Font.BOLD, 12));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                l.setOpaque(true);
                return l;
            }
        });
    }

    /** Mengatur lebar tiap kolom (urutan sama dengan kolom tabel). */
    public void aturLebar(int... lebar) {
        for (int i = 0; i < lebar.length; i++) {
            getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
    }

    @Override
    public boolean isCellEditable(int baris, int kolom) {
        return false;
    }
}
