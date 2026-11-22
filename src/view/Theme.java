package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

/** Pusat warna dan font.  Ubah di sini. */
public final class Theme {

    // latar gradasi
    public static final Color BG_ATAS = new Color(0xFBC6D9);
    public static final Color BG_BAWAH = new Color(0xF5A6C6);

    // warna utama (tombol, judul)
    public static final Color PRIMARY = new Color(0xEE5D9E);
    public static final Color PRIMARY_TUA = new Color(0xD63E85);
    public static final Color PRIMARY_MUDA = new Color(0xF98FC4);

    // warna teks
    public static final Color TEKS_TUA = new Color(0x59364B);
    public static final Color TEKS_MUDA = new Color(0xB491A3);

    // garis input, link, pola telapak
    public static final Color BORDER_FIELD = new Color(0xF2C3D9);
    public static final Color LINK = new Color(0xE874AC);
    public static final Color PAW_PUTIH = new Color(255, 255, 255, 75);
    public static final Color PAW_PINK = new Color(233, 137, 184, 90);
    public static final Color PAW_UNGU = new Color(169, 170, 246, 150);
    public static final Color GARIS = new Color(0xF2C3D9);
    public static final Color ERROR = new Color(0xE23D6D);
    public static final Color KOTAK_DEMO = new Color(0xFDF2F7);

    // warna tambahan untuk halaman konten
    public static final Color SUKSES = new Color(0x2E9E6B);
    public static final Color SUKSES_MUDA = new Color(0xE3F6EC);
    public static final Color LATAR_KONTEN = new Color(0xFFF8FB);
    public static final Color BARIS_GANJIL = new Color(0xFFF3F8);
    public static final Color BARIS_DIPILIH = new Color(0xFCDDEB);

    // warna portal pelanggan & kartu laporan
    public static final Color BG_PELANGGAN_ATAS = new Color(0xD8C7FA);
    public static final Color BG_PELANGGAN_BAWAH = new Color(0xF3B6D8);
    public static final Color UNGU_TUA = new Color(0x6B46C1);
    public static final Color UNGU_MUDA = new Color(0xF3EEFF);
    public static final Color BIRU_TUA = new Color(0x2B6CB0);
    public static final Color BIRU_MUDA = new Color(0xEBF4FF);

    // warna error toaster pop up
    public static final Color WARNING = new Color(0xE23D6D);

    private static final String FONT_FAMILY = pilihFont();

    private Theme() { }

    /** Pakai Segoe UI kalau ada di komputer, kalau tidak pakai SansSerif. */
    private static String pilihFont() {
        for (String nama : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            if (nama.equals("Segoe UI")) {
                return nama;
            }
        }
        return "SansSerif";
    }

    public static Font font(int style, int size) {
        return new Font(FONT_FAMILY, style, size);
    }
}
