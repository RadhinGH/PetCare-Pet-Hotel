package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import util.FormatUang;

/** Layanan grooming: biaya = harga dasar hewan x pengali paket. */
public class GroomingService extends Layanan {

    private final PaketGrooming paket;

    public GroomingService(int id, String kode, Hewan hewan, LocalDate tanggal,
                           StatusPenitipan status, PaketGrooming paket) {
        super(id, kode, hewan, tanggal, status);
        this.paket = paket;
    }

    public PaketGrooming getPaket() {
        return paket;
    }

    @Override
    public String getJenisLayanan() {
        return "GROOMING";
    }

    @Override
    public String getNamaLayanan() {
        return "Grooming";
    }

    @Override
    public String getDetail() {
        return "Paket " + paket.getLabel();
    }

    @Override
    public double hitungTotalBiaya() {
        return getHewan().biayaGroomingDasar() * paket.getPengali();
    }

    @Override
    public List<BarisTagihan> rincianTagihan() {
        double dasar = getHewan().biayaGroomingDasar();
        List<BarisTagihan> baris = new ArrayList<>();
        baris.add(new BarisTagihan("Biaya dasar grooming (" + getHewan().getJenis() + ")",
                FormatUang.rupiah(dasar), false));
        baris.add(new BarisTagihan("Paket " + paket.getLabel() + " (x" + paket.getPengali() + ")",
                FormatUang.rupiah(hitungTotalBiaya()), false));
        return baris;
    }
}
