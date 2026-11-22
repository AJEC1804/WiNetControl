package controladores;

import entidades.Empresa;
import modelos.EmpresaModel;
import utils.Respuesta;
import utils.ValidadorTexto;

import java.util.List;

public class EmpresaController {

    private final EmpresaModel empresaModel;
    private static final EmpresaModel sharedEmpresaModel = new EmpresaModel();

    public EmpresaController() {
        this(sharedEmpresaModel);
    }

    public EmpresaController(EmpresaModel empresaModel) {
        this.empresaModel = (empresaModel != null) ? empresaModel : sharedEmpresaModel;
    }

    public static EmpresaModel getSharedEmpresaModel() {
        return sharedEmpresaModel;
    }

    public Respuesta<Empresa> registrarEmpresa(String nombre, String nit, String correo,
                                               String telefono, String direccion,
                                               String planPlataforma, String precioMensual,
                                               boolean activo, String adminIdentificacion) {
        double precio = 250000;
        if (ValidadorTexto.esNoVacio(precioMensual)) {
            Double parsed = ValidadorTexto.parseDouble(precioMensual);
            if (parsed == null || parsed < 0) {
                return Respuesta.error("El precio mensual de la empresa debe ser numérico positivo.", 400);
            }
            precio = parsed;
        }
        Empresa nueva = new Empresa("", nombre, nit, correo, telefono, direccion,
                planPlataforma, precio, activo, adminIdentificacion);
        return empresaModel.registrarEmpresa(nueva);
    }

    public Respuesta<Empresa> registrarEmpresa(Empresa empresa) {
        return empresaModel.registrarEmpresa(empresa);
    }

    public Respuesta<List<Empresa>> listarEmpresas() {
        return empresaModel.listarEmpresas();
    }

    public Respuesta<List<Empresa>> listarEmpresasPorEstado(boolean activo) {
        return empresaModel.listarEmpresasPorEstado(activo);
    }

    public Respuesta<Empresa> buscarEmpresa(String idEmpresa) {
        return empresaModel.buscarEmpresa(idEmpresa);
    }

    public Respuesta<Empresa> actualizarEmpresa(String idEmpresa, Empresa datos) {
        return empresaModel.actualizarEmpresa(idEmpresa, datos);
    }

    public Respuesta<Empresa> activarEmpresa(String idEmpresa) {
        return empresaModel.cambiarEstadoEmpresa(idEmpresa, true, "");
    }

    public Respuesta<Empresa> desactivarEmpresa(String idEmpresa, String motivo) {
        return empresaModel.cambiarEstadoEmpresa(idEmpresa, false, motivo);
    }

    public Respuesta<Empresa> cambiarEstadoEmpresa(String idEmpresa, boolean activo, String motivo) {
        return empresaModel.cambiarEstadoEmpresa(idEmpresa, activo, motivo);
    }

    public Respuesta<Boolean> eliminarEmpresa(String idEmpresa) {
        return empresaModel.eliminarEmpresa(idEmpresa);
    }

    public boolean estaEmpresaActiva(String empresaId) {
        return empresaModel.estaEmpresaActiva(empresaId);
    }

    public Empresa obtenerEmpresaDirecta(String empresaId) {
        return empresaModel.obtenerEmpresaDirecta(empresaId);
    }
}
