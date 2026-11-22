package model;

public class User {

    private final int id;
    private final String username;
    private final String namaLengkap;
    private final String peran;

    public User(int id, String username, String namaLengkap, String peran) {
        this.id = id;
        this.username = username;
        this.namaLengkap = namaLengkap;
        this.peran = peran;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
    public String getNamaLengkap() {
        return namaLengkap;
    }

    public String getPeran() {
        return peran;
    }

    public boolean isPelanggan() {
        return "pelanggan".equalsIgnoreCase(peran);
    }

    public boolean isAdmin() {
        return !isPelanggan();
    }

    @Override
    public String toString() {
        return namaLengkap + " (" + username + " )";
    }

}
