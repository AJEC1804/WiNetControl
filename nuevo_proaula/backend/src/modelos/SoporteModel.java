package modelos;

import entidades.Soporte;
import metodos.ColaSoporte;
import utils.EmailUtil;
import utils.Respuesta;
import utils.ValidadorTexto;
import java.util.List;

/**
 * Modelo de Negocio para Soporte Técnico.
 * Administra el encolamiento y atención FIFO de tickets y solicitudes de usuarios.
 */
public class SoporteModel {

    private final ColaSoporte colaSoporte;
    private int consecutivoTicket;

    public SoporteModel() {
        this.colaSoporte = new ColaSoporte();
        this.consecutivoTicket = 1;
    }

    public SoporteModel(ColaSoporte colaSoporte) {
        this.colaSoporte = colaSoporte;
        this.consecutivoTicket = 1;
    }

    public ColaSoporte getColaSoporte() {
        return colaSoporte;
    }

    /**
     * Radica un nuevo ticket de soporte y lo encola al final (FIFO).
     */
    public Respuesta<Soporte> radicarTicket(Soporte ticket, boolean notificarPorEmail) {
        if (ticket == null) {
            return Respuesta.error("Los datos de soporte no pueden ser nulos.");
        }

        if (!ValidadorTexto.esNoVacio(ticket.getUsuario())) {
            return Respuesta.error("El usuario o cliente solicitante es obligatorio.");
        }

        if (!ValidadorTexto.esNoVacio(ticket.getAsunto())) {
            return Respuesta.error("El asunto del requerimiento es obligatorio.");
        }

        if (!ValidadorTexto.esNoVacio(ticket.getMensaje())) {
            return Respuesta.error("La descripción del problema es obligatoria.");
        }

        ticket.setIdTicket(consecutivoTicket++);
        ticket.setEstado("PENDIENTE");

        // Encolar al final
        colaSoporte.encolar(ticket);

        // Envío de email si corresponde
        if (notificarPorEmail && ValidadorTexto.esNoVacio(ticket.getCorreo())) {
            String cuerpo = "Hola " + ticket.getUsuario() + ",\n\n" +
                    "Hemos recibido tu ticket #" + ticket.getIdTicket() + ": " + ticket.getAsunto() + "\n" +
                    "Detalle: " + ticket.getMensaje() + "\n\n" +
                    "Pronto uno de nuestros asesores atenderá tu solicitud.\nWiNet Control Support";
            EmailUtil.enviarCorreo(ticket.getCorreo(), "Recepción Ticket #" + ticket.getIdTicket() + " - " + ticket.getAsunto(), cuerpo);
        }

        return Respuesta.exito("Ticket #" + ticket.getIdTicket() + " radicado y encolado correctamente.", ticket);
    }

    /**
     * Atiende y desencola el siguiente ticket en espera (Frente de la cola).
     */
    public Respuesta<Soporte> atenderSiguienteTicket() {
        Soporte ticket = colaSoporte.desencolar();
        if (ticket != null) {
            ticket.setEstado("ATENDIDO");
            return Respuesta.exito("Ticket #" + ticket.getIdTicket() + " atendido del frente de la cola.", ticket);
        }
        return Respuesta.error("No hay tickets pendientes en la cola de soporte.", 404);
    }

    /**
     * Consulta el siguiente ticket a ser atendido sin retirarlo de la cola.
     */
    public Respuesta<Soporte> consultarSiguiente() {
        Soporte ticket = colaSoporte.getFrente();
        if (ticket != null) {
            return Respuesta.exito("Siguiente ticket en espera obtenido.", ticket);
        }
        return Respuesta.error("No hay tickets pendientes en la cola.", 404);
    }

    /**
     * Listado de todos los tickets en espera.
     */
    public Respuesta<List<Soporte>> listarTicketsPendientes() {
        List<Soporte> pendientes = colaSoporte.listarPendientes();
        return Respuesta.exito("Tickets pendientes en cola: " + pendientes.size(), pendientes);
    }
}
