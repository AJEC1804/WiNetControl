package model;

public class PlanBase {

    protected int idPlan;
    protected String nombrePlan;
    protected String velocidadPlan;
    protected String descripcionPlan;
    protected double precio;

    public PlanBase(int idPlan, String nombrePlan, String velocidadPlan, double precio, String descripcionPlan) {
        setIdPlan(idPlan);
        setNombrePlan(nombrePlan);
        setVelocidadPlan(velocidadPlan);
        setPrecio(precio);
        setDescripcionPlan(descripcionPlan);
    }

    public int getIdPlan() {
        return idPlan;
    }

    public final void setIdPlan(int idPlan) {
        if (idPlan > 0) {
            this.idPlan = idPlan;
        } else {
            this.idPlan = 1;
        }
    }

    public String getNombrePlan() {
        return nombrePlan;
    }

    public final void setNombrePlan(String nombrePlan) {
        if (nombrePlan != null && !nombrePlan.trim().isEmpty()) {
            this.nombrePlan = nombrePlan.trim();
        } else {
            this.nombrePlan = "Sin nombre";
        }
    }

    public String getVelocidadPlan() {
        return velocidadPlan;
    }

    public final void setVelocidadPlan(String velocidadPlan) {
        if (velocidadPlan != null && !velocidadPlan.trim().isEmpty()) {
            this.velocidadPlan = velocidadPlan.trim();
        } else {
            this.velocidadPlan = "0 Mbps";
        }
    }

    public double getPrecio() {
        return precio;
    }

    public final void setPrecio(double precio) {
        if (precio >= 0) {
            this.precio = precio;
        } else {
            this.precio = 0;
        }
    }

    public String getDescripcionPlan() {
        return descripcionPlan;
    }

    public final void setDescripcionPlan(String descripcionPlan) {
        if (descripcionPlan != null && !descripcionPlan.trim().isEmpty()) {
            this.descripcionPlan = descripcionPlan.trim();
        } else {
            this.descripcionPlan = "Sin descripcion";
        }
    }

    public String getInfoPlan() {
        return idPlan + " - " + nombrePlan + " - " + velocidadPlan + " - " + precio;
    }
}