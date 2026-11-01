package controladores;

import entidades.Soporte;
import modelos.SoporteModel;
import utils.Respuesta;
import java.util.List;

/**
 * Controlador para la gestión de Tickets de Soporte Técnico.
 * Coordina el encolamiento y atención de usuarios respetando el principio FIFO.
 */
public class SoporteController {

    private final SoporteModel soporteModel;
    private static SoporteModel sharedSoporteModel = new SoporteModel();

    public SoporteController() {
        this(sharedSoporteModel);
    }

    public SoporteController(SoporteModel soporteModel) {
        this.soporteModel = (soporteModel != null) ? soporteModel : sharedSoporteModel;
    }

    public static SoporteModel getSharedSoporteModel() {
        return sharedSoporteModel;
    }

    /**
     * Endpoint para radicar un nuevo ticket de soporte y encolarlo al final.
     */
    public Respuesta<Soporte> radicarTicket(String usuario, String correo, String asunto, String mensaje, boolean notificarEmail) {
        Soporte ticket = new Soporte();
        ticket.setUsuario(usuario);
        ticket.setCorreo(correo);
        ticket.setAsunto(asunto);
        ticket.setMensaje(mensaje);

        return soporteModel.radicarTicket(ticket, notificarEmail);
    }

    /**
     * Endpoint para atender (desencolar) el siguiente ticket del frente.
     */
    public Respuesta<Soporte> atenderSiguienteTicket() {
        return soporteModel.atenderSiguienteTicket();
    }

    /**
     * Endpoint para consultar el siguiente ticket sin retirarlo de la cola.
     */
    public Respuesta<Soporte> consultarSiguienteTicket() {
        return soporteModel.consultarSiguiente();
    }

    /**
     * Endpoint para obtener la lista de todos los tickets en espera.
     */
    public Respuesta<List<Soporte>> listarTicketsPendientes() {
        return soporteModel.listarTicketsPendientes();
    }
}
