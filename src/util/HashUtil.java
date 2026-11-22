package util;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilitas hash SHA-256 (MessageDigest).
 * Password TIDAK disimpan sebagai teks pola, hanya hashnya.
 * Hasil berupa 64 karakter hex huruf kecil — sama dengan SHA2(x, 256) di MySQL.
 */

public final class HashUtil {

    private HashUtil() { }

    public static String sha256(String teks) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digesst = md.digest(teks.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digesst.length *2);

            for (byte b : digesst) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritma SHA-256 tidak tersedia", e);
        }
    }

}
