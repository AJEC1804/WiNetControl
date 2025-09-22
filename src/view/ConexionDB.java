/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author Jarvi
 */
public class ConexionDB {
   // Datos de conexión - AJUSTA según tu MySQL en XAMPP
    private static final String DB_URL = "jdbc:mysql://localhost:3306/winetdb?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";   // usuario XAMPP
    private static final String DB_PASS = "";       // contraseña (vacía por defecto en XAMPP)

    // Método estático que devuelve una conexión lista para usar
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (SQLException e) {
            System.err.println("❌ Error de conexión a la base de datos: " + e.getMessage());
            throw e;
        }
    }  
}
