package entidades;

import utils.FormateadorFecha;

/**
 * Entidad que representa un ticket o solicitud de soporte técnico.
 */
public class Soporte {

    private int idTicket;
    private String usuario; // Identificación o nombre del usuario que radica
    private String correo;
    private String asunto;
    private String mensaje;
    private String fecha;
    private String estado; // PENDIENTE, EN_PROCESO, RESUELTO

    public Soporte() {
        this.fecha = FormateadorFecha.fechaActualTexto();
        this.estado = "PENDIENTE";
    }

    public Soporte(String usuario, String asunto, String mensaje) {
        this.usuario = usuario != null ? usuario.trim() : "";
        this.asunto = asunto != null ? asunto.trim() : "";
        this.mensaje = mensaje != null ? mensaje.trim() : "";
        this.correo = "";
        this.fecha = FormateadorFecha.fechaActualTexto();
        this.estado = "PENDIENTE";
    }

    public Soporte(int idTicket, String usuario, String correo, String asunto, String mensaje) {
        this.idTicket = idTicket;
        this.usuario = usuario != null ? usuario.trim() : "";
        this.correo = correo != null ? correo.trim().toLowerCase() : "";
        this.asunto = asunto != null ? asunto.trim() : "";
        this.mensaje = mensaje != null ? mensaje.trim() : "";
        this.fecha = FormateadorFecha.fechaActualTexto();
        this.estado = "PENDIENTE";
    }

    public Soporte(int idTicket, String usuario, String correo, String asunto, String mensaje, String fecha, String estado) {
        this.idTicket = idTicket;
        this.usuario = usuario != null ? usuario.trim() : "";
        this.correo = correo != null ? correo.trim().toLowerCase() : "";
        this.asunto = asunto != null ? asunto.trim() : "";
        this.mensaje = mensaje != null ? mensaje.trim() : "";
        this.fecha = fecha != null ? fecha : FormateadorFecha.fechaActualTexto();
        this.estado = estado != null ? estado : "PENDIENTE";
    }

    public int getIdTicket() {
        return idTicket;
    }

    public void setIdTicket(int idTicket) {
        this.idTicket = idTicket;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    // Alias compatible con el proyecto anterior
    public String getDescripcion() {
        return mensaje;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "#" + idTicket + " [" + estado + "] " + usuario + " - Asunto: " + asunto + " (" + fecha + ")";
    }
}
