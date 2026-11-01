package controlador;

import entidades.Nodos;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Planes;
import model.cliente;

/**
 *
 * @author Gyanela Meza
 */
public class crud_planes {

    // Punteros de la lista
    public static Nodos.NodoPlan primero = null;
    public static Nodos.NodoPlan ultimo = null;

    public static Nodos.NodoPlan getPrimero() {
        return primero;
    }

    // Metodo para agregar un nuevo plan
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

        if (buscarNodoPlanPorCodigo(codigo) != null) {
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

        Planes nuevo = new Planes(codigo, nombre, velocidad, precio, descripcion);
        Nodos.NodoPlan nuevoNodo = new Nodos.NodoPlan(nuevo);

        if (primero == null) {
            primero = nuevoNodo;
            ultimo = nuevoNodo;
        } else {
            ultimo.setSiguiente(nuevoNodo);
            nuevoNodo.setAnterior(ultimo);
            ultimo = nuevoNodo;
        }

        JOptionPane.showMessageDialog(null, "Plan agregado correctamente.");
        limpiarCamposAgregar(tx_cod_plan, tx_nombre_plan, tx_velocidad_plan, tx_precio_plan, tx_descripcion_plan);
    }

    // Metodo para listar los planes en la tabla
    public static void listarPlanes(JTable Tab_Plan) {
        DefaultTableModel modelo = (DefaultTableModel) Tab_Plan.getModel();
        modelo.setRowCount(0);

        Nodos.NodoPlan aux = primero;
        while (aux != null) {
            Planes p = aux.getDato();
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
            aux = aux.getSiguiente();
        }
    }

    // Metodo para buscar un plan
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

        Nodos.NodoPlan nodoEncontrado = buscarNodoPlanPorCodigo(codigo);
        if (nodoEncontrado == null) {
            JOptionPane.showMessageDialog(null, "No se encontro ningun plan con ese codigo.");
            limpiarCamposBuscar(tx_codigo_buscado, tx_nombrePlan_buscado, tx_vel_buscar, tx_precio_buscar, tx_descripcion_plan1);
            return;
        }

        Planes p = nodoEncontrado.getDato();
        tx_codigo_buscado.setText(String.valueOf(p.getIdPlan()));
        tx_nombrePlan_buscado.setText(p.getNombrePlan());
        tx_vel_buscar.setText(p.getVelocidadPlan());
        tx_precio_buscar.setText(String.valueOf((int) p.getPrecio()));
        tx_descripcion_plan1.setText(p.getDescripcionPlan());

        limpiarCampoBusqueda(tx_idPlan_buscar);
    }

    // Metodo para buscar el plan que se va a actualizar
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

        Nodos.NodoPlan nodoEncontrado = buscarNodoPlanPorCodigo(codigo);
        if (nodoEncontrado == null) {
            JOptionPane.showMessageDialog(null, "No se encontro ningun plan con ese codigo.");
            limpiarCamposBuscar(tx_cod_plan_mostrar, tx_nombre_plan_mostrar, tx_velocidad_plan_mostrar, tx_precio_plan_mostrar, tx_descripcion_plan2);
            return;
        }

        Planes p = nodoEncontrado.getDato();
        tx_cod_plan_mostrar.setText(String.valueOf(p.getIdPlan()));
        tx_nombre_plan_mostrar.setText(p.getNombrePlan());
        tx_velocidad_plan_mostrar.setText(p.getVelocidadPlan());
        tx_precio_plan_mostrar.setText(String.valueOf((int) p.getPrecio()));
        tx_descripcion_plan2.setText(p.getDescripcionPlan());

        JOptionPane.showMessageDialog(null, "Plan encontrado. Deje en blanco los campos que no desea actualizar.");
        limpiarCampoBusqueda(tx_cod_buscar_actualizar_plan);
    }

    // Metodo para actualizar el plan
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

        Nodos.NodoPlan nodoEncontrado = buscarNodoPlanPorCodigo(codigoBuscar);
        if (nodoEncontrado == null) {
            JOptionPane.showMessageDialog(null, "Plan no encontrado.");
            return;
        }

        Planes p = nodoEncontrado.getDato();

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

            if (codigoPlanExisteActualizar(nuevoCodigo, p.getIdPlan())) {
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

    // Metodo para buscar el plan que se va a eliminar
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

        Nodos.NodoPlan nodoEncontrado = buscarNodoPlanPorCodigo(codigo);
        if (nodoEncontrado == null) {
            JOptionPane.showMessageDialog(null, "Plan no encontrado.");
            limpiarCamposEliminar(tx_cod_plan_mostrar1, tx_nombre_plan_mostrar1, tx_velocidad_plan_mostrar1, tx_precio_plan_mostrar1, tx_descripcion_plan3, tx_idPlan_buscar1);
            return;
        }

        Planes p = nodoEncontrado.getDato();
        tx_cod_plan_mostrar1.setText(String.valueOf(p.getIdPlan()));
        tx_nombre_plan_mostrar1.setText(p.getNombrePlan());
        tx_velocidad_plan_mostrar1.setText(p.getVelocidadPlan());
        tx_precio_plan_mostrar1.setText(String.valueOf((int) p.getPrecio()));
        tx_descripcion_plan3.setText(p.getDescripcionPlan());

        limpiarCampoBusqueda(tx_idPlan_buscar1);
    }

    // Metodo para eliminar plan
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

        Nodos.NodoPlan nodoEliminar = buscarNodoPlanPorCodigo(codigo);
        if (nodoEliminar == null) {
            JOptionPane.showMessageDialog(null, "No se encontro el plan a eliminar.");
            return;
        }

        String nombrePlanEliminado = nodoEliminar.getDato().getNombrePlan();

        if (nodoEliminar.getAnterior() != null) {
            nodoEliminar.getAnterior().setSiguiente(nodoEliminar.getSiguiente());
        } else {
            primero = nodoEliminar.getSiguiente();
        }

        if (nodoEliminar.getSiguiente() != null) {
            nodoEliminar.getSiguiente().setAnterior(nodoEliminar.getAnterior());
        } else {
            ultimo = nodoEliminar.getAnterior();
        }

        actualizarPlanClientesTrasEliminar(nombrePlanEliminado);

        JOptionPane.showMessageDialog(null, "Plan eliminado correctamente.");
        limpiarCamposEliminar(tx_cod_plan_mostrar1, tx_nombre_plan_mostrar1, tx_velocidad_plan_mostrar1, tx_precio_plan_mostrar1, tx_descripcion_plan3, tx_idPlan_buscar1);
    }

    // Metodos auxiliares
    public static boolean codigoPlanExisteActualizar(int codigoNuevo, int codigoActual) {
        Nodos.NodoPlan aux = primero;
        while (aux != null) {
            if (aux.getDato().getIdPlan() == codigoNuevo && aux.getDato().getIdPlan() != codigoActual) {
                return true;
            }
            aux = aux.getSiguiente();
        }
        return false;
    }

    public static Planes buscarPlanPorNombre(String nombrePlan) {
        if (nombrePlan == null || nombrePlan.trim().isEmpty()) {
            return null;
        }

        String nombreBuscado = nombrePlan.trim();
        Nodos.NodoPlan aux = primero;
        while (aux != null) {
            if (aux.getDato().getNombrePlan().equalsIgnoreCase(nombreBuscado)) {
                return aux.getDato();
            }
            aux = aux.getSiguiente();
        }
        return null;
    }

    public static Planes obtenerPrimerPlanDisponible() {
        if (primero != null) {
            return primero.getDato();
        }
        return null;
    }

    public static Nodos.NodoPlan buscarNodoPlanPorCodigo(int codigo) {
        Nodos.NodoPlan aux = primero;
        while (aux != null) {
            if (aux.getDato().getIdPlan() == codigo) {
                return aux;
            }
            aux = aux.getSiguiente();
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

    private static void actualizarPlanClientesTrasEliminar(String nombrePlanEliminado) {
        if (nombrePlanEliminado == null || nombrePlanEliminado.trim().isEmpty()) {
            return;
        }

        Planes planReemplazo = obtenerPrimerPlanDisponible();
        String nombreReemplazo = (planReemplazo != null) ? planReemplazo.getNombrePlan() : "Sin plan";

        Nodos.NodoCliente auxCliente = crud_usuarios.primero;
        while (auxCliente != null) {
            cliente c = auxCliente.getDato();
            if (c != null && c.getPlanActual() != null
                    && c.getPlanActual().equalsIgnoreCase(nombrePlanEliminado)) {
                c.setPlanActual(nombreReemplazo);
            }
            auxCliente = auxCliente.getSiguiente();
        }
    }

    // Limpiar campos
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
