package util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/** Mengubah angka menjadi tulisan rupiah, contoh: 441000 -> "Rp441.000". */
public final class FormatUang {

    private static final DecimalFormat FORMAT;

    static {
        DecimalFormatSymbols simbol = new DecimalFormatSymbols();
        simbol.setGroupingSeparator('.');
        FORMAT = new DecimalFormat("#,##0", simbol);
    }

    private FormatUang() { }

    public static String rupiah(double nilai) {
        return "Rp" + FORMAT.format(nilai);
    }
}
