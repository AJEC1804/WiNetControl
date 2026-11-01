package modelos;

import entidades.Cliente;
import entidades.Plan;
import metodos.ListaPlanes;
import utils.ValidadorTexto;
import utils.Respuesta;
import java.util.List;

/**
 * Modelo de Negocio para Planes de Internet.
 * Reglas de validación comercial, tarifas y actualización en cascada.
 */
public class PlanModel {

    private final ListaPlanes listaPlanes;

    public PlanModel() {
        this.listaPlanes = new ListaPlanes();
    }

    public PlanModel(ListaPlanes listaPlanes) {
        this.listaPlanes = listaPlanes;
    }

    public ListaPlanes getListaPlanes() {
        return listaPlanes;
    }

    /**
     * Crea un nuevo plan en el catálogo validando reglas de negocio.
     */
    public Respuesta<Plan> crearPlan(Plan plan) {
        if (plan == null) {
            return Respuesta.error("Los datos del plan no pueden ser nulos.");
        }

        if (plan.getIdPlan() <= 0) {
            return Respuesta.error("El código del plan debe ser un número entero mayor a cero.");
        }

        if (!ValidadorTexto.esNoVacio(plan.getNombrePlan())) {
            return Respuesta.error("El nombre del plan es obligatorio.");
        }

        if (!ValidadorTexto.esNoVacio(plan.getVelocidadPlan())) {
            return Respuesta.error("La velocidad del plan es obligatoria (ej: 100 Mbps).");
        }

        if (plan.getPrecio() < 0) {
            return Respuesta.error("El precio del plan no puede ser negativo.");
        }

        if (listaPlanes.existeIdPlan(plan.getIdPlan())) {
            return Respuesta.error("Ya existe un plan registrado con el código: " + plan.getIdPlan());
        }

        if (listaPlanes.buscarPorNombre(plan.getNombrePlan()) != null) {
            return Respuesta.error("Ya existe un plan registrado con el nombre: " + plan.getNombrePlan());
        }

        listaPlanes.insertarFinal(plan);
        return Respuesta.exito("Plan creado exitosamente en el catálogo.", plan);
    }

    /**
     * Actualiza un plan existente.
     */
    public Respuesta<Plan> actualizarPlan(int idActual, Plan datosNuevos) {
        Plan planExistente = listaPlanes.buscarPorId(idActual);
        if (planExistente == null) {
            return Respuesta.error("No se encontró ningún plan con el código: " + idActual, 404);
        }

        if (datosNuevos.getIdPlan() > 0 && datosNuevos.getIdPlan() != idActual) {
            if (listaPlanes.existeIdPlanEnOtro(datosNuevos.getIdPlan(), idActual)) {
                return Respuesta.error("El nuevo código ya pertenece a otro plan existente.");
            }
            planExistente.setIdPlan(datosNuevos.getIdPlan());
        }

        if (ValidadorTexto.esNoVacio(datosNuevos.getNombrePlan())) {
            planExistente.setNombrePlan(datosNuevos.getNombrePlan());
        }

        if (ValidadorTexto.esNoVacio(datosNuevos.getVelocidadPlan())) {
            planExistente.setVelocidadPlan(datosNuevos.getVelocidadPlan());
        }

        if (datosNuevos.getPrecio() >= 0) {
            planExistente.setPrecio(datosNuevos.getPrecio());
        }

        if (ValidadorTexto.esNoVacio(datosNuevos.getDescripcionPlan())) {
            planExistente.setDescripcionPlan(datosNuevos.getDescripcionPlan());
        }

        return Respuesta.exito("Plan actualizado correctamente.", planExistente);
    }

    /**
     * Elimina un plan y actualiza en cascada a los clientes que lo tenían contratado.
     */
    public Respuesta<Boolean> eliminarPlan(int idPlan, ClienteModel clienteModel) {
        Plan planAEliminar = listaPlanes.buscarPorId(idPlan);
        if (planAEliminar == null) {
            return Respuesta.error("No se encontró el plan con código: " + idPlan, 404);
        }

        String nombrePlanEliminado = planAEliminar.getNombrePlan();
        boolean eliminado = listaPlanes.eliminarPorId(idPlan);

        if (eliminado) {
            // Regla de negocio: reasignar plan sustituto a clientes afectados
            if (clienteModel != null) {
                Plan sustituto = listaPlanes.obtenerPrimerPlanDisponible();
                String nombreSustituto = (sustituto != null) ? sustituto.getNombrePlan() : "Sin plan";

                for (Cliente c : clienteModel.getListaClientes().listarATodos()) {
                    if (c.getPlanActual() != null && c.getPlanActual().equalsIgnoreCase(nombrePlanEliminado)) {
                        c.setPlanActual(nombreSustituto);
                    }
                }
            }
            return Respuesta.exito("Plan eliminado correctamente del catálogo.", true);
        }

        return Respuesta.error("No fue posible eliminar el plan.");
    }

    public Respuesta<Plan> consultarPlanPorId(int idPlan) {
        Plan p = listaPlanes.buscarPorId(idPlan);
        if (p != null) {
            return Respuesta.exito("Plan encontrado.", p);
        }
        return Respuesta.error("Plan no encontrado con código: " + idPlan, 404);
    }

    public Respuesta<Plan> consultarPlanPorNombre(String nombrePlan) {
        Plan p = listaPlanes.buscarPorNombre(nombrePlan);
        if (p != null) {
            return Respuesta.exito("Plan encontrado.", p);
        }
        return Respuesta.error("Plan no encontrado con nombre: " + nombrePlan, 404);
    }

    public Respuesta<List<Plan>> listarPlanes() {
        List<Plan> planes = listaPlanes.listarATodos();
        return Respuesta.exito("Catálogo de planes obtenido (" + planes.size() + ").", planes);
    }

    /**
     * Activa un plan en el catálogo comercial.
     */
    public Respuesta<Plan> activarPlan(int idPlan) {
        return cambiarEstadoPlan(idPlan, true);
    }

    /**
     * Desactiva un plan en el catálogo comercial.
     */
    public Respuesta<Plan> desactivarPlan(int idPlan) {
        return cambiarEstadoPlan(idPlan, false);
    }

    /**
     * Cambia el estado activo/inactivo de un plan.
     */
    public Respuesta<Plan> cambiarEstadoPlan(int idPlan, boolean nuevoEstado) {
        Plan plan = listaPlanes.buscarPorId(idPlan);
        if (plan == null) {
            return Respuesta.error("Plan no encontrado con código: " + idPlan, 404);
        }
        plan.setActivo(nuevoEstado);
        String estadoTxt = nuevoEstado ? "ACTIVADO" : "DESACTIVADO";
        return Respuesta.exito("Plan " + estadoTxt + " correctamente.", plan);
    }

    /**
     * Lista únicamente los planes activos disponibles para contratación.
     */
    public Respuesta<List<Plan>> listarPlanesActivos() {
        List<Plan> activos = listaPlanes.listarPorEstado(true);
        return Respuesta.exito("Planes activos disponibles: " + activos.size(), activos);
    }

    /**
     * Lista planes filtrados por estado.
     */
    public Respuesta<List<Plan>> listarPlanesPorEstado(boolean activo) {
        List<Plan> filtrados = listaPlanes.listarPorEstado(activo);
        String estadoTxt = activo ? "activos" : "inactivos";
        return Respuesta.exito("Planes " + estadoTxt + ": " + filtrados.size(), filtrados);
    }

    public Plan obtenerPlanPorDefecto() {
        Plan primero = listaPlanes.obtenerPrimerPlanDisponible();
        if (primero != null) {
            return primero;
        }
        return new Plan(1, "Plan Básico", "50 Mbps", 45000, "Plan residencial estándar");
    }
}
