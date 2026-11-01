package entidades;

/**
 * Entidad que representa la factura emitida a un cliente por la prestación del servicio.
 */
public class Factura {

    private int idFactura;
    private String identificacionCliente;
    private String usuario; // Nombre o identificación del titular
    private int idPlan;
    private String nombrePlan;
    private String fecha;
    private double total;
    private String archivoPDF;
    private String estado; // PENDIENTE, PAGADA, ANULADA

    public Factura() {
        this.estado = "PENDIENTE";
    }

    public Factura(int idFactura, String usuario, int idPlan, String fecha, double total, String archivoPDF) {
        this.idFactura = idFactura;
        this.usuario = usuario != null ? usuario.trim() : "";
        this.idPlan = idPlan;
        this.fecha = fecha != null ? fecha.trim() : "";
        this.total = Math.max(0, total);
        this.archivoPDF = archivoPDF != null ? archivoPDF.trim() : "";
        this.estado = "PENDIENTE";
    }

    public Factura(int idFactura, String identificacionCliente, String usuario, int idPlan,
                   String nombrePlan, String fecha, double total, String archivoPDF, String estado) {
        this.idFactura = idFactura;
        this.identificacionCliente = identificacionCliente != null ? identificacionCliente.trim() : "";
        this.usuario = usuario != null ? usuario.trim() : "";
        this.idPlan = idPlan;
        this.nombrePlan = nombrePlan != null ? nombrePlan.trim() : "";
        this.fecha = fecha != null ? fecha.trim() : "";
        this.total = Math.max(0, total);
        this.archivoPDF = archivoPDF != null ? archivoPDF.trim() : "";
        this.estado = estado != null ? estado.trim() : "PENDIENTE";
    }

    public int getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(int idFactura) {
        this.idFactura = idFactura;
    }

    public String getIdentificacionCliente() {
        return identificacionCliente;
    }

    public void setIdentificacionCliente(String identificacionCliente) {
        this.identificacionCliente = identificacionCliente;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public int getIdPlan() {
        return idPlan;
    }

    public void setIdPlan(int idPlan) {
        this.idPlan = idPlan;
    }

    public String getNombrePlan() {
        return nombrePlan;
    }

    public void setNombrePlan(String nombrePlan) {
        this.nombrePlan = nombrePlan;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = Math.max(0, total);
    }

    public String getArchivoPDF() {
        return archivoPDF;
    }

    public void setArchivoPDF(String archivoPDF) {
        this.archivoPDF = archivoPDF;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Factura #" + idFactura + " - " + usuario + " - $" + String.format("%,.0f", total) + " [" + fecha + "] (" + estado + ")";
    }
}
