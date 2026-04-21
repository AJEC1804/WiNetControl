package model;

public class Usuario {
    protected String nombre;
    protected String apellido;
    protected String tipo;
    protected String identificacion;
    protected String telefono;
    protected String correo;
    protected String direccion;
    protected String contrasena;

    public Usuario(String nombre, String apellido, String tipo, String identificacion,
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

    public final void setNombre(String nombre) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            this.nombre = nombre.trim();
        } else {
            this.nombre = "Sin nombre";
        }
    }

    public String getApellido() {
        return apellido;
    }

    public final void setApellido(String apellido) {
        if (apellido != null && !apellido.trim().isEmpty()) {
            this.apellido = apellido.trim();
        } else {
            this.apellido = "Sin apellido";
        }
    }

    public String getTipo() {
        return tipo;
    }

    public final void setTipo(String tipo) {
        if (tipo != null && !tipo.trim().isEmpty()) {
            this.tipo = tipo.trim();
        } else {
            this.tipo = "No definido";
        }
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public final void setIdentificacion(String identificacion) {
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

    public final void setTelefono(String telefono) {
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

    public final void setCorreo(String correo) {
        if (correo != null && correo.trim().contains("@") && correo.trim().contains(".")) {
            this.correo = correo.trim();
        } else {
            this.correo = "sin-correo@correo.com";
        }
    }

    public String getDireccion() {
        return direccion;
    }

    public final void setDireccion(String direccion) {
        if (direccion != null && !direccion.trim().isEmpty()) {
            this.direccion = direccion.trim();
        } else {
            this.direccion = "Sin direccion";
        }
    }

    public String getContrasena() {
        return contrasena;
    }

    public final void setContrasena(String contrasena) {
        if (contrasena != null && contrasena.trim().length() >= 6
                && contrasena.trim().matches(".*[A-Za-z].*")
                && contrasena.trim().matches(".*\\d.*")) {
            this.contrasena = contrasena.trim();
        } else {
            this.contrasena = "abc123";
        }
    }

    public String getTipoUsuario() {
        return "Usuario";
    }

    @Override
    public String toString() {
        return nombre + " " + apellido + " - " + tipo + "." + identificacion;
    }
}