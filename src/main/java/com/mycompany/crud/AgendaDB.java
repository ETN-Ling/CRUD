package com.mycompany.crud;

import java.sql.*;
import java.util.List;

public class AgendaDB {
    // Datos de conexión a la base de datos
    private static final String URL = "jdbc:mariadb://localhost:3306/agenda";
    private static final String USER = "usuario1";
    private static final String PASSWORD = "superpassword";

    public static void mostrarPersonas() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Personas")) {

            System.out.println("\n=== LISTADO DE PERSONAS ===");
            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String direccion = rs.getString("direccion");

                System.out.println("ID: " + id + ", Nombre: " + nombre + ", Dirección: " + direccion);

                try (Statement telStmt = conn.createStatement();
                     ResultSet rsTel = telStmt.executeQuery(
                             "SELECT telefono FROM Telefonos WHERE personaId = " + id)) {

                    System.out.println("  Teléfonos:");
                    while (rsTel.next()) {
                        System.out.println("    - " + rsTel.getString("telefono"));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al consultar personas: " + e.getMessage());
        }
    }

    public static void insertarPersona(String nombre, String direccion, List<String> telefonos) {
    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
        conn.setAutoCommit(false);

        String sqlPersona = "INSERT INTO Personas (nombre, direccion) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlPersona, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nombre);
            stmt.setString(2, direccion);
            int filas = stmt.executeUpdate();

            if (filas == 0) {
                conn.rollback();
                System.out.println("No se pudo insertar la persona.");
                return;
            }

            ResultSet rs = stmt.getGeneratedKeys();
            int personaId = -1;
            if (rs.next()) {
                personaId = rs.getInt(1);
            } else {
                conn.rollback();
                System.out.println("No se pudo obtener el ID de la persona insertada.");
                return;
            }

            String sqlTelefono = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";
            try (PreparedStatement stmtTel = conn.prepareStatement(sqlTelefono)) {
                for (String tel : telefonos) {
                    stmtTel.setInt(1, personaId);
                    stmtTel.setString(2, tel);
                    stmtTel.addBatch();
                }
                stmtTel.executeBatch();
            }
            conn.commit();
            System.out.println("Persona y teléfonos insertados correctamente.");
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    } catch (SQLException e) {
        System.out.println("Error al insertar persona y teléfonos: " + e.getMessage());
    }
}

    public static void eliminarPersona(int id) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sqlTelefonos = "DELETE FROM Telefonos WHERE personaId = ?";
            try (PreparedStatement stmtTel = conn.prepareStatement(sqlTelefonos)) {
                stmtTel.setInt(1, id);
                stmtTel.executeUpdate();
            }

            String sqlPersona = "DELETE FROM Personas WHERE id = ?";
            try (PreparedStatement stmtPers = conn.prepareStatement(sqlPersona)) {
                stmtPers.setInt(1, id);
                int filas = stmtPers.executeUpdate();
                System.out.println(filas > 0 ? "Persona eliminada." : "No se encontró la persona.");
            }
        } catch (SQLException e) {
            System.out.println("Error al eliminar: " + e.getMessage());
        }
    }

    public static void modificarDatosPersona(int id, String nuevoNombre, String nuevaDireccion) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "UPDATE Personas SET nombre = ?, direccion = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nuevoNombre);
                stmt.setString(2, nuevaDireccion);
                stmt.setInt(3, id);

                int filas = stmt.executeUpdate();
                System.out.println(filas > 0 ? "Datos de persona modificados." : "No se encontró la persona.");
            }
        } catch (SQLException e) {
        System.out.println("Error al modificar persona: " + e.getMessage());
        }
    }

    public static void modificarTelefonos(int id, List<String> nuevosTelefonos) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);
            
            String sqlDeleteTel = "DELETE FROM Telefonos WHERE personaId = ?";
            try (PreparedStatement stmtDel = conn.prepareStatement(sqlDeleteTel)) {
                stmtDel.setInt(1, id);
                stmtDel.executeUpdate();
            }

            String sqlInsertTel = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";
            try (PreparedStatement stmtIns = conn.prepareStatement(sqlInsertTel)) {
                for (String tel : nuevosTelefonos) {
                    stmtIns.setInt(1, id);
                    stmtIns.setString(2, tel);
                    stmtIns.addBatch();
                }
                stmtIns.executeBatch();
            }

            conn.commit();
            System.out.println("Teléfonos modificados correctamente.");
        } catch (SQLException e) {
        System.out.println("Error al modificar teléfonos: " + e.getMessage());
        }
    }
}
