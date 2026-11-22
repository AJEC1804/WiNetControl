package modelos;

import entidades.Empresa;
import metodos.ListaEmpresas;
import utils.PersistenciaUtil;
import utils.Respuesta;
import utils.ValidadorTexto;

import java.util.List;

public class EmpresaModel {

    private ListaEmpresas listaEmpresas;
    private int consecutivo = 1;
    private static final String ARCHIVO = "empresas.dat";

    public EmpresaModel() {
        this.listaEmpresas = new ListaEmpresas();
        cargarEstado();
    }

    public Respuesta<Empresa> registrarEmpresa(Empresa empresa) {
        if (empresa == null) {
            return Respuesta.error("Los datos de la empresa son obligatorios.");
        }
        if (!ValidadorTexto.esNoVacio(empresa.getNombre())) {
            return Respuesta.error("El nombre de la empresa es obligatorio.");
        }
        if (!ValidadorTexto.esNoVacio(empresa.getNit())) {
            return Respuesta.error("El NIT de la empresa es obligatorio.");
        }
        if (!ValidadorTexto.validarCorreo(empresa.getCorreo())) {
            return Respuesta.error("El correo de la empresa no es válido.");
        }
        if (!ValidadorTexto.validarTelefono(empresa.getTelefono())) {
            return Respuesta.error("El teléfono de la empresa debe tener 10 dígitos.");
        }
        if (listaEmpresas.buscarPorNit(empresa.getNit()) != null) {
            return Respuesta.error("Ya existe una empresa registrada con el NIT: " + empresa.getNit(), 400);
        }

        if (!ValidadorTexto.esNoVacio(empresa.getIdEmpresa())) {
            String idGen;
            do {
                idGen = String.format("EMP-%03d", consecutivo++);
            } while (listaEmpresas.buscarPorId(idGen) != null);
            empresa.setIdEmpresa(idGen);
        } else if (listaEmpresas.buscarPorId(empresa.getIdEmpresa()) != null) {
            return Respuesta.error("Ya existe una empresa con el ID: " + empresa.getIdEmpresa(), 400);
        } else {
            consecutivo = Math.max(consecutivo, listaEmpresas.getTamano() + 2);
        }

        listaEmpresas.insertarFinal(empresa);
        return Respuesta.exito("Empresa registrada exitosamente.", empresa);
    }

    public Respuesta<List<Empresa>> listarEmpresas() {
        List<Empresa> todas = listaEmpresas.listarTodas();
        return Respuesta.exito("Empresas registradas: " + todas.size(), todas);
    }

    public Respuesta<List<Empresa>> listarEmpresasPorEstado(boolean activo) {
        List<Empresa> filtradas = listaEmpresas.listarPorEstado(activo);
        return Respuesta.exito("Empresas encontradas: " + filtradas.size(), filtradas);
    }

    public Respuesta<Empresa> buscarEmpresa(String idEmpresa) {
        if (!ValidadorTexto.esNoVacio(idEmpresa)) {
            return Respuesta.error("El ID de empresa es obligatorio.");
        }
        Empresa e = listaEmpresas.buscarPorId(idEmpresa.trim());
        if (e == null) {
            return Respuesta.error("Empresa no encontrada: " + idEmpresa, 404);
        }
        return Respuesta.exito("Empresa encontrada.", e);
    }

    public Respuesta<Empresa> actualizarEmpresa(String idEmpresa, Empresa datos) {
        if (!ValidadorTexto.esNoVacio(idEmpresa) || datos == null) {
            return Respuesta.error("Datos inválidos para actualizar empresa.");
        }
        Empresa existente = listaEmpresas.buscarPorId(idEmpresa.trim());
        if (existente == null) {
            return Respuesta.error("Empresa no encontrada: " + idEmpresa, 404);
        }
        if (ValidadorTexto.esNoVacio(datos.getNit()) && !datos.getNit().equalsIgnoreCase(existente.getNit())) {
            Empresa otroNit = listaEmpresas.buscarPorNit(datos.getNit());
            if (otroNit != null && !otroNit.getIdEmpresa().equalsIgnoreCase(existente.getIdEmpresa())) {
                return Respuesta.error("El NIT ya pertenece a otra empresa.");
            }
            existente.setNit(datos.getNit());
        }
        if (ValidadorTexto.esNoVacio(datos.getNombre())) existente.setNombre(datos.getNombre());
        if (ValidadorTexto.validarCorreo(datos.getCorreo())) existente.setCorreo(datos.getCorreo());
        if (ValidadorTexto.validarTelefono(datos.getTelefono())) existente.setTelefono(datos.getTelefono());
        if (ValidadorTexto.esNoVacio(datos.getDireccion())) existente.setDireccion(datos.getDireccion());
        if (ValidadorTexto.esNoVacio(datos.getPlanPlataforma())) existente.setPlanPlataforma(datos.getPlanPlataforma());
        if (datos.getPrecioMensual() > 0) existente.setPrecioMensual(datos.getPrecioMensual());
        if (ValidadorTexto.esNoVacio(datos.getMotivoSuspension())) existente.setMotivoSuspension(datos.getMotivoSuspension());
        if (ValidadorTexto.esNoVacio(datos.getAdminIdentificacion())) existente.setAdminIdentificacion(datos.getAdminIdentificacion());
        existente.setActivo(datos.isActivo());

        return Respuesta.exito("Empresa actualizada correctamente.", existente);
    }

    public Respuesta<Empresa> cambiarEstadoEmpresa(String idEmpresa, boolean activo, String motivo) {
        Empresa existente = listaEmpresas.buscarPorId(idEmpresa != null ? idEmpresa.trim() : "");
        if (existente == null) {
            return Respuesta.error("Empresa no encontrada.", 404);
        }
        existente.setActivo(activo);
        if (!activo && ValidadorTexto.esNoVacio(motivo)) {
            existente.setMotivoSuspension(motivo);
        }
        String msg = activo
                ? "Empresa '" + existente.getNombre() + "' ACTIVADA."
                : "Empresa '" + existente.getNombre() + "' DESACTIVADA (acceso bloqueado por pago/suspensión).";
        return Respuesta.exito(msg, existente);
    }

    public Respuesta<Boolean> eliminarEmpresa(String idEmpresa) {
        if (!ValidadorTexto.esNoVacio(idEmpresa)) {
            return Respuesta.error("ID de empresa requerido.");
        }
        if (!listaEmpresas.eliminar(idEmpresa.trim())) {
            return Respuesta.error("Empresa no encontrada.", 404);
        }
        return Respuesta.exito("Empresa eliminada correctamente.", Boolean.TRUE);
    }

    public boolean estaEmpresaActiva(String empresaId) {
        if (!ValidadorTexto.esNoVacio(empresaId) || "PLATAFORMA".equalsIgnoreCase(empresaId.trim())) {
            return true;
        }
        Empresa e = listaEmpresas.buscarPorId(empresaId.trim());

        return e == null || e.isActivo();
    }

    public Empresa obtenerEmpresaDirecta(String empresaId) {
        if (!ValidadorTexto.esNoVacio(empresaId)) return null;
        return listaEmpresas.buscarPorId(empresaId.trim());
    }

    public void guardarEstado() {
        PersistenciaUtil.guardar(new Object[]{listaEmpresas, Integer.valueOf(consecutivo)}, ARCHIVO);
    }

    private void cargarEstado() {
        Object[] estado = PersistenciaUtil.cargar(ARCHIVO, Object[].class);
        if (estado == null || estado.length < 2) return;
        if (estado[0] instanceof ListaEmpresas) {
            listaEmpresas = (ListaEmpresas) estado[0];
        }
        if (estado[1] instanceof Integer) {
            consecutivo = ((Integer) estado[1]).intValue();
        }
    }
}
