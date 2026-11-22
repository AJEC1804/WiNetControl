package controladores;

import entidades.Notificacion;
import modelos.NotificacionModel;
import utils.Respuesta;
import utils.ValidadorTexto;

import java.util.List;

public class NotificacionController {

    private final NotificacionModel notificacionModel;
    private static final NotificacionModel sharedNotificacionModel = new NotificacionModel();

    public NotificacionController() {
        this(sharedNotificacionModel);
    }

    public NotificacionController(NotificacionModel notificacionModel) {
        this.notificacionModel = (notificacionModel != null) ? notificacionModel : sharedNotificacionModel;
    }

    public static NotificacionModel getSharedNotificacionModel() {
        return sharedNotificacionModel;
    }

    public Respuesta<Notificacion> crearNotificacion(String destinatarioId, String destinatarioCorreo,
                                                     String titulo, String mensaje, String tipo, int idReferencia) {
        if (!ValidadorTexto.esNoVacio(titulo) || !ValidadorTexto.esNoVacio(mensaje)) {
            return Respuesta.error("El título y mensaje de la notificación son obligatorios.", 400);
        }
        Notificacion creada = notificacionModel.crearNotificacion(
                destinatarioId, destinatarioCorreo, titulo, mensaje, tipo, idReferencia
        );
        return Respuesta.exito("Notificación registrada.", creada);
    }

    public Respuesta<List<Notificacion>> listarParaUsuario(String identificacion, String correo, String nombreCompleto) {
        List<Notificacion> lista = notificacionModel.listarParaUsuario(identificacion, correo, nombreCompleto);
        return Respuesta.exito("Notificaciones encontradas: " + lista.size(), lista);
    }

    public Respuesta<Notificacion> marcarLeida(int idNotificacion, String identificacion, String correo, String nombreCompleto) {
        if (idNotificacion <= 0) {
            return Respuesta.error("ID de notificación inválido.", 400);
        }
        return notificacionModel.marcarLeida(idNotificacion, identificacion, correo, nombreCompleto);
    }

    public Respuesta<Boolean> marcarTodasLeidas(String identificacion, String correo, String nombreCompleto) {
        notificacionModel.marcarTodasLeidas(identificacion, correo, nombreCompleto);
        return Respuesta.exito("Todas las notificaciones fueron marcadas como leídas.", Boolean.TRUE);
    }
}
