/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import static entidades.Arreglos.*;
import javax.swing.*;
import javax.swing.table.*;
import model.*;

/**
 *
 * @author Gyanela Meza
 */
public class crud_planes {
    public static void agregarPlan(JTextField tx_cod_plan, JTextField tx_nombre_plan,
            JTextField tx_velocidad_plan, JTextField tx_precio_plan,
            JTextPane tx_descripcion_plan) {

        String codigoStr = tx_cod_plan.getText().trim();
        String nombre = tx_nombre_plan.getText().trim();
        String velocidad = tx_velocidad_plan.getText().trim();
        String precioStr = tx_precio_plan.getText().trim();
        String descripcion = tx_descripcion_plan.getText().trim();

        if (codigoStr.isEmpty() || nombre.isEmpty() || velocidad.isEmpty()
                || precioStr.isEmpty() || descripcion.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Debe completar todos los campos del plan.");
            return;
        }

        Integer codigo = parseNumero(codigoStr);
        if (codigo == null) {
            JOptionPane.showMessageDialog(null, "El codigo debe ser numerico.");
            return;
        }

        if (codigo <= 0) {
            JOptionPane.showMessageDialog(null, "El codigo debe ser mayor que 0.");
            return;
        }

        if (buscarIndicePlanPorCodigo(codigo) != -1) {
            JOptionPane.showMessageDialog(null, "Ya existe un plan con este codigo.");
            return;
        }

        Integer precio = parseNumero(precioStr);
        if (precio == null) {
            JOptionPane.showMessageDialog(null, "El precio debe ser numerico.");
            return;
        }

        if (precio < 0) {
            JOptionPane.showMessageDialog(null, "El precio no puede ser negativo.");
            return;
        }

        if (contadorPlanes >= ELEMENTOS) {
            JOptionPane.showMessageDialog(null, "Limite de planes alcanzado.");
            return;
        }

        Planes nuevo = new Planes(codigo, nombre, velocidad, precio, descripcion);
        plan[contadorPlanes] = nuevo;
        contadorPlanes++;

        JOptionPane.showMessageDialog(null, "Plan agregado correctamente.");
        limpiarCamposAgregar(tx_cod_plan, tx_nombre_plan, tx_velocidad_plan, tx_precio_plan, tx_descripcion_plan);
    }

    public static void listarPlanes(JTable Tab_Plan) {

        DefaultTableModel modelo = (DefaultTableModel) Tab_Plan.getModel();
        modelo.setRowCount(0);

        for (int i = 0; i < contadorPlanes; i++) {
            Planes p = plan[i];

            if (p != null) {
                Object[] fila = {
                        p.getIdPlan(),
                        p.getNombrePlan(),
                        p.getVelocidadPlan(),
                        p.getPrecio(),
                        p.getDescripcionPlan()
                };
                modelo.addRow(fila);
            }
        }
    }

    public static void buscarPlan(JTextField tx_idPlan_buscar, JTextField tx_codigo_buscado,
            JTextField tx_nombrePlan_buscado, JTextField tx_vel_buscar,
            JTextField tx_precio_buscar, JTextPane tx_descripcion_plan1) {

        String docBuscar = tx_idPlan_buscar.getText().trim();
        if (docBuscar.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingrese el codigo del plan.");
            return;
        }

        Integer codigo = parseNumero(docBuscar);
        if (codigo == null) {
            JOptionPane.showMessageDialog(null, "El codigo debe ser numerico.");
            return;
        }

        int indice = buscarIndicePlanPorCodigo(codigo);
        if (indice == -1) {
            JOptionPane.showMessageDialog(null, "No se encontro ningun plan con ese codigo.");
            limpiarCamposBuscar(tx_codigo_buscado, tx_nombrePlan_buscado, tx_vel_buscar, tx_precio_buscar, tx_descripcion_plan1);
            return;
        }

        Planes p = plan[indice];
        tx_codigo_buscado.setText(String.valueOf(p.getIdPlan()));
        tx_nombrePlan_buscado.setText(p.getNombrePlan());
        tx_vel_buscar.setText(p.getVelocidadPlan());
        tx_precio_buscar.setText(String.valueOf((int) p.getPrecio()));
        tx_descripcion_plan1.setText(p.getDescripcionPlan());

        limpiarCampoBusqueda(tx_idPlan_buscar);
    }

    public static void buscarPlanActualizar(JTextField tx_cod_buscar_actualizar_plan,
            JTextField tx_cod_plan_mostrar,
            JTextField tx_nombre_plan_mostrar,
            JTextField tx_velocidad_plan_mostrar,
            JTextField tx_precio_plan_mostrar,
            JTextPane tx_descripcion_plan2) {

        String docBuscar = tx_cod_buscar_actualizar_plan.getText().trim();
        if (docBuscar.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingrese el codigo del plan.");
            return;
        }

        Integer codigo = parseNumero(docBuscar);
        if (codigo == null) {
            JOptionPane.showMessageDialog(null, "El codigo debe ser numerico.");
            return;
        }

        int indice = buscarIndicePlanPorCodigo(codigo);
        if (indice == -1) {
            JOptionPane.showMessageDialog(null, "No se encontro ningun plan con ese codigo.");
            limpiarCamposBuscar(tx_cod_plan_mostrar, tx_nombre_plan_mostrar, tx_velocidad_plan_mostrar, tx_precio_plan_mostrar, tx_descripcion_plan2);
            return;
        }

        Planes p = plan[indice];
        tx_cod_plan_mostrar.setText(String.valueOf(p.getIdPlan()));
        tx_nombre_plan_mostrar.setText(p.getNombrePlan());
        tx_velocidad_plan_mostrar.setText(p.getVelocidadPlan());
        tx_precio_plan_mostrar.setText(String.valueOf((int) p.getPrecio()));
        tx_descripcion_plan2.setText(p.getDescripcionPlan());

        JOptionPane.showMessageDialog(null, "Plan encontrado. Deje en blanco los campos que no desea actualizar.");
        limpiarCampoBusqueda(tx_cod_buscar_actualizar_plan);
    }

    public static void actualizarPlan(JTextField tx_cod_plan_mostrar,
            JTextField tx_nombre_plan_mostrar,
            JTextField tx_velocidad_plan_mostrar,
            JTextField tx_precio_plan_mostrar,
            JTextPane tx_descripcion_plan2,
            JTextField tx_cod_buscar_actualizar_plan,
            JTextField tx_cod_plan_act,
            JTextField tx_nombre_plan_act,
            JTextField tx_velocidad_plan_act,
            JTextField tx_precio_plan_act,
            JTextPane tx_descripcion_plan_act) {
        String docBuscar = tx_cod_plan_mostrar.getText().trim();

        if (docBuscar.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Busque un plan antes de actualizar.");
            return;
        }

        Integer codigoBuscar = parseNumero(docBuscar);
        if (codigoBuscar == null) {
            JOptionPane.showMessageDialog(null, "El codigo debe ser numerico.");
            return;
        }

        int indice = buscarIndicePlanPorCodigo(codigoBuscar);
        if (indice == -1) {
            JOptionPane.showMessageDialog(null, "Plan no encontrado.");
            return;
        }

        Planes p = plan[indice];

        String nuevoCodigoStr = tx_cod_plan_act.getText().trim();
        String nuevoNombre = tx_nombre_plan_act.getText().trim();
        String nuevaVelocidad = tx_velocidad_plan_act.getText().trim();
        String nuevoPrecioStr = tx_precio_plan_act.getText().trim();
        String nuevaDescripcion = tx_descripcion_plan_act.getText().trim();

        int codigoFinal = p.getIdPlan();
        int precioFinal = (int) p.getPrecio();

        if (!nuevoCodigoStr.isEmpty()) {
            Integer nuevoCodigo = parseNumero(nuevoCodigoStr);
            if (nuevoCodigo == null) {
                JOptionPane.showMessageDialog(null, "El codigo debe ser numerico.");
                return;
            }
            if (nuevoCodigo <= 0) {
                JOptionPane.showMessageDialog(null, "El codigo debe ser mayor que 0.");
                return;
            }

            if (codigoPlanExisteActualizar(nuevoCodigo, indice)) {
                JOptionPane.showMessageDialog(null, "Ya existe un plan con este codigo.");
                return;
            }
            codigoFinal = nuevoCodigo;
        }

        if (!nuevoPrecioStr.isEmpty()) {
            Integer nuevoPrecio = parseNumero(nuevoPrecioStr);
            if (nuevoPrecio == null) {
                JOptionPane.showMessageDialog(null, "El precio debe ser numerico.");
                return;
            }
            if (nuevoPrecio < 0) {
                JOptionPane.showMessageDialog(null, "El precio no puede ser negativo.");
                return;
            }
            precioFinal = nuevoPrecio;
        }

        String nombreFinal = nuevoNombre.isEmpty() ? p.getNombrePlan() : nuevoNombre;
        String velocidadFinal = nuevaVelocidad.isEmpty() ? p.getVelocidadPlan() : nuevaVelocidad;
        String descripcionFinal = nuevaDescripcion.isEmpty() ? p.getDescripcionPlan() : nuevaDescripcion;

        p.setIdPlan(codigoFinal);
        p.setNombrePlan(nombreFinal);
        p.setVelocidadPlan(velocidadFinal);
        p.setPrecio(precioFinal);
        p.setDescripcionPlan(descripcionFinal);

        JOptionPane.showMessageDialog(null, "Plan actualizado correctamente.");

        tx_cod_plan_mostrar.setText(String.valueOf(p.getIdPlan()));
        tx_nombre_plan_mostrar.setText(p.getNombrePlan());
        tx_velocidad_plan_mostrar.setText(p.getVelocidadPlan());
        tx_precio_plan_mostrar.setText(String.valueOf((int) p.getPrecio()));
        tx_descripcion_plan2.setText(p.getDescripcionPlan());

        limpiarCamposActualizar(tx_cod_plan_act, tx_nombre_plan_act, tx_velocidad_plan_act, tx_precio_plan_act, tx_descripcion_plan_act);
    }

    public static void buscarPlanEliminar(JTextField tx_idPlan_buscar1,
            JTextField tx_cod_plan_mostrar1,
            JTextField tx_nombre_plan_mostrar1,
            JTextField tx_velocidad_plan_mostrar1,
            JTextField tx_precio_plan_mostrar1,
            JTextPane tx_descripcion_plan3) {
        String docBuscar = tx_idPlan_buscar1.getText().trim();
        if (docBuscar.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingrese el codigo del plan.");
            return;
        }

        Integer codigo = parseNumero(docBuscar);
        if (codigo == null) {
            JOptionPane.showMessageDialog(null, "El codigo debe ser numerico.");
            return;
        }

        int indice = buscarIndicePlanPorCodigo(codigo);
        if (indice == -1) {
            JOptionPane.showMessageDialog(null, "Plan no encontrado.");
            limpiarCamposEliminar(tx_cod_plan_mostrar1, tx_nombre_plan_mostrar1, tx_velocidad_plan_mostrar1, tx_precio_plan_mostrar1, tx_descripcion_plan3, tx_idPlan_buscar1);
            return;
        }

        Planes p = plan[indice];
        tx_cod_plan_mostrar1.setText(String.valueOf(p.getIdPlan()));
        tx_nombre_plan_mostrar1.setText(p.getNombrePlan());
        tx_velocidad_plan_mostrar1.setText(p.getVelocidadPlan());
        tx_precio_plan_mostrar1.setText(String.valueOf((int) p.getPrecio()));
        tx_descripcion_plan3.setText(p.getDescripcionPlan());
        
        // Limpiar el campo de búsqueda después de encontrar el plan
        limpiarCampoBusqueda(tx_idPlan_buscar1);
    }

    public static void eliminarPlan(JTextField tx_cod_plan_mostrar1,
            JTextField tx_nombre_plan_mostrar1,
            JTextField tx_velocidad_plan_mostrar1,
            JTextField tx_precio_plan_mostrar1,
            JTextPane tx_descripcion_plan3,
            JTextField tx_idPlan_buscar1) {
        String codigoEliminar = tx_cod_plan_mostrar1.getText().trim();
        if (codigoEliminar.isEmpty()) {
            codigoEliminar = tx_idPlan_buscar1.getText().trim();
        }

        if (codigoEliminar.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingrese el codigo del plan.");
            return;
        }

        Integer codigo = parseNumero(codigoEliminar);
        if (codigo == null) {
            JOptionPane.showMessageDialog(null, "El codigo debe ser numerico.");
            return;
        }

        int indiceEliminar = buscarIndicePlanPorCodigo(codigo);
        if (indiceEliminar == -1) {
            JOptionPane.showMessageDialog(null, "No se encontro el plan a eliminar.");
            return;
        }

        String nombrePlanEliminado = plan[indiceEliminar].getNombrePlan();

        for (int j = indiceEliminar; j < contadorPlanes - 1; j++) {
            plan[j] = plan[j + 1];
        }
        plan[contadorPlanes - 1] = null;
        contadorPlanes--;

        actualizarPlanClientesTrasEliminar(nombrePlanEliminado);

        JOptionPane.showMessageDialog(null, "Plan eliminado correctamente.");
        limpiarCamposEliminar(tx_cod_plan_mostrar1, tx_nombre_plan_mostrar1, tx_velocidad_plan_mostrar1, tx_precio_plan_mostrar1, tx_descripcion_plan3, tx_idPlan_buscar1);
    }

    public static boolean codigoPlanExisteActualizar(int codigoNuevo, int indiceActual) {
        for (int i = 0; i < contadorPlanes; i++) {
            if (plan[i] != null && plan[i].getIdPlan() == codigoNuevo && i != indiceActual) {
                return true;
            }
        }
        return false;
    }

    public static Planes buscarPlanPorNombre(String nombrePlan) {
        if (nombrePlan == null || nombrePlan.trim().isEmpty()) {
            return null;
        }

        String nombreBuscado = nombrePlan.trim();
        for (int i = 0; i < contadorPlanes; i++) {
            Planes p = plan[i];
            if (p != null && p.getNombrePlan().equalsIgnoreCase(nombreBuscado)) {
                return p;
            }
        }
        return null;
    }

    public static Planes obtenerPrimerPlanDisponible() {
        for (int i = 0; i < contadorPlanes; i++) {
            if (plan[i] != null) {
                return plan[i];
            }
        }
        return null;
    }

    private static Integer parseNumero(String valor) {
        try {
            return Integer.valueOf(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int buscarIndicePlanPorCodigo(int codigo) {
        for (int i = 0; i < contadorPlanes; i++) {
            Planes p = plan[i];
            if (p != null && p.getIdPlan() == codigo) {
                return i;
            }
        }
        return -1;
    }

    private static void actualizarPlanClientesTrasEliminar(String nombrePlanEliminado) {
        if (nombrePlanEliminado == null || nombrePlanEliminado.trim().isEmpty()) {
            return;
        }

        Planes planReemplazo = obtenerPrimerPlanDisponible();
        String nombreReemplazo = (planReemplazo != null) ? planReemplazo.getNombrePlan() : "Sin plan";

        for (int i = 0; i < contadorClientes; i++) {
            cliente c = clientes[i];
            if (c != null && c.getPlanActual() != null
                    && c.getPlanActual().equalsIgnoreCase(nombrePlanEliminado)) {
                c.setPlanActual(nombreReemplazo);
            }
        }
    }

    private static void limpiarCamposAgregar(JTextField tx_cod_plan, JTextField tx_nombre_plan,
            JTextField tx_velocidad_plan, JTextField tx_precio_plan, JTextPane tx_descripcion_plan) {
        tx_cod_plan.setText("");
        tx_nombre_plan.setText("");
        tx_velocidad_plan.setText("");
        tx_precio_plan.setText("");
        tx_descripcion_plan.setText("");
    }

    private static void limpiarCamposBuscar(JTextField tx_codigo_buscado, JTextField tx_nombrePlan_buscado,
            JTextField tx_vel_buscar, JTextField tx_precio_buscar, JTextPane tx_descripcion_plan1) {
        tx_codigo_buscado.setText("");
        tx_nombrePlan_buscado.setText("");
        tx_vel_buscar.setText("");
        tx_precio_buscar.setText("");
        tx_descripcion_plan1.setText("");
    }

    private static void limpiarCamposActualizar(JTextField tx_cod_plan_act, JTextField tx_nombre_plan_act,
            JTextField tx_velocidad_plan_act, JTextField tx_precio_plan_act, JTextPane tx_descripcion_plan_act) {
        tx_cod_plan_act.setText("");
        tx_nombre_plan_act.setText("");
        tx_velocidad_plan_act.setText("");
        tx_precio_plan_act.setText("");
        tx_descripcion_plan_act.setText("");
    }

    private static void limpiarCamposEliminar(JTextField tx_cod_plan_mostrar1, JTextField tx_nombre_plan_mostrar1,
            JTextField tx_velocidad_plan_mostrar1, JTextField tx_precio_plan_mostrar1, JTextPane tx_descripcion_plan3,
            JTextField tx_idPlan_buscar1) {
        tx_cod_plan_mostrar1.setText("");
        tx_nombre_plan_mostrar1.setText("");
        tx_velocidad_plan_mostrar1.setText("");
        tx_precio_plan_mostrar1.setText("");
        tx_descripcion_plan3.setText("");
        tx_idPlan_buscar1.setText("");
    }

    private static void limpiarCampoBusqueda(JTextField campoBusqueda) {
        campoBusqueda.setText("");
    }

}
