package model;

/** Anjing: tarif grooming bergantung ukuran badan. */
public class Anjing extends Hewan {

    public static final String[] DAFTAR_UKURAN = {"Kecil", "Sedang", "Besar"};

    private String ukuran;

    public Anjing(int id, String nama, int umur, double berat, String namaPemilik,
                  String teleponPemilik, String ukuran) throws DataTidakValidException {
        super(id, nama, umur, berat, namaPemilik, teleponPemilik);
        setUkuran(ukuran);
    }

    public String getUkuran() {
        return ukuran;
    }

    public void setUkuran(String ukuran) throws DataTidakValidException {
        for (String u : DAFTAR_UKURAN) {
            if (u.equals(ukuran)) {
                this.ukuran = ukuran;
                return;
            }
        }
        throw new DataTidakValidException("Ukuran anjing harus Kecil, Sedang, atau Besar");
    }

    @Override
    public String getJenis() {
        return "Anjing";
    }

    @Override
    public String getDetail() {
        return "Ukuran " + ukuran;
    }

    @Override
    public double tarifBoardingPerHari() {
        return 70000;
    }

    @Override
    public double biayaGroomingDasar() {
        switch (ukuran) {
            case "Kecil":  return 60000;
            case "Sedang": return 80000;
            default:       return 100000; // Besar
        }
    }
}
