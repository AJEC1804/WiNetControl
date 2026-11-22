package modelos;

import entidades.Soporte;
import metodos.ColaSoporte;
import utils.EmailUtil;
import utils.FormateadorFecha;
import utils.PersistenciaUtil;
import utils.Respuesta;
import utils.ValidadorTexto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SoporteModel {

    private ColaSoporte colaSoporte;
    private ColaSoporte colaPlataforma;
    private List<Soporte> ticketsEnProceso;
    private List<Soporte> ticketsCerrados;

    private static final String ARCHIVO = "soporte.dat";
    private int consecutivoTicket;

    public SoporteModel() {
        this.colaSoporte = new ColaSoporte();
        this.colaPlataforma = new ColaSoporte();
        this.ticketsEnProceso = new ArrayList<>();
        this.ticketsCerrados = new ArrayList<>();
        this.consecutivoTicket = 1;
        cargarEstado();
    }

    public SoporteModel(ColaSoporte colaSoporte) {
        this.colaSoporte = colaSoporte != null ? colaSoporte : new ColaSoporte();
        this.colaPlataforma = new ColaSoporte();
        this.ticketsEnProceso = new ArrayList<>();
        this.ticketsCerrados = new ArrayList<>();
        this.consecutivoTicket = 1;
    }

    public ColaSoporte getColaSoporte() {
        return colaSoporte;
    }

    public ColaSoporte getColaPlataforma() {
        return colaPlataforma;
    }

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
        if (!ValidadorTexto.esNoVacio(ticket.getFecha())) {
            ticket.setFecha(FormateadorFecha.fechaActualTexto());
        }

        if ("PLATAFORMA".equalsIgnoreCase(ticket.getTipoTicket())) {
            ticket.setTipoTicket("PLATAFORMA");
            colaPlataforma.encolar(ticket);
        } else {
            ticket.setTipoTicket("CLIENTE");
            colaSoporte.encolar(ticket);
        }

        if (notificarPorEmail && ValidadorTexto.esNoVacio(ticket.getCorreo())) {
            final String destino = ticket.getCorreo();
            final String asuntoMail = "Recepción Ticket #" + ticket.getIdTicket() + " - " + ticket.getAsunto();
            final String cuerpo = "Hola " + ticket.getUsuario() + ",\n\n"
                    + "Hemos recibido tu ticket #" + ticket.getIdTicket() + ": " + ticket.getAsunto() + "\n"
                    + "Detalle: " + ticket.getMensaje() + "\n\n"
                    + "Pronto uno de nuestros asesores atenderá tu solicitud.\nWiNet Control Support";
            new Thread(() -> EmailUtil.enviarCorreo(destino, asuntoMail, cuerpo)).start();
        }

        return Respuesta.exito("Ticket #" + ticket.getIdTicket() + " radicado y encolado correctamente.", ticket);
    }

    public Respuesta<Soporte> atenderSiguienteTicket() {
        Soporte ticket = colaSoporte.desencolar();
        if (ticket != null) {
            ticket.setEstado("ATENDIDO");
            ticket.setFechaCierre(FormateadorFecha.fechaActualTexto());
            ticketsCerrados.add(0, ticket);
            return Respuesta.exito("Ticket #" + ticket.getIdTicket() + " atendido del frente de la cola.", ticket);
        }
        return Respuesta.error("No hay tickets pendientes en la cola de soporte.", 404);
    }

    public Respuesta<Soporte> iniciarSiguienteTicket(String tipoTicket, String empresaId, String agenteNombre) {
        boolean esPlataforma = "PLATAFORMA".equalsIgnoreCase(tipoTicket);
        Soporte ticket = esPlataforma
                ? colaPlataforma.desencolar()
                : (ValidadorTexto.esNoVacio(empresaId) && !"PLATAFORMA".equalsIgnoreCase(empresaId)
                    ? colaSoporte.desencolarPorEmpresa(empresaId)
                    : colaSoporte.desencolar());

        if (ticket == null) {
            return Respuesta.error("No hay tickets pendientes en la cola.", 404);
        }

        ticket.setEstado("EN_PROCESO");
        ticket.setFechaInicio(FormateadorFecha.fechaActualTexto());
        if (ValidadorTexto.esNoVacio(agenteNombre)) {
            ticket.setAtendidoPor(agenteNombre.trim());
        }
        ticketsEnProceso.add(0, ticket);

        return Respuesta.exito("Ticket #" + ticket.getIdTicket() + " iniciado. Revisa el problema y registra la solución.", ticket);
    }

    public Respuesta<Soporte> cerrarTicket(int idTicket, String observacion, String agenteNombre,
                                           NotificacionModel notificacionModel) {
        if (idTicket <= 0) {
            return Respuesta.error("Número de ticket inválido.", 400);
        }
        if (!ValidadorTexto.esNoVacio(observacion)) {
            return Respuesta.error("Debes escribir una observación o solución antes de cerrar el ticket.", 400);
        }

        Soporte ticket = null;
        Iterator<Soporte> it = ticketsEnProceso.iterator();
        while (it.hasNext()) {
            Soporte s = it.next();
            if (s != null && s.getIdTicket() == idTicket) {
                ticket = s;
                it.remove();
                break;
            }
        }

        if (ticket == null) {
            return Respuesta.error("El ticket #" + idTicket + " no se encuentra en proceso. Debes iniciarlo primero.", 404);
        }

        ticket.setObservacion(observacion.trim());
        ticket.setEstado("CERRADO");
        ticket.setFechaCierre(FormateadorFecha.fechaActualTexto());
        if (ValidadorTexto.esNoVacio(agenteNombre)) {
            ticket.setAtendidoPor(agenteNombre.trim());
        }
        ticketsCerrados.add(0, ticket);

        if (notificacionModel != null) {
            String destId = ValidadorTexto.esNoVacio(ticket.getRemitenteId())
                    ? ticket.getRemitenteId()
                    : ticket.getUsuario();
            notificacionModel.crearNotificacion(
                    destId,
                    ticket.getCorreo(),
                    "Ticket #" + ticket.getIdTicket() + " cerrado y resuelto",
                    "Asunto: " + ticket.getAsunto() + " | Solución: " + ticket.getObservacion(),
                    "TICKET_CERRADO",
                    ticket.getIdTicket()
            );
        }

        if (ValidadorTexto.esNoVacio(ticket.getCorreo())) {
            final String destino = ticket.getCorreo();
            final String asuntoMail = "[WiNet Control] Solución a tu Ticket #" + ticket.getIdTicket() + ": " + ticket.getAsunto();
            final String cuerpo = "Hola " + ticket.getUsuario() + ",\n\n"
                    + "Tu ticket #" + ticket.getIdTicket() + " ha sido CERRADO.\n\n"
                    + "Asunto: " + ticket.getAsunto() + "\n"
                    + "Problema reportado: " + ticket.getMensaje() + "\n\n"
                    + "Observación / Solución:\n" + ticket.getObservacion() + "\n\n"
                    + "Atendido por: " + (ValidadorTexto.esNoVacio(ticket.getAtendidoPor()) ? ticket.getAtendidoPor() : "Equipo WiNet Control") + "\n"
                    + "Fecha de cierre: " + ticket.getFechaCierre() + "\n\n"
                    + "También puedes consultar esta respuesta en las notificaciones de la plataforma web.\n"
                    + "WiNet Control";
            new Thread(() -> EmailUtil.enviarCorreo(destino, asuntoMail, cuerpo)).start();
        }

        return Respuesta.exito("Ticket #" + ticket.getIdTicket() + " cerrado y notificado al usuario.", ticket);
    }

    public Respuesta<Soporte> consultarSiguiente() {
        Soporte ticket = colaSoporte.getFrente();
        if (ticket != null) {
            return Respuesta.exito("Siguiente ticket en espera obtenido.", ticket);
        }
        return Respuesta.error("No hay tickets pendientes en la cola.", 404);
    }

    public Respuesta<Soporte> consultarSiguiente(String tipoTicket, String empresaId) {
        boolean esPlataforma = "PLATAFORMA".equalsIgnoreCase(tipoTicket);
        Soporte ticket = esPlataforma
                ? colaPlataforma.getFrente()
                : (ValidadorTexto.esNoVacio(empresaId) && !"PLATAFORMA".equalsIgnoreCase(empresaId)
                    ? colaSoporte.getFrentePorEmpresa(empresaId)
                    : colaSoporte.getFrente());
        if (ticket != null) {
            return Respuesta.exito("Siguiente ticket obtenido.", ticket);
        }
        return Respuesta.error("No hay tickets pendientes.", 404);
    }

    public Respuesta<List<Soporte>> listarTicketsPendientes() {
        List<Soporte> pendientes = colaSoporte.listarPendientes();
        return Respuesta.exito("Tickets pendientes en cola: " + pendientes.size(), pendientes);
    }

    public List<Soporte> listarPendientesFiltrados(String tipoTicket, String empresaId) {
        boolean esPlataforma = "PLATAFORMA".equalsIgnoreCase(tipoTicket);
        List<Soporte> base = esPlataforma ? colaPlataforma.listarPendientes() : colaSoporte.listarPendientes();
        return filtrarPorEmpresa(base, empresaId, esPlataforma);
    }

    public List<Soporte> listarEnProcesoFiltrados(String tipoTicket, String empresaId) {
        boolean esPlataforma = "PLATAFORMA".equalsIgnoreCase(tipoTicket);
        List<Soporte> res = new ArrayList<>();
        for (Soporte s : ticketsEnProceso) {
            if (s == null) continue;
            if (esPlataforma && !"PLATAFORMA".equalsIgnoreCase(s.getTipoTicket())) continue;
            if (!esPlataforma && "PLATAFORMA".equalsIgnoreCase(s.getTipoTicket())) continue;
            res.add(s);
        }
        return filtrarPorEmpresa(res, empresaId, esPlataforma);
    }

    public List<Soporte> listarCerradosFiltrados(String tipoTicket, String empresaId) {
        boolean esPlataforma = "PLATAFORMA".equalsIgnoreCase(tipoTicket);
        List<Soporte> res = new ArrayList<>();
        for (Soporte s : ticketsCerrados) {
            if (s == null) continue;
            if (esPlataforma && !"PLATAFORMA".equalsIgnoreCase(s.getTipoTicket())) continue;
            if (!esPlataforma && "PLATAFORMA".equalsIgnoreCase(s.getTipoTicket())) continue;
            res.add(s);
        }
        return filtrarPorEmpresa(res, empresaId, esPlataforma);
    }

    private List<Soporte> filtrarPorEmpresa(List<Soporte> origen, String empresaId, boolean esPlataforma) {
        if (!ValidadorTexto.esNoVacio(empresaId) || (esPlataforma && "PLATAFORMA".equalsIgnoreCase(empresaId))) {
            return origen;
        }
        List<Soporte> filtrados = new ArrayList<>();
        for (Soporte s : origen) {
            if (s != null && empresaId.equalsIgnoreCase(s.getEmpresaId())) {
                filtrados.add(s);
            }
        }
        return filtrados;
    }

    public Soporte buscarPorIdEnCualquierEstado(int idTicket) {
        for (Soporte s : ticketsEnProceso) {
            if (s != null && s.getIdTicket() == idTicket) return s;
        }
        for (Soporte s : ticketsCerrados) {
            if (s != null && s.getIdTicket() == idTicket) return s;
        }
        for (Soporte s : colaSoporte.listarPendientes()) {
            if (s != null && s.getIdTicket() == idTicket) return s;
        }
        for (Soporte s : colaPlataforma.listarPendientes()) {
            if (s != null && s.getIdTicket() == idTicket) return s;
        }
        return null;
    }

    public void guardarEstado() {
        PersistenciaUtil.guardar(new Object[]{
                colaSoporte,
                Integer.valueOf(consecutivoTicket),
                colaPlataforma,
                new ArrayList<>(ticketsEnProceso),
                new ArrayList<>(ticketsCerrados)
        }, ARCHIVO);
    }

    @SuppressWarnings("unchecked")
    private void cargarEstado() {
        Object[] estado = PersistenciaUtil.cargar(ARCHIVO, Object[].class);
        if (estado == null || estado.length < 2) {
            return;
        }
        if (estado[0] instanceof ColaSoporte) {
            colaSoporte = (ColaSoporte) estado[0];
        }
        if (estado[1] instanceof Integer) {
            consecutivoTicket = ((Integer) estado[1]).intValue();
        }
        if (estado.length >= 3 && estado[2] instanceof ColaSoporte) {
            colaPlataforma = (ColaSoporte) estado[2];
        }
        if (estado.length >= 4 && estado[3] instanceof List) {
            ticketsEnProceso = new ArrayList<>((List<Soporte>) estado[3]);
        }
        if (estado.length >= 5 && estado[4] instanceof List) {
            ticketsCerrados = new ArrayList<>((List<Soporte>) estado[4]);
        }
    }
}
