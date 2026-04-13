/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Gyanela Meza
 */
public class Administrador {
    private String nombre;
    private String apellido;
    private String tipo;
    private String identificacion;
    private String telefono;
    private String correo;
    private String direccion;
    private String contrasena;

    public Administrador(String nombre, String apellido, String tipo, String identificacion,
            String telefono, String correo, String direccion, String contrasena) {
        setNombre(nombre);
        setApellido(apellido);
        setTipo(tipo);
        setIdentificacion(identificacion);
        setTelefono(telefono);
        setCorreo(correo);
        setDireccion(direccion);
        setContrasena(contrasena);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            this.nombre = nombre.trim();
        } else {
            this.nombre = "Sin nombre";
        }
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        if (apellido != null && !apellido.trim().isEmpty()) {
            this.apellido = apellido.trim();
        } else {
            this.apellido = "Sin apellido";
        }
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        if (tipo != null && !tipo.trim().isEmpty()) {
            this.tipo = tipo.trim();
        } else {
            this.tipo = "No definido";
        }
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        if (identificacion != null && identificacion.trim().length() >= 8
                && identificacion.trim().length() <= 10
                && identificacion.trim().matches("\\d+")) {
            this.identificacion = identificacion.trim();
        } else {
            this.identificacion = "00000000";
        }
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        if (telefono != null && telefono.trim().length() == 10
                && telefono.trim().matches("\\d+")) {
            this.telefono = telefono.trim();
        } else {
            this.telefono = "0000000000";
        }
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        if (correo != null && correo.trim().contains("@") && correo.trim().contains(".")) {
            this.correo = correo.trim();
        } else {
            this.correo = "sin-correo@correo.com";
        }
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        if (direccion != null && !direccion.trim().isEmpty()) {
            this.direccion = direccion.trim();
        } else {
            this.direccion = "Sin direccion";
        }
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        if (contrasena != null && contrasena.trim().length() >= 6
                && contrasena.trim().matches(".*[A-Za-z].*")
                && contrasena.trim().matches(".*\\d.*")) {
            this.contrasena = contrasena.trim();
        } else {
            this.contrasena = "abc123";
        }
    }

    @Override
    public String toString() {
        return nombre + " " + apellido + " - " + tipo + "." + identificacion;
    }
}


