package model;

/**
 * Abstract class induk semua hewan (Kucing, Anjing, Reptil).
 * Atribut dibuat private (ENKAPSULASI); isinya hanya bisa diubah lewat setter
 * yang memeriksa dulu apakah datanya masuk akal.
 */
public abstract class Hewan {

    private int id;
    private String nama;
    private int umur;
    private double berat;
    private String namaPemilik;
    private String teleponPemilik;
    private Integer pemilikUserId;

    protected Hewan(int id, String nama, int umur, double berat,
                    String namaPemilik, String teleponPemilik) throws DataTidakValidException {
        this.id = id;
        setNama(nama);
        setUmur(umur);
        setBerat(berat);
        setNamaPemilik(namaPemilik);
        setTeleponPemilik(teleponPemilik);
    }

    // ----- method abstrak: WAJIB diisi oleh setiap subclass -----

    /** "Kucing", "Anjing", atau "Reptil". */
    public abstract String getJenis();

    /** Keterangan khusus jenis ini (ras, ukuran, suhu kandang). */
    public abstract String getDetail();

    /** Tarif penitipan per hari. */
    public abstract double tarifBoardingPerHari();

    /** Harga dasar grooming (sebelum dikali pengali paket). */
    public abstract double biayaGroomingDasar();

    // ----- getter & setter -----

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }
    public void setNama(String nama) throws DataTidakValidException {
        if (nama == null || nama.trim().isEmpty()) {
            throw new DataTidakValidException("Nama hewan belum diisi");
        }
        this.nama = nama.trim();
    }

    public int getUmur() {
        return umur;
    }
    public void setUmur(int umur) throws DataTidakValidException {
        if (umur < 0) {
            throw new DataTidakValidException("Umur tidak boleh negatif");
        }
        this.umur = umur;
    }

    public double getBerat() {
        return berat;
    }

    public void setBerat(double berat) throws DataTidakValidException {
        if (berat <= 0) {
            throw new DataTidakValidException("Berat harus lebih dari 0");
        }
        this.berat = berat;
    }

    public String getNamaPemilik() {
        return namaPemilik;
    }

    public void setNamaPemilik(String namaPemilik) throws DataTidakValidException {
        if (namaPemilik == null || namaPemilik.trim().isEmpty()) {
            throw new DataTidakValidException("Nama pemilik belum diisi");
        }
        this.namaPemilik = namaPemilik.trim();
    }

    public String getTeleponPemilik() {
        return teleponPemilik;
    }

    public void setTeleponPemilik(String telepon) throws DataTidakValidException {
        if (telepon == null || !telepon.trim().matches("[0-9+\\- ]{8,20}")) {
            throw new DataTidakValidException("Nomor telepon tidak valid (minimal 8 angka)");
        }
        this.teleponPemilik = telepon.trim();
    }

    public Integer getPemilikUserId() {
        return pemilikUserId;
    }

    public void setPemilikUserId(Integer pemilikUserId) {
        this.pemilikUserId = pemilikUserId;
    }

    @Override
    public String toString() {
        return nama + " (" + getJenis() + ")";
    }
}
