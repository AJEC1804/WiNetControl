package vistas;

import controladores.PlanController;
import entidades.Plan;
import utils.Respuesta;
import java.util.List;

/**
 * Plantilla base para la Vista de Gestión CRUD de Planes de Internet.
 */
public class PlanesView {

    private final PlanController planController;

    public PlanesView() {
        this.planController = new PlanController();
    }

    public Respuesta<Plan> crearPlan(String cod, String nom, String vel, String pre, String desc) {
        return planController.crearPlan(cod, nom, vel, pre, desc);
    }

    public Respuesta<Plan> buscarPorCodigo(String cod) {
        return planController.buscarPlanPorCodigo(cod);
    }

    public Respuesta<Plan> actualizar(String codActual, String nuevoCod, String nom, String vel, String pre, String desc) {
        return planController.actualizarPlan(codActual, nuevoCod, nom, vel, pre, desc);
    }

    public Respuesta<Boolean> eliminar(String cod) {
        return planController.eliminarPlan(cod);
    }

    public Respuesta<List<Plan>> obtenerTodos() {
        return planController.listarPlanes();
    }
}
