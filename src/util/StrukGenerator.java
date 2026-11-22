package util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.imageio.ImageIO;
import model.BarisTagihan;
import model.Hewan;
import model.Layanan;
import view.Theme;
import view_need.PawPainter;

/**
 * Membuat gambar struk pembayaran sederhana bertema PetCare (PNG).
 * Hanya memakai Java 2D bawaan JDK tanpa library tambahan.
 */
public final class StrukGenerator {

    private static final int LEBAR = 500;
    private static final int KIRI_TEKS = 42;
    private static final int KANAN_TEKS = 458;
    private static final DateTimeFormatter FORMAT_CETAK =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private StrukGenerator() { }

    public static BufferedImage buatGambarStruk(Layanan layanan) {
        List<BarisTagihan> rincian = layanan.rincianTagihan();
        boolean lunas = layanan.isLunas();
        int tinggi = (lunas ? 626 : 520) + rincian.size() * 26;

        BufferedImage img = new BufferedImage(LEBAR, tinggi, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Latar luar lembut dan kartu struk putih
        g.setColor(Theme.LATAR_KONTEN);
        g.fillRect(0, 0, LEBAR, tinggi);

        g.setColor(Color.WHITE);
        g.fillRoundRect(18, 16, LEBAR - 36, tinggi - 32, 24, 24);
        g.setColor(Theme.BORDER_FIELD);
        g.drawRoundRect(18, 16, LEBAR - 36, tinggi - 32, 24, 24);

        gambarHeader(g);

        int y = 142;
        Hewan h = layanan.getHewan();
        y = gambarInfo(g, y, "No. Struk", layanan.getKode());
        y = gambarInfo(g, y, "Tanggal", layanan.getTanggal().format(Layanan.FORMAT_TAMPIL));
        y = gambarInfo(g, y, "Status", lunas
                ? "Check-out \u2022 LUNAS"
                : layanan.getStatus().getLabel() + " \u2022 Belum Bayar",
                lunas ? Theme.SUKSES : Theme.TEKS_TUA);
        y = gambarInfo(g, y, "Hewan", h.getNama() + " (" + h.getJenis() + ")");
        y = gambarInfo(g, y, "Detail Hewan", h.getDetail());
        y = gambarInfo(g, y, "Pemilik", h.getNamaPemilik());
        y = gambarInfo(g, y, "Telepon", h.getTeleponPemilik());
        y = gambarInfo(g, y, "Layanan", layanan.getNamaLayanan() + " - " + layanan.getDetail());

        gambarGarisPutus(g, y - 4);
        y += 18;

        g.setFont(Theme.font(Font.BOLD, 11));
        g.setColor(Theme.TEKS_MUDA);
        g.drawString("RINCIAN BIAYA", KIRI_TEKS, y);
        y += 22;

        for (BarisTagihan b : rincian) {
            g.setFont(Theme.font(Font.PLAIN, 12));
            g.setColor(Theme.TEKS_TUA);
            g.drawString(potongTeks(g, b.getLabel(), 260), KIRI_TEKS, y);

            g.setFont(Theme.font(Font.BOLD, 12));
            g.setColor(b.isPotongan() ? Theme.SUKSES : Theme.TEKS_TUA);
            gambarKanan(g, b.getNilai(), KANAN_TEKS, y);
            y += 26;
        }

        gambarGarisPutus(g, y - 6);
        y += 12;

        gambarKotakTotal(g, y, FormatUang.rupiah(layanan.hitungTotalBiaya()));
        y += 80;

        if (lunas) {
            y = gambarInfo(g, y, "Metode Bayar", layanan.getMetodePembayaran());
            y = gambarInfo(g, y, "Dibayar", FormatUang.rupiah(layanan.getJumlahDibayar()));
            y = gambarInfo(g, y, "Kembalian", FormatUang.rupiah(layanan.getKembalian()));
            y = gambarInfo(g, y, "Waktu Bayar", layanan.getTanggalBayar());
            y += 14;
        }

        g.setFont(Theme.font(Font.PLAIN, 11));
        g.setColor(Theme.TEKS_MUDA);
        gambarTengah(g, "Terima kasih telah mempercayakan peliharaan Anda!", LEBAR / 2, y);

        g.setFont(Theme.font(Font.PLAIN, 10));
        gambarTengah(g, "Dicetak: " + LocalDateTime.now().format(FORMAT_CETAK), LEBAR / 2, y + 18);

        g.dispose();
        return img;
    }

    public static void simpanPng(Layanan layanan, File tujuan) throws IOException {
        ImageIO.write(buatGambarStruk(layanan), "png", tujuan);
    }

    private static void gambarHeader(Graphics2D g) {
        g.setPaint(new GradientPaint(34, 32, Theme.PRIMARY_MUDA, LEBAR - 34, 116, Theme.PRIMARY));
        g.fillRoundRect(34, 32, LEBAR - 68, 84, 18, 18);

        PawPainter.gambar(g, 52, 57, 32, -12, new Color(255, 255, 255, 220));
        PawPainter.gambar(g, LEBAR - 84, 57, 32, 12, new Color(255, 255, 255, 220));

        g.setColor(Color.WHITE);
        g.setFont(Theme.font(Font.BOLD, 22));
        gambarTengah(g, "PetCare", LEBAR / 2, 68);

        g.setFont(Theme.font(Font.PLAIN, 12));
        gambarTengah(g, "Pet Care & Pet Hotel \u2014 Struk Pembayaran", LEBAR / 2, 92);
    }

    private static int gambarInfo(Graphics2D g, int y, String label, String nilai) {
        return gambarInfo(g, y, label, nilai, Theme.TEKS_TUA);
    }

    private static int gambarInfo(Graphics2D g, int y, String label, String nilai, Color warnaNilai) {
        g.setFont(Theme.font(Font.PLAIN, 12));
        g.setColor(Theme.TEKS_MUDA);
        g.drawString(label, KIRI_TEKS, y);

        g.setFont(Theme.font(Font.BOLD, 12));
        g.setColor(warnaNilai);
        gambarKanan(g, potongTeks(g, nilai, 290), KANAN_TEKS, y);
        return y + 24;
    }

    private static void gambarKotakTotal(Graphics2D g, int y, String totalRupiah) {
        g.setColor(Theme.SUKSES_MUDA);
        g.fillRoundRect(38, y, LEBAR - 76, 56, 16, 16);
        g.setColor(new Color(0xA9E1C5));
        g.drawRoundRect(38, y, LEBAR - 76, 56, 16, 16);

        g.setColor(Theme.SUKSES);
        g.setFont(Theme.font(Font.BOLD, 12));
        g.drawString("TOTAL TAGIHAN", KIRI_TEKS + 12, y + 33);

        g.setFont(Theme.font(Font.BOLD, 20));
        gambarKanan(g, totalRupiah, KANAN_TEKS - 12, y + 35);
    }

    private static void gambarGarisPutus(Graphics2D g, int y) {
        Stroke lama = g.getStroke();
        g.setColor(Theme.BORDER_FIELD);
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10f, new float[]{5f, 5f}, 0f));
        g.drawLine(KIRI_TEKS, y, KANAN_TEKS, y);
        g.setStroke(lama);
    }

    private static void gambarTengah(Graphics2D g, String teks, int tengahX, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(teks, tengahX - fm.stringWidth(teks) / 2, y);
    }

    private static void gambarKanan(Graphics2D g, String teks, int kananX, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(teks, kananX - fm.stringWidth(teks), y);
    }

    private static String potongTeks(Graphics2D g, String teks, int maxLebar) {
        if (teks == null) {
            return "-";
        }
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(teks) <= maxLebar) {
            return teks;
        }
        String hasil = teks;
        while (hasil.length() > 3 && fm.stringWidth(hasil + "...") > maxLebar) {
            hasil = hasil.substring(0, hasil.length() - 1);
        }
        return hasil + "...";
    }
}
