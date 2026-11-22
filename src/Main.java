import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import util.DatabaseConnection;
import view.LoginFrame;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        // siapkan database (dibuat otomatis kalau belum ada)
        try {
            DatabaseConnection.inisialisasi();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Database tidak bisa disiapkan:\n" + e.getMessage()
                    + "\n\nPastikan sqlite-jdbc sudah ditambahkan sebagai library.",
                    "PetCare", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
