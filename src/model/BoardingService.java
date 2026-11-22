package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import util.FormatUang;

/** Layanan penitipan: biaya = tarif per hari x jumlah hari, diskon 10% jika >= 7 hari. */
public class BoardingService extends Layanan {

    public static final int HARI_MINIMAL_DISKON = 7;
    public static final double PERSEN_DISKON = 10;

    private final int jumlahHari;

    public BoardingService(int id, String kode, Hewan hewan, LocalDate tanggal,
                           StatusPenitipan status, int jumlahHari) throws DataTidakValidException {
        super(id, kode, hewan, tanggal, status);
        if (jumlahHari <= 0) {
            throw new DataTidakValidException("Jumlah hari menginap minimal 1");
        }
        this.jumlahHari = jumlahHari;
    }

    public int getJumlahHari() {
        return jumlahHari;
    }

    @Override
    public String getJenisLayanan() {
        return "BOARDING";
    }

    @Override
    public String getNamaLayanan() {
        return "Boarding (Penitipan)";
    }

    @Override
    public String getDetail() {
        return jumlahHari + " hari";
    }

    private double subtotal() {
        return getHewan().tarifBoardingPerHari() * jumlahHari;
    }

    private double diskon() {
        return jumlahHari >= HARI_MINIMAL_DISKON ? subtotal() * PERSEN_DISKON / 100 : 0;
    }

    @Override
    public double hitungTotalBiaya() {
        return subtotal() - diskon();
    }

    @Override
    public List<BarisTagihan> rincianTagihan() {
        List<BarisTagihan> baris = new ArrayList<>();
        baris.add(new BarisTagihan("Tarif per hari (" + getHewan().getJenis() + ")",
                FormatUang.rupiah(getHewan().tarifBoardingPerHari()), false));
        baris.add(new BarisTagihan("Jumlah hari", jumlahHari + " hari", false));
        baris.add(new BarisTagihan("Subtotal", FormatUang.rupiah(subtotal()), false));
        if (diskon() > 0) {
            baris.add(new BarisTagihan("Diskon penitipan \u2265 " + HARI_MINIMAL_DISKON
                    + " hari (" + (int) PERSEN_DISKON + "%)",
                    "\u2212 " + FormatUang.rupiah(diskon()), true));
        }
        return baris;
    }
}
