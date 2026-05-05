package controlador;

import static entidades.Arreglos.*;
import java.time.LocalDate;
import javax.swing.JOptionPane;
import model.*;

/**
 * @author Gyanela Meza
 */
public class ctrlAsignacionPlan {

    private static crud_usuarios crudUsuarios = new crud_usuarios();
    private static crud_planes crudPlanes = new crud_planes();

    public static class ResultadoAsignacion {
        public boolean exito;
        public String mensaje;
        public cliente clienteAsignado;
        public Planes planAsignado;
        public LocalDate fechaAsignacion;
        public String nombreCompleto;
        public String rutaPDF;

        public ResultadoAsignacion(boolean exito, String mensaje) {
            this.exito = exito;
            this.mensaje = mensaje;
        }
    }

    public static ResultadoAsignacion asignarPlanACliente(String identificacionCliente, String nombrePlan) {
        if (identificacionCliente == null || identificacionCliente.trim().isEmpty()) {
            return new ResultadoAsignacion(false, "Ingrese la identificación del cliente.");
        }

        if (nombrePlan == null || nombrePlan.trim().isEmpty()) {
            return new ResultadoAsignacion(false, "Seleccione un plan válido.");
        }

        cliente clienteEncontrado = crudUsuarios.consultaDetallada(identificacionCliente);
        if (clienteEncontrado == null) {
            return new ResultadoAsignacion(false, "No se encontró un cliente con esa identificación.");
        }

        Planes planEncontrado = crudPlanes.buscarPlanPorNombre(nombrePlan);
        if (planEncontrado == null) {
            return new ResultadoAsignacion(false, "No se encontró el plan seleccionado.");
        }

        clienteEncontrado.setPlanActual(planEncontrado);
        LocalDate fechaAsignacion = LocalDate.now();
        planEncontrado.setFechaActivacion(fechaAsignacion.toString());

        ResultadoAsignacion resultado = new ResultadoAsignacion(true, 
            "Plan asignado correctamente al cliente " + clienteEncontrado.getNombre() + " (fecha: " + fechaAsignacion + ").");

        resultado.clienteAsignado = clienteEncontrado;
        resultado.planAsignado = planEncontrado;
        resultado.fechaAsignacion = fechaAsignacion;
        resultado.nombreCompleto = clienteEncontrado.getNombre() + " " + clienteEncontrado.getApellido();

        return resultado;
    }
}
