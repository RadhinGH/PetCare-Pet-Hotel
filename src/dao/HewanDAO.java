package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.Anjing;
import model.DataTidakValidException;
import model.Hewan;
import model.Kucing;
import model.Reptil;
import util.DatabaseConnection;

/** Semua perintah SQL untuk tabel hewan (CRUD). */
public class HewanDAO {

    private static final String KOLOM_INSERT =
            "INSERT INTO hewan (nama, jenis, ras, steril, ukuran, suhu_kandang, "
            + "umur, berat, nama_pemilik, telepon_pemilik, pemilik_user_id) VALUES (?,?,?,?,?,?,?,?,?,?,?)";

    private static final String SQL_UPDATE =
            "UPDATE hewan SET nama=?, jenis=?, ras=?, steril=?, ukuran=?, suhu_kandang=?, "
            + "umur=?, berat=?, nama_pemilik=?, telepon_pemilik=?, pemilik_user_id=? WHERE id=?";

    // ===== READ =====

    public List<Hewan> semuaHewan() throws SQLException {
        return cari("", "Semua", "Nama (A-Z)", null);
    }

    public List<Hewan> semuaHewanMilik(int pemilikUserId) throws SQLException {
        return cari("", "Semua", "Nama (A-Z)", pemilikUserId);
    }

    /** Cari berdasarkan nama hewan atau nama pemilik. Kata kosong = semua. */
    public List<Hewan> cari(String kata) throws SQLException {
        return cari(kata, "Semua", "Nama (A-Z)", null);
    }

    /**
     * Cari hewan dengan filter kategori jenis, urutan, dan batas pemilik (null = semua pelanggan).
     */
    public List<Hewan> cari(String kata, String filterJenis, String urutan, Integer pemilikUserId)
            throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM hewan WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (pemilikUserId != null) {
            sql.append(" AND pemilik_user_id = ?");
            params.add(pemilikUserId);
        }

        String kataCari = kata == null ? "" : kata.trim();
        if (!kataCari.isEmpty()) {
            sql.append(" AND (nama LIKE ? OR nama_pemilik LIKE ?)");
            params.add("%" + kataCari + "%");
            params.add("%" + kataCari + "%");
        }

        if (filterJenis != null && !filterJenis.equals("Semua") && !filterJenis.equals("Semua Jenis")) {
            sql.append(" AND jenis = ?");
            params.add(filterJenis);
        }

        if ("Jenis Hewan".equals(urutan)) {
            sql.append(" ORDER BY jenis ASC, nama ASC");
        } else if ("Jenis (Z-A)".equals(urutan)) {
            sql.append(" ORDER BY jenis DESC, nama ASC");
        } else {
            sql.append(" ORDER BY nama ASC");
        }

        List<Hewan> hasil = new ArrayList<>();
        try (Connection con = DatabaseConnection.get();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(dariBaris(rs));
                }
            }
        }
        return hasil;
    }

    // ===== CREATE =====

    public void tambah(Hewan h) throws SQLException {
        try (Connection con = DatabaseConnection.get();
             PreparedStatement ps = con.prepareStatement(KOLOM_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            isiParameter(ps, h);
            ps.executeUpdate();
            try (ResultSet kunci = ps.getGeneratedKeys()) {
                if (kunci.next()) {
                    h.setId(kunci.getInt(1));
                }
            }
        }
    }

    // ===== UPDATE =====

    public void ubah(Hewan h) throws SQLException {
        try (Connection con = DatabaseConnection.get();
             PreparedStatement ps = con.prepareStatement(SQL_UPDATE)) {
            isiParameter(ps, h);
            ps.setInt(12, h.getId());
            ps.executeUpdate();
        }
    }

    // ===== DELETE =====

    /** Layanan milik hewan ini ikut terhapus (ON DELETE CASCADE). */
    public void hapus(int id) throws SQLException {
        try (Connection con = DatabaseConnection.get();
             PreparedStatement ps = con.prepareStatement("DELETE FROM hewan WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ===== Bantuan: objek <-> baris tabel =====

    /** Mengisi 10 tanda tanya pertama. Kolom milik jenis lain diisi NULL. */
    private void isiParameter(PreparedStatement ps, Hewan h) throws SQLException {
        ps.setString(1, h.getNama());
        ps.setString(2, h.getJenis());
        ps.setNull(3, Types.VARCHAR);
        ps.setNull(4, Types.INTEGER);
        ps.setNull(5, Types.VARCHAR);
        ps.setNull(6, Types.REAL);

        if (h instanceof Kucing) {
            Kucing k = (Kucing) h;
            ps.setString(3, k.getRas());
            ps.setInt(4, k.isSteril() ? 1 : 0);
        } else if (h instanceof Anjing) {
            ps.setString(5, ((Anjing) h).getUkuran());
        } else if (h instanceof Reptil) {
            ps.setDouble(6, ((Reptil) h).getSuhuKandang());
        }

        ps.setInt(7, h.getUmur());
        ps.setDouble(8, h.getBerat());
        ps.setString(9, h.getNamaPemilik());
        ps.setString(10, h.getTeleponPemilik());
        if (h.getPemilikUserId() == null) {
            ps.setNull(11, Types.INTEGER);
        } else {
            ps.setInt(11, h.getPemilikUserId());
        }
    }

    /** Membuat objek Kucing / Anjing / Reptil sesuai kolom "jenis". */
    private Hewan dariBaris(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nama = rs.getString("nama");
        int umur = rs.getInt("umur");
        double berat = rs.getDouble("berat");
        String pemilik = rs.getString("nama_pemilik");
        String telepon = rs.getString("telepon_pemilik");

        try {
            Hewan hewan;
            switch (rs.getString("jenis")) {
                case "Kucing":
                    hewan = new Kucing(id, nama, umur, berat, pemilik, telepon,
                            rs.getString("ras"), rs.getInt("steril") == 1);
                    break;
                case "Anjing":
                    hewan = new Anjing(id, nama, umur, berat, pemilik, telepon,
                            rs.getString("ukuran"));
                    break;
                default:
                    hewan = new Reptil(id, nama, umur, berat, pemilik, telepon,
                            rs.getDouble("suhu_kandang"));
                    break;
            }
            Object objPemilik = rs.getObject("pemilik_user_id");
            if (objPemilik != null) {
                hewan.setPemilikUserId(((Number) objPemilik).intValue());
            }
            return hewan;
        } catch (DataTidakValidException e) {
            throw new SQLException("Data hewan id " + id + " di database tidak valid: " + e.getMessage(), e);
        }
    }
}
