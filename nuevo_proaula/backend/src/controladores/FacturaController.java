package controladores;

import entidades.Cliente;
import entidades.Factura;
import entidades.Plan;
import modelos.ClienteModel;
import modelos.FacturaModel;
import modelos.PlanModel;
import utils.FormateadorFecha;
import utils.Respuesta;
import utils.ValidadorTexto;
import java.util.List;

/**
 * Controlador para operaciones de Facturación y comprobantes.
 * Conecta las solicitudes de la vista con la Pila de Facturas (LIFO)
 * y el generador de reportes PDF.
 */
public class FacturaController {

    private final FacturaModel facturaModel;
    private final ClienteModel clienteModel;
    private final PlanModel planModel;

    public FacturaController() {
        this(UsuarioController.getSharedFacturaModel(),
             UsuarioController.getSharedClienteModel(),
             UsuarioController.getSharedPlanModel());
    }

    public FacturaController(FacturaModel facturaModel, ClienteModel clienteModel, PlanModel planModel) {
        this.facturaModel = (facturaModel != null) ? facturaModel : UsuarioController.getSharedFacturaModel();
        this.clienteModel = (clienteModel != null) ? clienteModel : UsuarioController.getSharedClienteModel();
        this.planModel = (planModel != null) ? planModel : UsuarioController.getSharedPlanModel();
    }

    /**
     * Generar y apilar una factura para el cliente indicado.
     */
    public Respuesta<Factura> generarFactura(String identificacionCliente, String nombreOCodigoPlan) {
        if (!ValidadorTexto.esNoVacio(identificacionCliente)) {
            return Respuesta.error("Identificación del cliente requerida.");
        }

        Respuesta<Cliente> respCliente = clienteModel.consultarCliente(identificacionCliente);
        if (!respCliente.isExito() || respCliente.getDatos() == null) {
            return Respuesta.error("Cliente no encontrado con identificación: " + identificacionCliente, 404);
        }
        Cliente cliente = respCliente.getDatos();

        Plan plan = null;
        if (ValidadorTexto.esNoVacio(nombreOCodigoPlan)) {
            Integer cod = ValidadorTexto.parseEntero(nombreOCodigoPlan);
            if (cod != null) {
                Respuesta<Plan> respPlan = planModel.consultarPlanPorId(cod);
                if (respPlan.isExito()) plan = respPlan.getDatos();
            }
            if (plan == null) {
                Respuesta<Plan> respPlan = planModel.consultarPlanPorNombre(nombreOCodigoPlan);
                if (respPlan.isExito()) plan = respPlan.getDatos();
            }
        }

        if (plan == null && ValidadorTexto.esNoVacio(cliente.getPlanActual())) {
            Respuesta<Plan> respPlan = planModel.consultarPlanPorNombre(cliente.getPlanActual());
            if (respPlan.isExito()) plan = respPlan.getDatos();
        }

        if (plan == null) {
            plan = planModel.obtenerPlanPorDefecto();
        }

        return facturaModel.emitirFactura(cliente, plan);
    }

    /**
     * Generar y apilar una factura usando exclusivamente el plan contratado del cliente
     * para el mes y año indicados.
     */
    public Respuesta<Factura> generarFactura(String identificacionCliente, int mes, int ano) {
        if (!ValidadorTexto.esNoVacio(identificacionCliente)) {
            return Respuesta.error("Identificación del cliente requerida.");
        }

        Respuesta<Cliente> respCliente = clienteModel.consultarCliente(identificacionCliente);
        if (!respCliente.isExito() || respCliente.getDatos() == null) {
            return Respuesta.error("Cliente no encontrado con identificación: " + identificacionCliente, 404);
        }
        Cliente cliente = respCliente.getDatos();

        String nombrePlan = (cliente.getPlanActual() != null) ? cliente.getPlanActual().trim() : "";
        if (nombrePlan.isEmpty() || "Sin plan".equalsIgnoreCase(nombrePlan)) {
            return Respuesta.error("El cliente no tiene un plan contratado. Asígnale un plan antes de facturar.", 400);
        }

        Respuesta<Plan> respPlan = planModel.consultarPlanPorNombre(nombrePlan);
        if (!respPlan.isExito() || respPlan.getDatos() == null) {
            return Respuesta.error("No se encontró en el catálogo el plan contratado por el cliente: " + nombrePlan, 404);
        }

        return facturaModel.emitirFactura(cliente, respPlan.getDatos(), mes, ano);
    }

    /**
     * Obtener el historial de facturas de un usuario.
     */
    public Respuesta<List<Factura>> obtenerFacturasUsuario(String criterioUsuarioOId) {
        return facturaModel.consultarFacturasUsuario(criterioUsuarioOId);
    }

    /**
     * Buscar factura por mes y año.
     */
    public Respuesta<Factura> buscarFacturaPorPeriodo(String usuario, int mes, int ano) {
        return facturaModel.buscarFacturaPorPeriodo(usuario, mes, ano);
    }

    /**
     * Desapilar la última factura emitida (tope de la pila LIFO).
     */
    public Respuesta<Factura> desapilarUltimaFactura() {
        return facturaModel.desapilarUltimaFactura();
    }

    /**
     * Consultar la cima de la pila sin desapilar.
     */
    public Respuesta<Factura> consultarCima() {
        return facturaModel.consultarCima();
    }

    /**
     * Listar todas las facturas del sistema.
     */
    public Respuesta<List<Factura>> listarTodasFacturas() {
        return facturaModel.listarTodas();
    }

    /**
     * Registrar el pago de una factura por su ID.
     */
    public Respuesta<Factura> pagarFactura(int idFactura) {
        return facturaModel.pagarFactura(idFactura);
    }
}
