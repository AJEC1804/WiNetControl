package entidades;

import java.io.Serializable;
import utils.FormateadorFecha;

public class Notificacion implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idNotificacion;
    private String destinatarioId;
    private String destinatarioCorreo;
    private String titulo;
    private String mensaje;
    private String tipo;
    private int idReferencia;
    private String fecha;
    private boolean leida;

    public Notificacion() {
        this.fecha = FormateadorFecha.fechaActualTexto();
        this.leida = false;
    }

    public Notificacion(int idNotificacion, String destinatarioId, String destinatarioCorreo,
                        String titulo, String mensaje, String tipo, int idReferencia) {
        this.idNotificacion = idNotificacion;
        this.destinatarioId = destinatarioId != null ? destinatarioId.trim() : "";
        this.destinatarioCorreo = destinatarioCorreo != null ? destinatarioCorreo.trim().toLowerCase() : "";
        this.titulo = titulo != null ? titulo.trim() : "";
        this.mensaje = mensaje != null ? mensaje.trim() : "";
        this.tipo = tipo != null ? tipo.trim() : "INFO";
        this.idReferencia = idReferencia;
        this.fecha = FormateadorFecha.fechaActualTexto();
        this.leida = false;
    }

    public int getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(int idNotificacion) { this.idNotificacion = idNotificacion; }

    public String getDestinatarioId() { return destinatarioId != null ? destinatarioId : ""; }
    public void setDestinatarioId(String destinatarioId) { this.destinatarioId = destinatarioId != null ? destinatarioId.trim() : ""; }

    public String getDestinatarioCorreo() { return destinatarioCorreo != null ? destinatarioCorreo : ""; }
    public void setDestinatarioCorreo(String destinatarioCorreo) { this.destinatarioCorreo = destinatarioCorreo != null ? destinatarioCorreo.trim().toLowerCase() : ""; }

    public String getTitulo() { return titulo != null ? titulo : ""; }
    public void setTitulo(String titulo) { this.titulo = titulo != null ? titulo.trim() : ""; }

    public String getMensaje() { return mensaje != null ? mensaje : ""; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje != null ? mensaje.trim() : ""; }

    public String getTipo() { return tipo != null ? tipo : "INFO"; }
    public void setTipo(String tipo) { this.tipo = tipo != null ? tipo.trim() : "INFO"; }

    public int getIdReferencia() { return idReferencia; }
    public void setIdReferencia(int idReferencia) { this.idReferencia = idReferencia; }

    public String getFecha() { return fecha != null ? fecha : ""; }
    public void setFecha(String fecha) { this.fecha = fecha != null ? fecha.trim() : ""; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
}
