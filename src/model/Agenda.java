package model;

public class Agenda {
    private int id;                  // ID agenda
    private String judul;            // Judul agenda
    private String tanggal;          // Tanggal agenda
    private String deskripsi;        // Deskripsi agenda

    // Constructor
    public Agenda(int id, String judul, String tanggal, String deskripsi) {
        this.id = id;
        this.judul = judul;
        this.tanggal = tanggal;
        this.deskripsi = deskripsi;
    }

    // Getter and Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }

    public String getTanggal() { return tanggal; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
}
