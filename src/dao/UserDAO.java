package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import model.User;
import util.DatabaseConnection;
import util.HashUtil;

public class UserDAO {

    public User login(String username, String password) {
        String sql = "SELECT id, username, nama_lengkap, peran, password_hash "
                + "FROM pengguna WHERE username = ?";

        try (Connection con = DatabaseConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashDb = rs.getString("password_hash");
                    String hashInput = HashUtil.sha256(password);

                    if (hashDb != null && hashDb.trim().equalsIgnoreCase(hashInput)) {
                        String peran = rs.getString("peran");
                        return new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("nama_lengkap"),
                                peran == null ? "pelanggan" : peran);
                    }
                }
            }
        } catch (SQLException e) {
            // kemungkinan besar MySQL belum jalan / driver belum ada di classpath
            System.err.println("[PenggunaDAO] Gagal login: " + e.getMessage());
        }
        return null;
    }

    /** Cek apakah username sudah dipakai akun lain (tidak membedakan huruf besar/kecil). */
    public boolean usernameSudahAda(String username) throws SQLException {
        String sql = "SELECT 1 FROM pengguna WHERE LOWER(username) = LOWER(?)";
        try (Connection con = DatabaseConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Mendaftarkan akun baru dengan peran pelanggan. */
    public User daftarPelanggan(String username, String namaLengkap, String password) throws SQLException {
        String sql = "INSERT INTO pengguna (username, password_hash, nama_lengkap, peran) "
                + "VALUES (?, ?, ?, 'pelanggan')";
        String userBersih = username.trim();
        String namaBersih = namaLengkap.trim();

        try (Connection con = DatabaseConnection.get();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, userBersih);
            ps.setString(2, HashUtil.sha256(password));
            ps.setString(3, namaBersih);
            ps.executeUpdate();

            try (ResultSet kunci = ps.getGeneratedKeys()) {
                int id = kunci.next() ? kunci.getInt(1) : 0;
                return new User(id, userBersih, namaBersih, "pelanggan");
            }
        }
    }
}
