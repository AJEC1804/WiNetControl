package modelos;

import entidades.Notificacion;
import utils.PersistenciaUtil;
import utils.Respuesta;
import utils.ValidadorTexto;

import java.util.ArrayList;
import java.util.List;

public class NotificacionModel {

    private List<Notificacion> notificaciones;
    private int consecutivo = 1;
    private static final String ARCHIVO = "notificaciones.dat";

    public NotificacionModel() {
        this.notificaciones = new ArrayList<>();
        cargarEstado();
    }

    public Notificacion crearNotificacion(String destinatarioId, String destinatarioCorreo,
                                          String titulo, String mensaje, String tipo, int idReferencia) {
        Notificacion n = new Notificacion(
                consecutivo++,
                destinatarioId,
                destinatarioCorreo,
                titulo,
                mensaje,
                tipo,
                idReferencia
        );
        notificaciones.add(0, n);
        return n;
    }

    public List<Notificacion> listarParaUsuario(String identificacion, String correo, String nombreCompleto) {
        List<Notificacion> resultado = new ArrayList<>();
        for (Notificacion n : notificaciones) {
            if (n == null) continue;
            boolean porId = ValidadorTexto.esNoVacio(identificacion)
                    && n.getDestinatarioId().equalsIgnoreCase(identificacion.trim());
            boolean porNombre = ValidadorTexto.esNoVacio(nombreCompleto)
                    && n.getDestinatarioId().equalsIgnoreCase(nombreCompleto.trim());
            boolean porCorreo = ValidadorTexto.esNoVacio(correo)
                    && !n.getDestinatarioCorreo().isEmpty()
                    && n.getDestinatarioCorreo().equalsIgnoreCase(correo.trim());
            if (porId || porNombre || porCorreo) {
                resultado.add(n);
            }
        }
        return resultado;
    }

    public Respuesta<Notificacion> marcarLeida(int idNotificacion, String identificacion, String correo, String nombreCompleto) {
        for (Notificacion n : notificaciones) {
            if (n != null && n.getIdNotificacion() == idNotificacion) {
                boolean esMia = (ValidadorTexto.esNoVacio(identificacion) && n.getDestinatarioId().equalsIgnoreCase(identificacion.trim()))
                        || (ValidadorTexto.esNoVacio(nombreCompleto) && n.getDestinatarioId().equalsIgnoreCase(nombreCompleto.trim()))
                        || (ValidadorTexto.esNoVacio(correo) && n.getDestinatarioCorreo().equalsIgnoreCase(correo.trim()));
                if (!esMia) {
                    return Respuesta.error("No puedes modificar notificaciones de otro usuario.", 403);
                }
                n.setLeida(true);
                return Respuesta.exito("Notificación marcada como leída.", n);
            }
        }
        return Respuesta.error("Notificación no encontrada.", 404);
    }

    public void marcarTodasLeidas(String identificacion, String correo, String nombreCompleto) {
        for (Notificacion n : listarParaUsuario(identificacion, correo, nombreCompleto)) {
            n.setLeida(true);
        }
    }

    public void guardarEstado() {
        PersistenciaUtil.guardar(new Object[]{new ArrayList<>(notificaciones), Integer.valueOf(consecutivo)}, ARCHIVO);
    }

    @SuppressWarnings("unchecked")
    private void cargarEstado() {
        Object[] estado = PersistenciaUtil.cargar(ARCHIVO, Object[].class);
        if (estado == null || estado.length < 2) return;
        if (estado[0] instanceof List) {
            notificaciones = new ArrayList<>((List<Notificacion>) estado[0]);
        }
        if (estado[1] instanceof Integer) {
            consecutivo = ((Integer) estado[1]).intValue();
        }
    }
}
