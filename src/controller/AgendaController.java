package controller;

import database.DatabaseConnection;
import model.Agenda;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AgendaController {

    // CREATE
    public void createAgenda(Agenda agenda) throws SQLException {
        String query = "INSERT INTO agenda (judul, tanggal, deskripsi) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, agenda.getJudul());
            stmt.setString(2, agenda.getTanggal());
            stmt.setString(3, agenda.getDeskripsi());
            stmt.executeUpdate();
        }
    }

    // READ
    public List<Agenda> getAllAgenda() throws SQLException {
        List<Agenda> agendas = new ArrayList<>();
        String query = "SELECT * FROM agenda";

        try (Connection conn = DatabaseConnection.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Agenda agenda = new Agenda(
                        rs.getInt("id"),
                        rs.getString("judul"),
                        rs.getString("tanggal"),
                        rs.getString("deskripsi")
                );
                agendas.add(agenda);
            }
        }
        return agendas;
    }

    // UPDATE
    public void updateAgenda(Agenda agenda) throws SQLException {
        String query = "UPDATE agenda SET judul = ?, tanggal = ?, deskripsi = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, agenda.getJudul());
            stmt.setString(2, agenda.getTanggal());
            stmt.setString(3, agenda.getDeskripsi());
            stmt.setInt(4, agenda.getId());
            stmt.executeUpdate();
        }
    }

    // DELETE
    public void deleteAgenda(int id) throws SQLException {
        String query = "DELETE FROM agenda WHERE id = ?";
        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Agenda> searchAgenda(String keyword) throws SQLException {
        List<Agenda> agendas = new ArrayList<>();
        String query = "SELECT * FROM agenda WHERE judul LIKE ? OR deskripsi LIKE ?";

        try (Connection conn = DatabaseConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Agenda agenda = new Agenda(
                        rs.getInt("id"),
                        rs.getString("judul"),
                        rs.getString("tanggal"),
                        rs.getString("deskripsi")
                );
                agendas.add(agenda);
            }
        }
        return agendas;
    }

}
