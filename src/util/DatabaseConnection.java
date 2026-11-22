package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Pintu masuk ke database SQLite.
 * Database berupa SATU file (pethotel.db) di folder proyek, tanpa server.
 * Saat pertama dijalankan, file itu dibuat otomatis dari db/pethotel.sql.
 */
public final class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:pethotel.db";

    private DatabaseConnection() { }

    /** Membuka koneksi baru. Selalu dipakai dengan try-with-resources. */
    public static Connection get() throws SQLException {
        Connection con = DriverManager.getConnection(URL);
        try (Statement st = con.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON"); // supaya ON DELETE CASCADE aktif
        }
        return con;
    }

    /** Dipanggil sekali di Main: membuat tabel + data awal jika belum ada. */
    public static void inisialisasi() throws SQLException {
        try (Connection con = get()) {
            if (!tabelSudahAda(con)) {
                for (String perintah : bacaSkrip()) {
                    if (perintah.trim().isEmpty()) {
                        continue; // sisa teks kosong setelah ';' terakhir
                    }
                    try (Statement st = con.createStatement()) {
                        st.execute(perintah);
                    }
                }
            }
            migrasiJikaPerlu(con);
        }
    }

    /** Menambahkan kolom dan akun pelanggan demo pada database lama tanpa menghapus data. */
    private static void migrasiJikaPerlu(Connection con) throws SQLException {
        if (!kolomSudahAda(con, "hewan", "pemilik_user_id")) {
            try (Statement st = con.createStatement()) {
                st.execute("ALTER TABLE hewan ADD COLUMN pemilik_user_id INTEGER");
            }
        }
        // kolom pembayaran pada tabel layanan (layanan lama otomatis berstatus BELUM_BAYAR)
        tambahKolomJikaBelumAda(con, "layanan", "status_pembayaran",
                "TEXT NOT NULL DEFAULT 'BELUM_BAYAR'");
        tambahKolomJikaBelumAda(con, "layanan", "metode_pembayaran", "TEXT");
        tambahKolomJikaBelumAda(con, "layanan", "jumlah_dibayar", "REAL");
        tambahKolomJikaBelumAda(con, "layanan", "tanggal_bayar", "TEXT");

        try (Statement st = con.createStatement()) {
            st.execute("CREATE INDEX IF NOT EXISTS idx_hewan_jenis ON hewan(jenis)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_hewan_pemilik_user ON hewan(pemilik_user_id)");
        }
        pastikanAkunPelanggan(con, "pelanggan1", "pelanggan123", "Cici Wulandari");
        pastikanAkunPelanggan(con, "pelanggan2", "pelanggan123", "Dian Puspita");
    }

    private static void tambahKolomJikaBelumAda(Connection con, String tabel, String kolom,
                                                String tipe) throws SQLException {
        if (!kolomSudahAda(con, tabel, kolom)) {
            try (Statement st = con.createStatement()) {
                st.execute("ALTER TABLE " + tabel + " ADD COLUMN " + kolom + " " + tipe);
            }
        }
    }

    private static boolean kolomSudahAda(Connection con, String tabel, String kolom) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info(" + tabel + ")")) {
            while (rs.next()) {
                if (kolom.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void pastikanAkunPelanggan(Connection con, String username, String password,
                                              String namaLengkap) throws SQLException {
        Integer idPelanggan = null;
        try (PreparedStatement ps = con.prepareStatement("SELECT id FROM pengguna WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    idPelanggan = rs.getInt("id");
                }
            }
        }

        if (idPelanggan == null) {
            String sqlInsert = "INSERT INTO pengguna (username, password_hash, nama_lengkap, peran) "
                    + "VALUES (?, ?, ?, 'pelanggan')";
            try (PreparedStatement ps = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, HashUtil.sha256(password));
                ps.setString(3, namaLengkap);
                ps.executeUpdate();
                try (ResultSet kunci = ps.getGeneratedKeys()) {
                    if (kunci.next()) {
                        idPelanggan = kunci.getInt(1);
                    }
                }
            }
        }

        if (idPelanggan != null) {
            String sqlKait = "UPDATE hewan SET pemilik_user_id = ? "
                    + "WHERE nama_pemilik = ? AND pemilik_user_id IS NULL";
            try (PreparedStatement ps = con.prepareStatement(sqlKait)) {
                ps.setInt(1, idPelanggan);
                ps.setString(2, namaLengkap);
                ps.executeUpdate();
            }
        }
    }

    private static boolean tabelSudahAda(Connection con) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='pengguna'";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next();
        }
    }

    /** Membaca db/pethotel.sql, membuang komentar, lalu memecah per ';'. */
    private static String[] bacaSkrip() throws SQLException {
        InputStream in = DatabaseConnection.class.getResourceAsStream("/db/pethotel.sql");
        if (in == null) {
            throw new SQLException("File db/pethotel.sql tidak ditemukan di classpath");
        }
        StringBuilder isi = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String baris;
            while ((baris = br.readLine()) != null) {
                int komentar = baris.indexOf("--");
                if (komentar >= 0) {
                    baris = baris.substring(0, komentar);
                }
                isi.append(baris).append('\n');
            }
        } catch (IOException e) {
            throw new SQLException("Gagal membaca pethotel.sql", e);
        }
        return isi.toString().split(";");
    }
}
