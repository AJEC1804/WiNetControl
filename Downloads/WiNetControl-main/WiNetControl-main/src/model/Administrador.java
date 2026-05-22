/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Gyanela Meza
 */
public class Administrador extends Usuario {

    public Administrador(String nombre, String apellido, String tipo, String identificacion,
            String telefono, String correo, String direccion, String contrasena) {
        super(nombre, apellido, tipo, identificacion, telefono, correo, direccion, contrasena);
    }

    @Override
    public String getTipoUsuario() {
        return "Administrador";
    }
}


