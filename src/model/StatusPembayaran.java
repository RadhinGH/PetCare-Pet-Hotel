package model;

/** Status pembayaran sebuah layanan: awalnya BELUM_BAYAR, jadi LUNAS setelah dibayar saat Check-out. */
public enum StatusPembayaran {
    BELUM_BAYAR("Belum Bayar"),
    LUNAS("Lunas");

    private final String label;

    StatusPembayaran(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
