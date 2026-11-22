package model;

/** Satu baris pada rincian tagihan, contoh: "Jumlah hari" -> "7 hari". */
public class BarisTagihan {

    private final String label;
    private final String nilai;
    private final boolean potongan; // true = tampil hijau (diskon)

    public BarisTagihan(String label, String nilai, boolean potongan) {
        this.label = label;
        this.nilai = nilai;
        this.potongan = potongan;
    }

    public String getLabel() {
        return label;
    }

    public String getNilai() {
        return nilai;
    }

    public boolean isPotongan() {
        return potongan;
    }
}
