package modelos;

import utils.PersistenciaUtil;

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

public class FacturaModel {

    private PilaFacturas pilaFacturas;

    private static final String ARCHIVO = "facturas.dat";
    private int consecutivoFactura;
    private static final String CARPETA_FACTURAS = "facturas";

    public FacturaModel() {
        this.pilaFacturas = new PilaFacturas();
        this.consecutivoFactura = 1;
        asegurarDirectorio();
        cargarEstado();
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

    public Respuesta<Factura> emitirFactura(Cliente cliente, Plan plan) {
        return emitirFactura(cliente, plan, FormateadorFecha.getMesActual(), FormateadorFecha.getAnoActual());
    }

    public Respuesta<Factura> emitirFactura(Cliente cliente, Plan plan, int mes, int ano) {
        if (cliente == null || plan == null) {
            return Respuesta.error("Cliente y plan son requeridos para facturar.");
        }
        if (mes < 1 || mes > 12 || ano < 2000 || ano > 2100) {
            return Respuesta.error("El mes o año seleccionado es inválido.", 400);
        }

        int mesActual = FormateadorFecha.getMesActual();
        int anoActual = FormateadorFecha.getAnoActual();
        if (ano > anoActual || (ano == anoActual && mes > mesActual)) {
            return Respuesta.error("No se puede emitir factura para un mes futuro (" + String.format("%02d/%d", mes, ano) + ").", 400);
        }

        String fechaPlan = cliente.getFechaEfectivaPlan();
        if (fechaPlan != null && fechaPlan.contains("/")) {
            String[] partes = fechaPlan.split("/");
            if (partes.length == 3) {
                try {
                    int mesInicio = Integer.parseInt(partes[1].trim());
                    int anoInicio = Integer.parseInt(partes[2].trim());
                    if (ano < anoInicio || (ano == anoInicio && mes < mesInicio)) {
                        return Respuesta.error(
                                "No se puede emitir factura para " + String.format("%02d/%d", mes, ano)
                                        + " porque el cliente adquirió su plan el " + fechaPlan + ".",
                                400);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        String fechaActual = (mes == mesActual && ano == anoActual)
                ? FormateadorFecha.fechaActualTexto()
                : String.format("01/%02d/%d", mes, ano);

        if (pilaFacturas.buscarFacturaMes(cliente.getIdentificacion(), mes, ano) != null) {
            return Respuesta.error("Ya se emitió una factura para este cliente en el mes " + mes + "/" + ano + ".", 400);
        }

        int idFactura = consecutivoFactura++;
        String nombreArchivo = String.format("%s/factura_%d_%s-%d-%d.pdf",
                CARPETA_FACTURAS,
                idFactura,
                cliente.getNombreCompleto().replace(" ", "_"),
                mes,
                ano);

        Factura factura = new Factura(
                idFactura,
                cliente.getIdentificacion(),
                cliente.getNombreCompleto(),
                plan.getIdPlan(),
                plan.getNombrePlan(),
                fechaActual,
                plan.getPrecio(),
                nombreArchivo,
                "EMITIDA"
        );
        factura.setEmpresaId(cliente.getEmpresaId());

        GeneradorPdfUtil.generarFacturaPdf(
                nombreArchivo,
                factura.getIdFactura(),
                cliente.getNombreCompleto(),
                plan.getNombrePlan(),
                plan.getVelocidadPlan(),
                plan.getPrecio(),
                fechaActual
        );

        pilaFacturas.apilar(factura);

        return Respuesta.exito("Factura emitida y apilada correctamente.", factura);
    }

    public Respuesta<List<Factura>> consultarFacturasUsuario(String criterioUsuarioOId) {
        if (!ValidadorTexto.esNoVacio(criterioUsuarioOId)) {
            return Respuesta.error("Identificación o nombre de usuario requerido.");
        }
        List<Factura> facturas = pilaFacturas.obtenerFacturasUsuario(criterioUsuarioOId);
        return Respuesta.exito("Facturas encontradas: " + facturas.size(), facturas);
    }

    public Respuesta<Factura> buscarFacturaPorPeriodo(String usuario, int mes, int ano) {
        Factura f = pilaFacturas.buscarFacturaMes(usuario, mes, ano);
        if (f != null) {
            return Respuesta.exito("Factura encontrada para el periodo " + mes + "/" + ano, f);
        }
        return Respuesta.error("No se encontró factura para el usuario en el periodo " + mes + "/" + ano, 404);
    }

    public Respuesta<Factura> desapilarUltimaFactura() {
        Factura f = pilaFacturas.desapilar();
        if (f != null) {
            return Respuesta.exito("Última factura desapilada con éxito.", f);
        }
        return Respuesta.error("No hay facturas en la pila para desapilar.", 404);
    }

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

    public Respuesta<Factura> pagarFactura(int idFactura) {
        if (idFactura <= 0) {
            return Respuesta.error("Número de factura inválido.", 400);
        }

        Factura encontrada = pilaFacturas.buscarPorId(idFactura);

        if (encontrada == null) {
            return Respuesta.error("No se encontró la factura #" + idFactura, 404);
        }

        if ("PAGADA".equalsIgnoreCase(encontrada.getEstado())) {
            return Respuesta.error("La factura #00" + idFactura + " ya se encuentra PAGADA.", 400);
        }

        if ("ANULADA".equalsIgnoreCase(encontrada.getEstado())) {
            return Respuesta.error("La factura #00" + idFactura + " está ANULADA y no puede ser pagada.", 400);
        }

        encontrada.setEstado("PAGADA");
        return Respuesta.exito("Pago registrado correctamente para la factura #00" + idFactura + ".", encontrada);
    }

    public Respuesta<Factura> anularFactura(int idFactura) {
        if (idFactura <= 0) {
            return Respuesta.error("Número de factura inválido.", 400);
        }

        Factura encontrada = pilaFacturas.buscarPorId(idFactura);

        if (encontrada == null) {
            return Respuesta.error("No se encontró la factura #" + idFactura, 404);
        }

        if ("PAGADA".equalsIgnoreCase(encontrada.getEstado())) {
            return Respuesta.error("No se puede anular la factura #00" + idFactura + " porque ya fue PAGADA.", 400);
        }

        if ("ANULADA".equalsIgnoreCase(encontrada.getEstado())) {
            return Respuesta.error("La factura #00" + idFactura + " ya se encuentra ANULADA.", 400);
        }

        encontrada.setEstado("ANULADA");
        return Respuesta.exito("Factura #00" + idFactura + " anulada correctamente.", encontrada);
    }

    public Respuesta<Factura> cambiarEstadoFactura(int idFactura, String nuevoEstado) {
        if (idFactura <= 0) {
            return Respuesta.error("Número de factura inválido.", 400);
        }
        if (!ValidadorTexto.esNoVacio(nuevoEstado)) {
            return Respuesta.error("El nuevo estado es obligatorio.", 400);
        }

        String estadoNormalizado = nuevoEstado.trim().toUpperCase();
        if (!"PENDIENTE".equals(estadoNormalizado)
                && !"EMITIDA".equals(estadoNormalizado)
                && !"PAGADA".equals(estadoNormalizado)
                && !"ANULADA".equals(estadoNormalizado)) {
            return Respuesta.error("Estado de factura no válido: " + nuevoEstado, 400);
        }

        Factura encontrada = pilaFacturas.buscarPorId(idFactura);
        if (encontrada == null) {
            return Respuesta.error("No se encontró la factura #" + idFactura, 404);
        }

        if (estadoNormalizado.equalsIgnoreCase(encontrada.getEstado())) {
            return Respuesta.error("La factura #00" + idFactura + " ya se encuentra en estado " + estadoNormalizado + ".", 400);
        }

        encontrada.setEstado(estadoNormalizado);
        return Respuesta.exito("Estado de la factura #00" + idFactura + " actualizado a " + estadoNormalizado + ".", encontrada);
    }

    public void guardarEstado() {
        PersistenciaUtil.guardar(new Object[]{pilaFacturas, Integer.valueOf(consecutivoFactura)}, ARCHIVO);
    }

    private void cargarEstado() {
        Object[] estado = PersistenciaUtil.cargar(ARCHIVO, Object[].class);
        if (estado == null || estado.length < 2) {
            return;
        }
        if (estado[0] instanceof PilaFacturas) {
            pilaFacturas = (PilaFacturas) estado[0];
        }
        if (estado[1] instanceof Integer) {
            consecutivoFactura = ((Integer) estado[1]).intValue();
        }
    }

}
