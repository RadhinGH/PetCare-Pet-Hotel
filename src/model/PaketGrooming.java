package model;

/** Paket grooming beserta pengali harganya. */
public enum PaketGrooming {
    BASIC("Basic", 1.0),
    DELUXE("Deluxe", 1.5),
    SPA("Spa", 2.0);

    private final String label;
    private final double pengali;

    PaketGrooming(String label, double pengali) {
        this.label = label;
        this.pengali = pengali;
    }

    public String getLabel() {
        return label;
    }

    public double getPengali() {
        return pengali;
    }

    @Override
    public String toString() {
        return label + " (x" + pengali + ")";
    }
}
