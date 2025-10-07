/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;
import java.sql.Connection;
/**
 *
 * @author Jarvi
 */
public class TestConexion {
    public static void main(String[] args) {
        try (Connection con = ConexionDB.getConnection()) {
            System.out.println("✅ Conexión establecida correctamente a MySQL.");
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }
}
