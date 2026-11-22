package controladores;

import entidades.Soporte;
import modelos.NotificacionModel;
import modelos.SoporteModel;
import utils.Respuesta;
import java.util.List;

public class SoporteController {

    private final SoporteModel soporteModel;
    private final NotificacionModel notificacionModel;
    private static final SoporteModel sharedSoporteModel = new SoporteModel();

    public SoporteController() {
        this(sharedSoporteModel, NotificacionController.getSharedNotificacionModel());
    }

    public SoporteController(SoporteModel soporteModel) {
        this(soporteModel, NotificacionController.getSharedNotificacionModel());
    }

    public SoporteController(SoporteModel soporteModel, NotificacionModel notificacionModel) {
        this.soporteModel = (soporteModel != null) ? soporteModel : sharedSoporteModel;
        this.notificacionModel = (notificacionModel != null)
                ? notificacionModel
                : NotificacionController.getSharedNotificacionModel();
    }

    public static SoporteModel getSharedSoporteModel() {
        return sharedSoporteModel;
    }

    public static NotificacionModel getSharedNotificacionModel() {
        return NotificacionController.getSharedNotificacionModel();
    }

    public Respuesta<Soporte> radicarTicket(String usuario, String correo, String asunto, String mensaje, boolean notificarEmail) {
        Soporte ticket = new Soporte();
        ticket.setUsuario(usuario);
        ticket.setCorreo(correo);
        ticket.setAsunto(asunto);
        ticket.setMensaje(mensaje);
        return soporteModel.radicarTicket(ticket, notificarEmail);
    }

    public Respuesta<Soporte> radicarTicket(String usuario, String remitenteId, String remitenteRol,
                                            String correo, String asunto, String mensaje,
                                            String tipoTicket, String empresaId, boolean notificarEmail) {
        Soporte ticket = new Soporte();
        ticket.setUsuario(usuario);
        ticket.setRemitenteId(remitenteId);
        ticket.setRemitenteRol(remitenteRol);
        ticket.setCorreo(correo);
        ticket.setAsunto(asunto);
        ticket.setMensaje(mensaje);
        ticket.setTipoTicket(tipoTicket);
        ticket.setEmpresaId(empresaId);
        return soporteModel.radicarTicket(ticket, notificarEmail);
    }

    public Respuesta<Soporte> atenderSiguienteTicket() {
        return soporteModel.atenderSiguienteTicket();
    }

    public Respuesta<Soporte> iniciarSiguienteTicket(String tipoTicket, String empresaId, String agenteNombre) {
        return soporteModel.iniciarSiguienteTicket(tipoTicket, empresaId, agenteNombre);
    }

    public Respuesta<Soporte> cerrarTicket(int idTicket, String observacion, String agenteNombre) {
        return soporteModel.cerrarTicket(idTicket, observacion, agenteNombre, notificacionModel);
    }

    public Respuesta<Soporte> consultarSiguienteTicket() {
        return soporteModel.consultarSiguiente();
    }

    public Respuesta<Soporte> consultarSiguienteTicket(String tipoTicket, String empresaId) {
        return soporteModel.consultarSiguiente(tipoTicket, empresaId);
    }

    public Respuesta<List<Soporte>> listarTicketsPendientes() {
        return soporteModel.listarTicketsPendientes();
    }

    public Respuesta<List<Soporte>> listarPendientesPorFiltro(String tipoTicket, String empresaId) {
        List<Soporte> lista = soporteModel.listarPendientesFiltrados(tipoTicket, empresaId);
        return Respuesta.exito("Tickets pendientes: " + lista.size(), lista);
    }

    public Respuesta<List<Soporte>> listarEnProcesoPorFiltro(String tipoTicket, String empresaId) {
        List<Soporte> lista = soporteModel.listarEnProcesoFiltrados(tipoTicket, empresaId);
        return Respuesta.exito("Tickets en proceso: " + lista.size(), lista);
    }

    public Respuesta<List<Soporte>> listarCerradosPorFiltro(String tipoTicket, String empresaId) {
        List<Soporte> lista = soporteModel.listarCerradosFiltrados(tipoTicket, empresaId);
        return Respuesta.exito("Tickets cerrados: " + lista.size(), lista);
    }

    public Respuesta<Soporte> buscarTicketPorId(int idTicket) {
        Soporte s = soporteModel.buscarPorIdEnCualquierEstado(idTicket);
        if (s == null) {
            return Respuesta.error("Ticket #" + idTicket + " no encontrado.", 404);
        }
        return Respuesta.exito("Ticket encontrado.", s);
    }

    public List<Soporte> listarPendientesFiltrados(String tipoTicket, String empresaId) {
        return soporteModel.listarPendientesFiltrados(tipoTicket, empresaId);
    }

    public List<Soporte> listarEnProcesoFiltrados(String tipoTicket, String empresaId) {
        return soporteModel.listarEnProcesoFiltrados(tipoTicket, empresaId);
    }

    public List<Soporte> listarCerradosFiltrados(String tipoTicket, String empresaId) {
        return soporteModel.listarCerradosFiltrados(tipoTicket, empresaId);
    }

    public Soporte buscarTicket(int idTicket) {
        return soporteModel.buscarPorIdEnCualquierEstado(idTicket);
    }
}
