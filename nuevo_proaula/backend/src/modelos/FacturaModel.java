package modelos;

import entidades.Cliente;
import entidades.Factura;
import entidades.Plan;
import metodos.PilaFacturas;
import utils.FormateadorFecha;
import utils.GeneradorPdfUtil;
import utils.Respuesta;
import utils.ValidadorTexto;
import java.io.File;
import java.util.List;

/**
 * Modelo de Negocio para Facturación.
 * Gestiona la emisión y registro de facturas en la Pila LIFO,
 * así como la generación de comprobantes en PDF.
 */
public class FacturaModel {

    private final PilaFacturas pilaFacturas;
    private int consecutivoFactura;
    private static final String CARPETA_FACTURAS = "facturas";

    public FacturaModel() {
        this.pilaFacturas = new PilaFacturas();
        this.consecutivoFactura = 1;
        asegurarDirectorio();
    }

    public FacturaModel(PilaFacturas pilaFacturas) {
        this.pilaFacturas = pilaFacturas;
        this.consecutivoFactura = 1;
        asegurarDirectorio();
    }

    private void asegurarDirectorio() {
        File f = new File(CARPETA_FACTURAS);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    public PilaFacturas getPilaFacturas() {
        return pilaFacturas;
    }

    /**
     * Emite una factura para un cliente dado según su plan contratado y la apila.
     */
    public Respuesta<Factura> emitirFactura(Cliente cliente, Plan plan) {
        if (cliente == null || plan == null) {
            return Respuesta.error("Cliente y plan son requeridos para facturar.");
        }

        int mes = FormateadorFecha.getMesActual();
        int ano = FormateadorFecha.getAnoActual();
        String fechaActual = FormateadorFecha.fechaActualTexto();

        String nombreArchivo = String.format("%s/factura_%s-%d-%d.pdf",
                CARPETA_FACTURAS,
                cliente.getNombreCompleto().replace(" ", "_"),
                mes,
                ano);

        Factura factura = new Factura(
                consecutivoFactura++,
                cliente.getIdentificacion(),
                cliente.getNombreCompleto(),
                plan.getIdPlan(),
                plan.getNombrePlan(),
                fechaActual,
                plan.getPrecio(),
                nombreArchivo,
                "EMITIDA"
        );

        // Generar archivo PDF en disco
        GeneradorPdfUtil.generarFacturaPdf(
                nombreArchivo,
                factura.getIdFactura(),
                cliente.getNombreCompleto(),
                plan.getNombrePlan(),
                plan.getVelocidadPlan(),
                plan.getPrecio(),
                fechaActual
        );

        // Apilar en la estructura de datos LIFO
        pilaFacturas.apilar(factura);

        return Respuesta.exito("Factura emitida y apilada correctamente.", factura);
    }

    /**
     * Consulta el historial de facturas emitidas a un cliente.
     */
    public Respuesta<List<Factura>> consultarFacturasUsuario(String criterioUsuarioOId) {
        if (!ValidadorTexto.esNoVacio(criterioUsuarioOId)) {
            return Respuesta.error("Identificación o nombre de usuario requerido.");
        }
        List<Factura> facturas = pilaFacturas.obtenerFacturasUsuario(criterioUsuarioOId);
        return Respuesta.exito("Facturas encontradas: " + facturas.size(), facturas);
    }

    /**
     * Busca la factura emitida a un usuario en un mes y año determinados.
     */
    public Respuesta<Factura> buscarFacturaPorPeriodo(String usuario, int mes, int ano) {
        Factura f = pilaFacturas.buscarFacturaMes(usuario, mes, ano);
        if (f != null) {
            return Respuesta.exito("Factura encontrada para el periodo " + mes + "/" + ano, f);
        }
        return Respuesta.error("No se encontró factura para el usuario en el periodo " + mes + "/" + ano, 404);
    }

    /**
     * Desapila la última factura emitida (tope de la pila).
     */
    public Respuesta<Factura> desapilarUltimaFactura() {
        Factura f = pilaFacturas.desapilar();
        if (f != null) {
            return Respuesta.exito("Última factura desapilada con éxito.", f);
        }
        return Respuesta.error("No hay facturas en la pila para desapilar.", 404);
    }

    /**
     * Consulta la factura en la cima sin desapilarla.
     */
    public Respuesta<Factura> consultarCima() {
        Factura f = pilaFacturas.getCima();
        if (f != null) {
            return Respuesta.exito("Factura en cima obtenida.", f);
        }
        return Respuesta.error("La pila de facturas está vacía.", 404);
    }

    public Respuesta<List<Factura>> listarTodas() {
        List<Factura> todas = pilaFacturas.listarTodas();
        return Respuesta.exito("Historial total de facturas obtenido (" + todas.size() + ").", todas);
    }
}
