package model;

/** Tahapan layanan: CHECK_IN -> PERAWATAN -> CHECK_OUT. */
public enum StatusPenitipan {
    CHECK_IN("Check-in"),
    PERAWATAN("Perawatan"),
    CHECK_OUT("Check-out");

    private final String label;

    StatusPenitipan(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Status sesudahnya, atau null kalau sudah Check-out. */
    public StatusPenitipan berikutnya() {
        StatusPenitipan[] semua = values();
        int urutan = ordinal() + 1;
        return urutan < semua.length ? semua[urutan] : null;
    }
}
