package model;

/** Reptil: butuh kandang dengan suhu tertentu. */
public class    Reptil extends Hewan {

    private double suhuKandang;

    public Reptil(int id, String nama, int umur, double berat, String namaPemilik,
                  String teleponPemilik, double suhuKandang) throws DataTidakValidException {
        super(id, nama, umur, berat, namaPemilik, teleponPemilik);
        setSuhuKandang(suhuKandang);
    }

    public double getSuhuKandang() {
        return suhuKandang;
    }

    public void setSuhuKandang(double suhuKandang) throws DataTidakValidException {
        if (suhuKandang < 15 || suhuKandang > 45) {
            throw new DataTidakValidException("Suhu kandang harus antara 15 - 45 \u00B0C");
        }
        this.suhuKandang = suhuKandang;
    }

    @Override
    public String getJenis() {
        return "Reptil";
    }

    @Override
    public String getDetail() {
        return "Suhu kandang " + suhuKandang + " \u00B0C";
    }

    @Override
    public double tarifBoardingPerHari() {
        return 40000;
    }

    @Override
    public double biayaGroomingDasar() {
        return 45000;
    }
}
