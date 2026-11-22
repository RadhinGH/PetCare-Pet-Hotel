-- ═══════════════════════════════════════════════════════════════════════
--  pethotel.sql — Skema & Data Awal Basis Data SQLite
--  Sistem Informasi Pet Care & Pet Hotel «PetCare»
--  Tugas Besar Pemrograman Berorientasi Objek (SYS61516)
--  Catatan: SQLite tanpa server — cukup satu berkas pethotel.db
--  Cara pakai ( salah satu ):
--    1) CLI      : sqlite3 pethotel.db < pethotel.sql
--    2) Java/JDBC: DriverManager.getConnection("jdbc:sqlite:pethotel.db")
--                  lalu eksekusi skrip ini via Statement (seeder class)
-- ═══════════════════════════════════════════════════════════════════════

PRAGMA foreign_keys = ON;   -- wajib agar constraint FOREIGN KEY aktif di SQLite

-- ── Bersihkan tabel lama (agar skrip bisa dijalankan ulang) ──
DROP TABLE IF EXISTS layanan;
DROP TABLE IF EXISTS hewan;
DROP TABLE IF EXISTS pengguna;

-- ═══════════════════════════════════════════════════════════════════
--  TABEL PENGGUNA  →  class Pengguna (model) + PenggunaDAO (login)
--  Password disimpan sebagai hash SHA-256 (hex), bukan teks polos.
--  Hash seed di bawah = SHA-256 dari:
--    admin123        → 240be518...  (petugas admin)
--    resepsionis123  → f7a9f07c...  (resepsionis)
--    pelanggan123    → c72b07da...  (pelanggan demo)
--  (hitung ulang: python -c "import hashlib;print(hashlib.sha256(b'admin123').hexdigest())")
-- ═══════════════════════════════════════════════════════════════════
CREATE TABLE pengguna (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    username      TEXT    NOT NULL UNIQUE,
    password_hash TEXT    NOT NULL,               -- SHA-256 hex (64 karakter)
    nama_lengkap  TEXT    NOT NULL,
    peran         TEXT    NOT NULL DEFAULT 'admin',   -- 'admin' / 'resepsionis' / 'pelanggan'
    dibuat_pada   TEXT    NOT NULL DEFAULT (datetime('now'))
);

INSERT INTO pengguna (username, password_hash, nama_lengkap, peran) VALUES
  ('admin',       '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Andi Saputra',   'admin'),
  ('resepsionis', 'f7a9f07cf479ca415f01dbdb0d6c45830ef6bf372f9ee6206060afbb5a9e26a0', 'Budi Santoso',   'resepsionis'),
  ('pelanggan1',  'c72b07da279dcd31547fbce04ba65815266ae8689b0fe069ad9080502c57b264', 'Cici Wulandari', 'pelanggan'),
  ('pelanggan2',  'c72b07da279dcd31547fbce04ba65815266ae8689b0fe069ad9080502c57b264', 'Dian Puspita',   'pelanggan');

-- ═══════════════════════════════════════════════════════════════════════
--  TABEL HEWAN  →  class Hewan (abstract) + Kucing / Anjing / Reptil
--  Kolom "single table inheritance": kolom khusus tiap jenis boleh NULL
-- ═══════════════════════════════════════════════════════════════════════
CREATE TABLE hewan (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    nama            TEXT    NOT NULL,
    jenis           TEXT    NOT NULL CHECK (jenis IN ('Kucing','Anjing','Reptil')),
    ras             TEXT,                      -- khusus Kucing
    steril          INTEGER,                   -- khusus Kucing (0 = belum, 1 = steril)
    ukuran          TEXT                       -- khusus Anjing ('Kecil','Sedang','Besar')
                    CHECK (ukuran IN ('Kecil','Sedang','Besar')),
    suhu_kandang    REAL,                      -- khusus Reptil (derajat Celsius)
    umur            INTEGER NOT NULL CHECK (umur >= 0),
    berat           REAL    NOT NULL CHECK (berat > 0),
    nama_pemilik    TEXT    NOT NULL,
    telepon_pemilik TEXT    NOT NULL,
    pemilik_user_id INTEGER REFERENCES pengguna(id) ON DELETE SET NULL
);

CREATE INDEX idx_hewan_nama ON hewan(nama);     -- mempercepat fitur pencarian
CREATE INDEX idx_hewan_jenis ON hewan(jenis);   -- mempercepat sortir/filter jenis
CREATE INDEX idx_hewan_pemilik_user ON hewan(pemilik_user_id);

-- ═══════════════════════════════════════════════════════════════════════
--  TABEL LAYANAN  →  class Layanan (abstract)
--                    + GroomingService / BoardingService
--  status mengikuti enum StatusPenitipan:
--    CHECK_IN → PERAWATAN → CHECK_OUT
-- ═══════════════════════════════════════════════════════════════════════
CREATE TABLE layanan (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    kode            TEXT    NOT NULL UNIQUE,   -- contoh: GRM-0001, BRD-0001
    jenis_layanan   TEXT    NOT NULL CHECK (jenis_layanan IN ('GROOMING','BOARDING')),
    hewan_id        INTEGER NOT NULL,
    tanggal         TEXT    NOT NULL,           -- format yyyy-MM-dd
    paket_grooming  TEXT    CHECK (paket_grooming IN ('BASIC','DELUXE','SPA')),
    jumlah_hari     INTEGER CHECK (jumlah_hari > 0),
    status          TEXT    NOT NULL DEFAULT 'CHECK_IN'
                    CHECK (status IN ('CHECK_IN','PERAWATAN','CHECK_OUT')),
    total_biaya     REAL,                       -- hasil hitungTotalBiaya()
    status_pembayaran TEXT  NOT NULL DEFAULT 'BELUM_BAYAR'
                    CHECK (status_pembayaran IN ('BELUM_BAYAR','LUNAS')),
    metode_pembayaran TEXT,                     -- Tunai / QRIS / Transfer Bank / E-Wallet
    jumlah_dibayar  REAL,
    tanggal_bayar   TEXT,                       -- format yyyy-MM-dd HH:mm
    FOREIGN KEY (hewan_id) REFERENCES hewan(id) ON DELETE CASCADE
);

CREATE INDEX idx_layanan_hewan ON layanan(hewan_id);

-- ═══════════════════════════════════════════════════════════════════════
--  DATA CONTOH (seed) — untuk demo & pengujian black-box
--  Total biaya mengikuti aturan bisnis:
--    Grooming  = biayaGroomingDasar() x multiplier (BASIC 1.0 / DELUXE 1.5 / SPA 2.0)
--    Boarding  = tarifBoardingPerHari() x jumlah hari, diskon 10% jika >= 7 hari
--    Tarif/hari : Kucing 50.000 | Anjing 70.000 | Reptil 40.000
-- ═══════════════════════════════════════════════════════════════════════
INSERT INTO hewan (nama, jenis, ras, steril, ukuran, suhu_kandang, umur, berat, nama_pemilik, telepon_pemilik, pemilik_user_id) VALUES
  ('Mochi', 'Kucing', 'Persia',        1, NULL,     NULL, 2,  4.2, 'Andi Saputra',  '0812-3456-7890', NULL),
  ('Bruno', 'Anjing', NULL,            NULL, 'Besar', NULL, 4, 28.5, 'Budi Santoso',  '0813-2222-1010', NULL),
  ('Rango', 'Reptil', NULL,            NULL, NULL,    28.0, 3,  1.2, 'Cici Wulandari','0821-1111-2020', 3),
  ('Sisi',  'Kucing', 'Domestik',      0, NULL,     NULL, 1,  3.8, 'Dian Puspita',  '0857-8888-3030', 4),
  ('Rex',   'Anjing', NULL,            NULL, 'Kecil', NULL, 2,  8.0, 'Eka Pratama',   '0878-9999-4040', NULL),
  ('Nagini','Reptil', NULL,            NULL, NULL,    30.5, 5,  2.5, 'Fajar Ramadhan','0896-7777-5050', NULL);

INSERT INTO layanan (kode, jenis_layanan, hewan_id, tanggal, paket_grooming, jumlah_hari, status, total_biaya,
                     status_pembayaran, metode_pembayaran, jumlah_dibayar, tanggal_bayar) VALUES
  ('GRM-0001', 'GROOMING',  1, '2026-11-05', 'DELUXE', NULL, 'CHECK_OUT',  90000.0, 'LUNAS',       'Tunai', 100000.0, '2026-11-05 14:30'),  -- Mochi 60.000 x 1.5
  ('GRM-0002', 'GROOMING',  2, '2026-11-06', 'SPA',    NULL, 'PERAWATAN', 200000.0, 'BELUM_BAYAR', NULL,    NULL,     NULL),                -- Bruno Besar 100.000 x 2.0
  ('BRD-0001', 'BOARDING',  1, '2026-11-01', NULL,     3,    'CHECK_OUT', 150000.0, 'LUNAS',       'QRIS',  150000.0, '2026-11-04 10:15'),  -- Mochi 50.000 x 3
  ('BRD-0002', 'BOARDING',  3, '2026-11-07', NULL,     5,    'PERAWATAN', 200000.0, 'BELUM_BAYAR', NULL,    NULL,     NULL),                -- Rango 40.000 x 5
  ('BRD-0003', 'BOARDING',  2, '2026-11-09', NULL,     7,    'CHECK_IN',  441000.0, 'BELUM_BAYAR', NULL,    NULL,     NULL),                -- Bruno 70.000 x 7 - 10%
  ('GRM-0003', 'GROOMING',  4, '2026-11-10', 'BASIC',  NULL, 'CHECK_IN',  60000.0,  'BELUM_BAYAR', NULL,    NULL,     NULL);                -- Sisi 60.000 x 1.0

-- ═══════════════════════════════════════════════════════════════════════
--  CONTOH KUERI YANG DIPAKAI APLIKASI (referensi DAO)
-- ═══════════════════════════════════════════════════════════════════════
--  Read semua hewan        : SELECT * FROM hewan ORDER BY nama;
--  Cari hewan              : SELECT * FROM hewan WHERE nama LIKE ? || '%';
--  Read layanan + JOIN     : SELECT l.kode, h.nama, l.jenis_layanan, l.tanggal,
--                                    l.paket_grooming, l.jumlah_hari, l.status, l.total_biaya
--                             FROM layanan l JOIN hewan h ON l.hewan_id = h.id
--                             ORDER BY l.tanggal DESC;
--  Update status tracking  : UPDATE layanan SET status = ? WHERE id = ?;
--  Update data hewan       : UPDATE hewan SET nama=?, jenis=?, ras=?, steril=?,
--                                    ukuran=?, suhu_kandang=?, umur=?, berat=?,
--                                    nama_pemilik=?, telepon_pemilik=? WHERE id=?;
--  Delete hewan + layanan  : DELETE FROM hewan WHERE id = ?;  (layanan ikut
--                             terhapus otomatis karena ON DELETE CASCADE)
--  Login (autentikasi)     : SELECT id, username, password_hash, nama_lengkap
--                             FROM pengguna WHERE username = ?;
--                             → hash input dibandingkan di Java (MessageDigest)
