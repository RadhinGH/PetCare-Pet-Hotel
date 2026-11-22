package model;

/** Custom exception: dilempar saat data yang diisi user tidak masuk akal. */
public class DataTidakValidException extends Exception {

    public DataTidakValidException(String pesan) {
        super(pesan);
    }
}
