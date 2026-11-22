package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import dao.UserDAO;
import model.User;
import util.ToasterPopOut;
import view_need.FormBantu;
import view_need.OutlineButton;
import view_need.PillButton;
import view_need.PinkDialog;
import view_need.RoundedPasswordField;
import view_need.RoundedTextField;

/** Form pendaftaran akun baru dengan peran pelanggan. */
public class RegisterDialog extends PinkDialog {

    private final RoundedTextField kotakUsername = new RoundedTextField();
    private final RoundedTextField kotakNama = new RoundedTextField();
    private final RoundedPasswordField kotakPassword = new RoundedPasswordField();
    private final RoundedPasswordField kotakKonfirmasi = new RoundedPasswordField();

    private User hasil;

    public RegisterDialog(Window pemilik) {
        super(pemilik, "Daftar Akun Pelanggan", 420, 440);

        getIsi().add(bangunForm(), BorderLayout.NORTH);
        getIsi().add(bangunTombol(), BorderLayout.SOUTH);
    }

    public User getHasil() {
        return hasil;
    }

    private JPanel bangunForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(18, 26, 8, 26));

        kotakPassword.setPreferredSize(new Dimension(10, 38));
        kotakKonfirmasi.setPreferredSize(new Dimension(10, 38));

        FormBantu.tambahBaris(form, 0, "USERNAME", kotakUsername);
        FormBantu.tambahBaris(form, 1, "NAMA LENGKAP", kotakNama);
        FormBantu.tambahBaris(form, 2, "PASSWORD", kotakPassword);
        FormBantu.tambahBaris(form, 3, "KONFIRMASI PASSWORD", kotakKonfirmasi);
        return form;
    }

    private JPanel bangunTombol() {
        OutlineButton btnBatal = new OutlineButton("Batal");
        btnBatal.setPreferredSize(new Dimension(90, 38));
        btnBatal.addActionListener(e -> dispose());

        PillButton btnDaftar = new PillButton("Daftar");
        btnDaftar.setFont(Theme.font(Font.BOLD, 14));
        btnDaftar.setPreferredSize(new Dimension(130, 38));
        btnDaftar.addActionListener(e -> daftar());

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        tombol.setOpaque(false);
        tombol.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 16));
        tombol.add(btnBatal);
        tombol.add(btnDaftar);
        return tombol;
    }

    private void daftar() {
        String username = kotakUsername.getText().trim();
        String nama = kotakNama.getText().trim();
        String password = new String(kotakPassword.getPassword());
        String konfirmasi = new String(kotakKonfirmasi.getPassword());

        if (username.isEmpty() || nama.isEmpty() || password.isEmpty()) {
            ToasterPopOut.error(this, "Meow? Data belum lengkap!", "isi semua kolom terlebih dahulu");
            return;
        }
        if (username.length() < 3 || username.contains(" ")) {
            ToasterPopOut.error(this, "Username tidak valid!", "minimal 3 karakter tanpa spasi");
            return;
        }
        if (password.length() < 4) {
            ToasterPopOut.error(this, "Password terlalu pendek!", "minimal 4 karakter ya");
            return;
        }
        if (!password.equals(konfirmasi)) {
            ToasterPopOut.error(this, "Password tidak sama!", "periksa ulang konfirmasi password");
            return;
        }

        UserDAO dao = new UserDAO();
        try {
            if (dao.usernameSudahAda(username)) {
                ToasterPopOut.error(this, "Username sudah dipakai!", "coba pilih username lain");
                return;
            }
            hasil = dao.daftarPelanggan(username, nama, password);
            dispose();
        } catch (SQLException e) {
            ToasterPopOut.error(this, "Meow? Gagal mendaftar!", e.getMessage());
        }
    }
}
