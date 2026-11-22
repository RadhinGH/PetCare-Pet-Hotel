package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Abstract class induk layanan. Menerapkan interface Tagihan, tetapi cara
 * menghitung biaya diserahkan ke subclass (GroomingService / BoardingService).
 */
public abstract class Layanan implements Tagihan {

    public static final DateTimeFormatter FORMAT_TAMPIL = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter FORMAT_WAKTU_BAYAR = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private int id;
    private String kode;
    private final Hewan hewan;
    private final LocalDate tanggal;
    private StatusPenitipan status;

    // data pembayaran (awalnya belum bayar)
    private StatusPembayaran statusPembayaran = StatusPembayaran.BELUM_BAYAR;
    private String metodePembayaran;
    private double jumlahDibayar;
    private String tanggalBayar;

    protected Layanan(int id, String kode, Hewan hewan, LocalDate tanggal, StatusPenitipan status) {
        this.id = id;
        this.kode = kode;
        this.hewan = hewan;
        this.tanggal = tanggal;
        this.status = status;
    }

    /** "GROOMING" atau "BOARDING" (sama dengan isi kolom jenis_layanan di DB). */
    public abstract String getJenisLayanan();

    /** Nama untuk ditampilkan di GUI. */
    public abstract String getNamaLayanan();

    /** Keterangan singkat: paket grooming atau jumlah hari. */
    public abstract String getDetail();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        this.kode = kode;
    }

    public Hewan getHewan() {
        return hewan;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public StatusPenitipan getStatus() {
        return status;
    }

    public void setStatus(StatusPenitipan status) {
        this.status = status;
    }

    // ----- pembayaran -----

    public boolean isLunas() {
        return statusPembayaran == StatusPembayaran.LUNAS;
    }

    /** Tagihan baru boleh dibayar kalau sudah Check-out dan belum lunas. */
    public boolean bisaDibayar() {
        return status == StatusPenitipan.CHECK_OUT && !isLunas();
    }

    /** Uang kembalian (hanya ada kalau sudah lunas dan uang yang dibayar lebih besar dari tagihan). */
    public double getKembalian() {
        return isLunas() ? Math.max(0, jumlahDibayar - hitungTotalBiaya()) : 0;
    }

    public void tandaiLunas(String metode, double jumlahDibayar, String tanggalBayar) {
        this.statusPembayaran = StatusPembayaran.LUNAS;
        this.metodePembayaran = metode;
        this.jumlahDibayar = jumlahDibayar;
        this.tanggalBayar = tanggalBayar;
    }

    public StatusPembayaran getStatusPembayaran() {
        return statusPembayaran;
    }

    public void setStatusPembayaran(StatusPembayaran statusPembayaran) {
        this.statusPembayaran = statusPembayaran;
    }

    public String getMetodePembayaran() {
        return metodePembayaran;
    }

    public void setMetodePembayaran(String metodePembayaran) {
        this.metodePembayaran = metodePembayaran;
    }

    public double getJumlahDibayar() {
        return jumlahDibayar;
    }

    public void setJumlahDibayar(double jumlahDibayar) {
        this.jumlahDibayar = jumlahDibayar;
    }

    public String getTanggalBayar() {
        return tanggalBayar;
    }

    public void setTanggalBayar(String tanggalBayar) {
        this.tanggalBayar = tanggalBayar;
    }
}
