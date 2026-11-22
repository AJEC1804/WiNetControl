package controladores;

import entidades.Plan;
import modelos.ClienteModel;
import modelos.PlanModel;
import utils.Respuesta;
import utils.ValidadorTexto;
import java.util.List;

public class PlanController {

    private final PlanModel planModel;
    private final ClienteModel clienteModel;

    public PlanController() {
        this(UsuarioController.getSharedPlanModel(), UsuarioController.getSharedClienteModel());
    }

    public PlanController(PlanModel planModel, ClienteModel clienteModel) {
        this.planModel = (planModel != null) ? planModel : UsuarioController.getSharedPlanModel();
        this.clienteModel = (clienteModel != null) ? clienteModel : UsuarioController.getSharedClienteModel();
    }

    public Respuesta<Plan> crearPlan(String codigo, String nombre, String velocidad, String precio, String descripcion) {
        Integer codNum = ValidadorTexto.parseEntero(codigo);
        if (codNum == null) {
            return Respuesta.error("El código del plan debe ser un número entero válido.");
        }

        Double precNum = ValidadorTexto.parseDouble(precio);
        if (precNum == null) {
            return Respuesta.error("El precio del plan debe ser numérico.");
        }

        Plan nuevo = new Plan(codNum, nombre, velocidad, precNum, descripcion);
        return planModel.crearPlan(nuevo);
    }

    public Respuesta<Plan> crearPlan(Plan plan) {
        return planModel.crearPlan(plan);
    }

    public Respuesta<Plan> actualizarPlan(String codigoActual, String nuevoCodigo, String nombre,
                                         String velocidad, String precio, String descripcion) {
        Integer codActualNum = ValidadorTexto.parseEntero(codigoActual);
        if (codActualNum == null) {
            return Respuesta.error("Código actual inválido.");
        }

        Plan datosNuevos = new Plan();
        if (ValidadorTexto.esNoVacio(nuevoCodigo)) {
            Integer codNuevoNum = ValidadorTexto.parseEntero(nuevoCodigo);
            if (codNuevoNum == null) {
                return Respuesta.error("El nuevo código debe ser numérico.");
            }
            datosNuevos.setIdPlan(codNuevoNum);
        } else {
            datosNuevos.setIdPlan(codActualNum);
        }

        datosNuevos.setNombrePlan(nombre);
        datosNuevos.setVelocidadPlan(velocidad);

        if (ValidadorTexto.esNoVacio(precio)) {
            Double precNum = ValidadorTexto.parseDouble(precio);
            if (precNum == null) {
                return Respuesta.error("El precio debe ser numérico.");
            }
            if (precNum < 0) {
                return Respuesta.error("El precio del plan no puede ser negativo.");
            }
            datosNuevos.setPrecio(precNum);
        } else {
            datosNuevos.setPrecio(-1);
        }

        datosNuevos.setDescripcionPlan(descripcion);
        return planModel.actualizarPlan(codActualNum, datosNuevos, clienteModel);
    }

    public Respuesta<Boolean> eliminarPlan(String codigo) {
        Integer codNum = ValidadorTexto.parseEntero(codigo);
        if (codNum == null) {
            return Respuesta.error("Código de plan inválido.");
        }
        return planModel.eliminarPlan(codNum, clienteModel);
    }

    public Respuesta<Plan> buscarPlanPorCodigo(String codigo) {
        Integer codNum = ValidadorTexto.parseEntero(codigo);
        if (codNum == null) {
            return Respuesta.error("El código debe ser numérico.");
        }
        return planModel.consultarPlanPorId(codNum);
    }

    public Respuesta<Plan> buscarPlanPorNombre(String nombre) {
        return planModel.consultarPlanPorNombre(nombre);
    }

    public Respuesta<Plan> activarPlan(String codigo) {
        Integer codNum = ValidadorTexto.parseEntero(codigo);
        if (codNum == null) {
            return Respuesta.error("Código de plan inválido.");
        }
        return planModel.activarPlan(codNum);
    }

    public Respuesta<Plan> desactivarPlan(String codigo) {
        Integer codNum = ValidadorTexto.parseEntero(codigo);
        if (codNum == null) {
            return Respuesta.error("Código de plan inválido.");
        }
        return planModel.desactivarPlan(codNum);
    }

    public Respuesta<Plan> cambiarEstadoPlan(String codigo, boolean activo) {
        Integer codNum = ValidadorTexto.parseEntero(codigo);
        if (codNum == null) {
            return Respuesta.error("Código de plan inválido.");
        }
        return planModel.cambiarEstadoPlan(codNum, activo);
    }

    public Respuesta<List<Plan>> listarPlanesActivos() {
        return planModel.listarPlanesActivos();
    }

    public Respuesta<List<Plan>> listarPlanes() {
        return planModel.listarPlanes();
    }
}
