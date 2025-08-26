package com.mycompany.crud;

import java.sql.*;
import java.util.*;

public class AgendaDB {
    // Datos de conexión a la base de datos
    private static final String URL = "jdbc:mariadb://localhost:3306/agenda";
    private static final String USER = "usuario1";
    private static final String PASSWORD = "superpassword";

    public static List<Persona> obtenerPersonas() {
        List<Persona> personas = new ArrayList<>();

        String queryPersonas = "SELECT id, nombre, direccion FROM Personas";
        String queryTelefonos = "SELECT telefono FROM Telefonos WHERE personaId = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement psPersonas = conn.prepareStatement(queryPersonas);
             ResultSet rsPersonas = psPersonas.executeQuery()) {

            while (rsPersonas.next()) {
                int id = rsPersonas.getInt("id");
                String nombre = rsPersonas.getString("nombre");
                String direccion = rsPersonas.getString("direccion");

                List<String> telefonos = new ArrayList<>();
                try (PreparedStatement psTel = conn.prepareStatement(queryTelefonos)) {
                    psTel.setInt(1, id);
                    try (ResultSet rsTel = psTel.executeQuery()) {
                        while (rsTel.next()) {
                            telefonos.add(rsTel.getString("telefono"));
                        }
                    }
                }

                Persona persona = new Persona(id, nombre, direccion, telefonos);
                personas.add(persona);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return personas;
    }

    public static void insertarPersona(String nombre, List<String> direcciones, List<String> telefonos) {
        String direccionUnida = String.join(", ", direcciones);

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);

            String sqlPersona = "INSERT INTO Personas (nombre, direccion) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlPersona, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, nombre);
                stmt.setString(2, direccionUnida);
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
                        if (tel.trim().isEmpty()) continue;
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

    public static boolean eliminarPersona(int id) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "DELETE FROM Personas WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int filas = stmt.executeUpdate();
                return filas > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error al eliminar: " + e.getMessage());
            return false;
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
    
    public static boolean modificarPersonaCompleta(int id, String nuevoNombre, String nuevaDireccion, List<String> nuevosTelefonos) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);

            String sqlPers = "UPDATE Personas SET nombre = ?, direccion = ? WHERE id = ?";
            try (PreparedStatement stmtPers = conn.prepareStatement(sqlPers)) {
                stmtPers.setString(1, nuevoNombre);
                stmtPers.setString(2, nuevaDireccion);
                stmtPers.setInt(3, id);
                stmtPers.executeUpdate();
            }

            String sqlDelTel = "DELETE FROM Telefonos WHERE personaId = ?";
            try (PreparedStatement stmtDel = conn.prepareStatement(sqlDelTel)) {
                stmtDel.setInt(1, id);
                stmtDel.executeUpdate();
            }

            String sqlInsTel = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";
            try (PreparedStatement stmtIns = conn.prepareStatement(sqlInsTel)) {
                for (String tel : nuevosTelefonos) {
                    stmtIns.setInt(1, id);
                    stmtIns.setString(2, tel);
                    stmtIns.addBatch();
                }
                stmtIns.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static Persona obtenerPersona(int id) {
       Persona persona = null;
        String sqlPersona = "SELECT nombre, direccion FROM Personas WHERE id = ?";
        String sqlTelefonos = "SELECT telefono FROM Telefonos WHERE personaId = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement psPers = conn.prepareStatement(sqlPersona)) {

            psPers.setInt(1, id);
            try (ResultSet rs = psPers.executeQuery()) {
                if (rs.next()) {
                    String nombre = rs.getString("nombre");
                    String direccion = rs.getString("direccion");

                    List<String> telefonos = new ArrayList<>();
                    try (PreparedStatement psTel = conn.prepareStatement(sqlTelefonos)) {
                        psTel.setInt(1, id);
                        try (ResultSet rsTel = psTel.executeQuery()) {
                            while (rsTel.next()) {
                                telefonos.add(rsTel.getString("telefono"));
                            }
                        }
                    }
                    
                    persona = new Persona(id, nombre, direccion, telefonos);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return persona;
    }
}
