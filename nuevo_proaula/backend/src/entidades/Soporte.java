package entidades;

import java.io.Serializable;

import utils.FormateadorFecha;

public class Soporte implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idTicket;
    private String usuario;
    private String correo;
    private String asunto;
    private String mensaje;
    private String fecha;
    private String estado;
    private String observacion;
    private String fechaInicio;
    private String fechaCierre;
    private String atendidoPor;
    private String tipoTicket;
    private String empresaId;
    private String remitenteId;
    private String remitenteRol;

    public Soporte() {
        this.fecha = FormateadorFecha.fechaActualTexto();
        this.estado = "PENDIENTE";
        this.observacion = "";
        this.fechaInicio = "";
        this.fechaCierre = "";
        this.atendidoPor = "";
        this.tipoTicket = "CLIENTE";
        this.empresaId = "EMP-001";
        this.remitenteId = "";
        this.remitenteRol = "CLIENTE";
    }

    public Soporte(String usuario, String asunto, String mensaje) {
        this();
        this.usuario = usuario != null ? usuario.trim() : "";
        this.asunto = asunto != null ? asunto.trim() : "";
        this.mensaje = mensaje != null ? mensaje.trim() : "";
        this.correo = "";
    }

    public Soporte(int idTicket, String usuario, String correo, String asunto, String mensaje) {
        this();
        this.idTicket = idTicket;
        this.usuario = usuario != null ? usuario.trim() : "";
        this.correo = correo != null ? correo.trim().toLowerCase() : "";
        this.asunto = asunto != null ? asunto.trim() : "";
        this.mensaje = mensaje != null ? mensaje.trim() : "";
    }

    public Soporte(int idTicket, String usuario, String correo, String asunto, String mensaje, String fecha, String estado) {
        this(idTicket, usuario, correo, asunto, mensaje);
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

    public String getObservacion() {
        return observacion != null ? observacion : "";
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion != null ? observacion.trim() : "";
    }

    public String getFechaInicio() {
        return fechaInicio != null ? fechaInicio : "";
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio != null ? fechaInicio.trim() : "";
    }

    public String getFechaCierre() {
        return fechaCierre != null ? fechaCierre : "";
    }

    public void setFechaCierre(String fechaCierre) {
        this.fechaCierre = fechaCierre != null ? fechaCierre.trim() : "";
    }

    public String getAtendidoPor() {
        return atendidoPor != null ? atendidoPor : "";
    }

    public void setAtendidoPor(String atendidoPor) {
        this.atendidoPor = atendidoPor != null ? atendidoPor.trim() : "";
    }

    public String getTipoTicket() {
        return (tipoTicket != null && !tipoTicket.trim().isEmpty()) ? tipoTicket : "CLIENTE";
    }

    public void setTipoTicket(String tipoTicket) {
        this.tipoTicket = (tipoTicket != null && !tipoTicket.trim().isEmpty()) ? tipoTicket.trim().toUpperCase() : "CLIENTE";
    }

    public String getEmpresaId() {
        return (empresaId != null && !empresaId.trim().isEmpty()) ? empresaId : "EMP-001";
    }

    public void setEmpresaId(String empresaId) {
        this.empresaId = (empresaId != null && !empresaId.trim().isEmpty()) ? empresaId.trim() : "EMP-001";
    }

    public String getRemitenteId() {
        return remitenteId != null ? remitenteId : "";
    }

    public void setRemitenteId(String remitenteId) {
        this.remitenteId = remitenteId != null ? remitenteId.trim() : "";
    }

    public String getRemitenteRol() {
        return (remitenteRol != null && !remitenteRol.trim().isEmpty()) ? remitenteRol : "CLIENTE";
    }

    public void setRemitenteRol(String remitenteRol) {
        this.remitenteRol = (remitenteRol != null && !remitenteRol.trim().isEmpty()) ? remitenteRol.trim().toUpperCase() : "CLIENTE";
    }

    @Override
    public String toString() {
        return "#" + idTicket + " [" + estado + "] " + usuario + " - Asunto: " + asunto + " (" + fecha + ")";
    }
}
