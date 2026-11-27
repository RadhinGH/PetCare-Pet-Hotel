package model;

/** Kucing: punya ras dan status steril. */
public class Kucing extends Hewan {

    private String ras;
    private boolean steril;

    public Kucing(int id, String nama, int umur, double berat, String namaPemilik,
                  String teleponPemilik, String ras, boolean steril) throws DataTidakValidException {
        super(id, nama, umur, berat, namaPemilik, teleponPemilik);
        setRas(ras);
        this.steril = steril;
    }

    public String getRas() {
        return ras;
    }

    public void setRas(String ras) throws DataTidakValidException {
        if (ras == null || ras.trim().isEmpty()) {
            throw new DataTidakValidException("Ras kucing belum diisi");
        }
        this.ras = ras.trim();
    }

    public boolean isSteril() {
        return steril;
    }

    public void setSteril(boolean steril) { this.steril = steril;}

    @Override
    public String getJenis() {
        return "Kucing";
    }

    @Override
    public String getDetail() {
        return ras + (steril ? " \u2022 Steril" : " \u2022 Belum steril");
    }

    @Override
    public double tarifBoardingPerHari() {
        return 50000;
    }

    @Override
    public double biayaGroomingDasar() {
        return 60000;
    }
}
