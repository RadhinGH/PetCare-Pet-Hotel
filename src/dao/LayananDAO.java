package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.BoardingService;
import model.DataTidakValidException;
import model.GroomingService;
import model.Hewan;
import model.Layanan;
import model.PaketGrooming;
import model.StatusPembayaran;
import model.StatusPenitipan;
import util.DatabaseConnection;

/** Semua perintah SQL untuk tabel layanan (CRUD). */
public class LayananDAO {

    // ===== READ =====

    public List<Layanan> semuaLayanan() throws SQLException {
        Map<Integer, Hewan> petaHewan = buatPetaHewan(new HewanDAO().semuaHewan());
        List<Layanan> hasil = new ArrayList<>();
        String sql = "SELECT * FROM layanan ORDER BY tanggal DESC, id DESC";

        try (Connection con = DatabaseConnection.get();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Hewan hewan = petaHewan.get(rs.getInt("hewan_id"));
                if (hewan != null) {
                    hasil.add(dariBaris(rs, hewan));
                }
            }
        }
        return hasil;
    }

    /** Ambil semua layanan hanya untuk hewan milik akun pelanggan tertentu. */
    public List<Layanan> semuaLayananMilik(int pemilikUserId) throws SQLException {
        Map<Integer, Hewan> petaHewan = buatPetaHewan(new HewanDAO().semuaHewanMilik(pemilikUserId));
        List<Layanan> hasil = new ArrayList<>();
        String sql = "SELECT l.* FROM layanan l "
                + "JOIN hewan h ON l.hewan_id = h.id "
                + "WHERE h.pemilik_user_id = ? "
                + "ORDER BY l.tanggal DESC, l.id DESC";

        try (Connection con = DatabaseConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pemilikUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Hewan hewan = petaHewan.get(rs.getInt("hewan_id"));
                    if (hewan != null) {
                        hasil.add(dariBaris(rs, hewan));
                    }
                }
            }
        }
        return hasil;
    }

    /**
     * Cari layanan (untuk admin): kata kunci, filter, dan urutan.
     * Kata dicocokkan ke kode layanan, nama/jenis hewan, nama pemilik, dan jenis layanan.
     */
    public List<Layanan> cari(String kata, String filter, String urutan) throws SQLException {
        Map<Integer, Hewan> petaHewan = buatPetaHewan(new HewanDAO().semuaHewan());
        StringBuilder sql = new StringBuilder(
                "SELECT l.* FROM layanan l JOIN hewan h ON l.hewan_id = h.id WHERE 1=1");

        String kataCari = kata == null ? "" : kata.trim();
        if (!kataCari.isEmpty()) {
            sql.append(" AND (l.kode LIKE ? OR h.nama LIKE ? OR h.jenis LIKE ?"
                    + " OR h.nama_pemilik LIKE ? OR l.jenis_layanan LIKE ?)");
        }

        if ("Grooming".equals(filter)) {
            sql.append(" AND l.jenis_layanan = 'GROOMING'");
        } else if ("Boarding".equals(filter)) {
            sql.append(" AND l.jenis_layanan = 'BOARDING'");
        } else if ("Check-in".equals(filter)) {
            sql.append(" AND l.status = 'CHECK_IN'");
        } else if ("Perawatan".equals(filter)) {
            sql.append(" AND l.status = 'PERAWATAN'");
        } else if ("Check-out".equals(filter)) {
            sql.append(" AND l.status = 'CHECK_OUT'");
        } else if ("Belum Bayar".equals(filter)) {
            sql.append(" AND l.status_pembayaran = 'BELUM_BAYAR'");
        } else if ("Lunas".equals(filter)) {
            sql.append(" AND l.status_pembayaran = 'LUNAS'");
        }

        if ("Terlama".equals(urutan)) {
            sql.append(" ORDER BY l.tanggal ASC, l.id ASC");
        } else if ("Kode (A-Z)".equals(urutan)) {
            sql.append(" ORDER BY l.kode ASC");
        } else if ("Nama Hewan (A-Z)".equals(urutan)) {
            sql.append(" ORDER BY h.nama ASC, l.tanggal DESC");
        } else if ("Jenis Hewan".equals(urutan)) {
            sql.append(" ORDER BY h.jenis ASC, h.nama ASC");
        } else if ("Status".equals(urutan)) {
            sql.append(" ORDER BY CASE l.status WHEN 'CHECK_IN' THEN 1 WHEN 'PERAWATAN' THEN 2 ELSE 3 END,"
                    + " l.tanggal DESC");
        } else if ("Total Tertinggi".equals(urutan)) {
            sql.append(" ORDER BY l.total_biaya DESC, l.id DESC");
        } else if ("Total Terendah".equals(urutan)) {
            sql.append(" ORDER BY l.total_biaya ASC, l.id DESC");
        } else {
            sql.append(" ORDER BY l.tanggal DESC, l.id DESC"); // Terbaru
        }

        List<Layanan> hasil = new ArrayList<>();
        try (Connection con = DatabaseConnection.get();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (!kataCari.isEmpty()) {
                for (int i = 1; i <= 5; i++) {
                    ps.setString(i, "%" + kataCari + "%");
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Hewan hewan = petaHewan.get(rs.getInt("hewan_id"));
                    if (hewan != null) {
                        hasil.add(dariBaris(rs, hewan));
                    }
                }
            }
        }
        return hasil;
    }

    private Map<Integer, Hewan> buatPetaHewan(List<Hewan> daftar) {
        Map<Integer, Hewan> peta = new HashMap<>();
        for (Hewan h : daftar) {
            peta.put(h.getId(), h);
        }
        return peta;
    }

    // ===== CREATE =====

    /** Kode layanan (GRM-0001 / BRD-0001) dibuat otomatis di sini. */
    public void tambah(Layanan l) throws SQLException {
        String sql = "INSERT INTO layanan (kode, jenis_layanan, hewan_id, tanggal, paket_grooming, "
                + "jumlah_hari, status, total_biaya, status_pembayaran) VALUES (?,?,?,?,?,?,?,?,?)";

        try (Connection con = DatabaseConnection.get()) {
            l.setKode(buatKode(con, l.getJenisLayanan()));

            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, l.getKode());
                ps.setString(2, l.getJenisLayanan());
                ps.setInt(3, l.getHewan().getId());
                ps.setString(4, l.getTanggal().toString()); // yyyy-MM-dd
                ps.setNull(5, Types.VARCHAR);
                ps.setNull(6, Types.INTEGER);

                if (l instanceof GroomingService) {
                    ps.setString(5, ((GroomingService) l).getPaket().name());
                } else if (l instanceof BoardingService) {
                    ps.setInt(6, ((BoardingService) l).getJumlahHari());
                }

                ps.setString(7, l.getStatus().name());
                ps.setDouble(8, l.hitungTotalBiaya());
                ps.setString(9, l.getStatusPembayaran().name()); // layanan baru selalu BELUM_BAYAR
                ps.executeUpdate();

                try (ResultSet kunci = ps.getGeneratedKeys()) {
                    if (kunci.next()) {
                        l.setId(kunci.getInt(1));
                    }
                }
            }
        }
    }

    // ===== UPDATE =====

    /** Menyimpan status baru + total biaya yang dihitung ulang. */
    public void ubahStatus(Layanan l) throws SQLException {
        String sql = "UPDATE layanan SET status = ?, total_biaya = ? WHERE id = ?";
        try (Connection con = DatabaseConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, l.getStatus().name());
            ps.setDouble(2, l.hitungTotalBiaya());
            ps.setInt(3, l.getId());
            ps.executeUpdate();
        }
    }

    /** Menyimpan data pembayaran (status LUNAS, metode, nominal, waktu bayar). */
    public void simpanPembayaran(Layanan l) throws SQLException {
        String sql = "UPDATE layanan SET status_pembayaran = ?, metode_pembayaran = ?, "
                + "jumlah_dibayar = ?, tanggal_bayar = ?, total_biaya = ? WHERE id = ?";
        try (Connection con = DatabaseConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, l.getStatusPembayaran().name());
            ps.setString(2, l.getMetodePembayaran());
            ps.setDouble(3, l.getJumlahDibayar());
            ps.setString(4, l.getTanggalBayar());
            ps.setDouble(5, l.hitungTotalBiaya());
            ps.setInt(6, l.getId());
            ps.executeUpdate();
        }
    }

    // ===== DELETE =====

    public void hapus(int id) throws SQLException {
        try (Connection con = DatabaseConnection.get();
             PreparedStatement ps = con.prepareStatement("DELETE FROM layanan WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ===== Bantuan =====

    private String buatKode(Connection con, String jenisLayanan) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTR(kode, 5) AS INTEGER)), 0) + 1 "
                + "FROM layanan WHERE jenis_layanan = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, jenisLayanan);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                String awalan = jenisLayanan.equals("GROOMING") ? "GRM" : "BRD";
                return String.format("%s-%04d", awalan, rs.getInt(1));
            }
        }
    }

    private Layanan dariBaris(ResultSet rs, Hewan hewan) throws SQLException {
        int id = rs.getInt("id");
        String kode = rs.getString("kode");
        LocalDate tanggal = LocalDate.parse(rs.getString("tanggal"));
        StatusPenitipan status = StatusPenitipan.valueOf(rs.getString("status"));

        Layanan layanan;
        if (rs.getString("jenis_layanan").equals("GROOMING")) {
            PaketGrooming paket = PaketGrooming.valueOf(rs.getString("paket_grooming"));
            layanan = new GroomingService(id, kode, hewan, tanggal, status, paket);
        } else {
            try {
                layanan = new BoardingService(id, kode, hewan, tanggal, status, rs.getInt("jumlah_hari"));
            } catch (DataTidakValidException e) {
                throw new SQLException("Data layanan " + kode + " di database tidak valid", e);
            }
        }

        // data pembayaran
        String statusBayar = rs.getString("status_pembayaran");
        if (statusBayar != null && !statusBayar.isEmpty()) {
            layanan.setStatusPembayaran(StatusPembayaran.valueOf(statusBayar));
        }
        layanan.setMetodePembayaran(rs.getString("metode_pembayaran"));
        layanan.setJumlahDibayar(rs.getDouble("jumlah_dibayar"));
        layanan.setTanggalBayar(rs.getString("tanggal_bayar"));
        return layanan;
    }
}
