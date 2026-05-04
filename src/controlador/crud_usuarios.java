/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import static entidades.Arreglos.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.*;

/**
 *
 * @author Gyanela Meza
 */
public class crud_usuarios {

    public static void agregarCliente(JTextField tx_nombres, JTextField tx_apellidos, JComboBox<String> cb_tipo, JTextField tx_identificacion, JTextField tx_telefono, JTextField tx_correo, JTextField tx_direccion,
            JPasswordField tx_contrasena, JPasswordField tx_confContrasena) {

        String nombre = tx_nombres.getText().trim();
        String apellido = tx_apellidos.getText().trim();
        String tipo = cb_tipo.getSelectedItem().toString().trim();
        String identificacion = tx_identificacion.getText().trim();
        String telefono = tx_telefono.getText().trim();
        String correo = tx_correo.getText().trim();
        String direccion = tx_direccion.getText().trim();
        String contrasena = new String(tx_contrasena.getPassword());
        String confContrasena = new String(tx_confContrasena.getPassword());

        if (nombre.isEmpty() || apellido.isEmpty() || identificacion.isEmpty() || telefono.isEmpty() || correo.isEmpty() || direccion.isEmpty() || contrasena.isEmpty() || confContrasena.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor complete los campos obligatorios.");
            return;
        }
        if (!validarIdentificacion(identificacion)) {
            JOptionPane.showMessageDialog(null, "La identificación debe tener entre 8 y 10 dígitos numéricos.");
            return;
        }

        if (!validarCorreo(correo)) {
            JOptionPane.showMessageDialog(null, "El correo tiene un formato inválido.");
            return;
        }

        if (!validarTelefono(telefono)) {
            JOptionPane.showMessageDialog(null, "El teléfono debe ser numérico y tener 10 dígitos.");
            return;
        }
        if (!validarPassword(contrasena)) {
            JOptionPane.showMessageDialog(null, "La contraseña debe tener al menos 6 caracteres e incluir letra y numero.");
            return;
        }

        if (!contrasena.equals(confContrasena)) {
            JOptionPane.showMessageDialog(null, "Las contraseñas no coinciden.");
            return;
        }

        if (usuarioExiste(correo, identificacion)) {
            JOptionPane.showMessageDialog(null, "Ya existe un cliente con este correo o identificación.");
            return;
        }

        String planInicial = obtenerPlanInicialCliente();
        cliente nuevo = new cliente(nombre, apellido, tipo, identificacion, telefono, correo, direccion, contrasena, confContrasena, planInicial);

        if (contadorClientes < ELEMENTOS) {
            clientes[contadorClientes] = nuevo;
            contadorClientes++;
            JOptionPane.showMessageDialog(null, "Cliente agregado correctamente.");

            limpiarCamposAgregarCliente(tx_nombres, tx_apellidos, cb_tipo, tx_identificacion,
                    tx_telefono, tx_correo, tx_direccion, tx_contrasena, tx_confContrasena);

        } else {
            JOptionPane.showMessageDialog(null, "Error: límite de clientes alcanzado.");
        }
    }

    public static boolean validarIdentificacion(String identificacion) {
        return identificacion.length() >= 8 && identificacion.length() <= 10 && identificacion.matches("\\d+");
    }

    public static boolean validarCorreo(String correo) {
        return correo.contains("@") && correo.contains(".");
    }

    public static boolean validarTelefono(String telefono) {
        return telefono.length() == 10 && telefono.matches("\\d+");
    }

    public static boolean validarPassword(String contrasena) {
        return contrasena.length() >= 6
                && contrasena.matches(".*[A-Za-z].*")
                && contrasena.matches(".*\\d.*");
    }

    private static int buscarIndiceClientePorIdentificacion(String identificacion) {
        for (int i = 0; i < contadorClientes; i++) {
            cliente c = clientes[i];
            if (c != null && c.getIdentificacion().equals(identificacion)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean identificacionExisteEnOtroCliente(String identificacion, int indiceActual) {
        for (int i = 0; i < contadorClientes; i++) {
            if (i == indiceActual) {
                continue;
            }
            cliente c = clientes[i];
            if (c != null && c.getIdentificacion().equals(identificacion)) {
                return true;
            }
        }
        return false;
    }

    private static boolean correoExisteEnOtroCliente(String correo, int indiceActual) {
        for (int i = 0; i < contadorClientes; i++) {
            if (i == indiceActual) {
                continue;
            }
            cliente c = clientes[i];
            if (c != null && c.getCorreo().equals(correo)) {
                return true;
            }
        }
        return false;
    }

    public static boolean usuarioExiste(String correo, String identificacion) {
        for (int i = 0; i < contadorClientes; i++) {
            cliente c = clientes[i];
            if (c != null && (c.getCorreo().equals(correo) || c.getIdentificacion().equals(identificacion))) {
                return true;
            }
        }
        return false;

    }

    public static boolean correoExiste(String correo) {
        for (int i = 0; i < contadorClientes; i++) {
            cliente c = clientes[i];
            if (c != null && (c.getCorreo().equals(correo))) {
                return true;
            }
        }
        return false;

    }

    public static void asignarPlanACliente(JTextField tx_identificacion_cliente, JTextField tx_nombre_plan) {
        String identificacion = tx_identificacion_cliente.getText().trim();
        String nombrePlan = tx_nombre_plan.getText().trim();

        if (identificacion.isEmpty() || nombrePlan.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingrese identificacion del cliente y nombre del plan.");
            return;
        }

        int indiceCliente = buscarIndiceClientePorIdentificacion(identificacion);
        if (indiceCliente == -1) {
            JOptionPane.showMessageDialog(null, "No se encontro cliente con esa identificacion.");
            return;
        }

        Planes planSeleccionado = crud_planes.buscarPlanPorNombre(nombrePlan);
        if (planSeleccionado == null) {
            JOptionPane.showMessageDialog(null, "No existe un plan con ese nombre.");
            return;
        }

        clientes[indiceCliente].setPlanActual(planSeleccionado);
        JOptionPane.showMessageDialog(null, "Plan asignado correctamente al cliente.");
    }

    public static void listarClientes(JTable tabla) {
        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        modelo.setRowCount(0);

        for (int i = 0; i < contadorClientes; i++) {
            cliente c = clientes[i];
            if (c != null) {
                Object[] fila = {
                    c.getTipo(),
                    c.getIdentificacion(),
                    c.getNombre(),
                    c.getApellido()
                };
                modelo.addRow(fila);
            }
        }
    }

    public static void buscarClienteEliminar(
            JTextField tx_docu_buscar_elim,
            JTextField tx_nombre_eliminar,
            JTextField tx_apellido_eliminar,
            JTextField tx_tipo_eliminar,
            JTextField tx_documento_eliminar,
            JTextField tx_telefono_eliminar,
            JTextField tx_correo_eliminar,
            JTextField tx_direccion_eliminar
    ) {
        String docBuscar = tx_docu_buscar_elim.getText().trim();
        int indice = buscarIndiceClientePorIdentificacion(docBuscar);

        if (indice != -1) {
            cliente c = clientes[indice];
            tx_nombre_eliminar.setText(c.getNombre());
            tx_apellido_eliminar.setText(c.getApellido());
            tx_tipo_eliminar.setText(c.getTipo());
            tx_documento_eliminar.setText(c.getIdentificacion());
            tx_telefono_eliminar.setText(c.getTelefono());
            tx_correo_eliminar.setText(c.getCorreo());
            tx_direccion_eliminar.setText(c.getDireccion());
            
            // Limpiar el campo de búsqueda después de encontrar el cliente
            tx_docu_buscar_elim.setText("");
        } else {
            JOptionPane.showMessageDialog(null, "Cliente no encontrado");
            limpiarCamposClienteEliminar(tx_documento_eliminar, tx_nombre_eliminar, tx_apellido_eliminar,
                    tx_tipo_eliminar, tx_telefono_eliminar, tx_correo_eliminar, tx_direccion_eliminar, tx_docu_buscar_elim);
        }
    }

    public static void eliminarCliente(JTextField tx_documento_eliminar, JTextField tx_nombre_eliminar, JTextField tx_apellido_eliminar,
            JTextField tx_tipo_eliminar, JTextField tx_telefono_eliminar, JTextField tx_correo_eliminar, JTextField tx_direccion_eliminar,
            JTextField tx_docu_buscar_elim) {
        String docEliminar = tx_documento_eliminar.getText().trim();

        if (docEliminar.isEmpty()) {
            docEliminar = tx_docu_buscar_elim.getText().trim();
        }

        if (docEliminar.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingrese un documento para eliminar.");
            return;
        }

        int indiceEliminar = buscarIndiceClientePorIdentificacion(docEliminar);

        if (indiceEliminar != -1) {
            for (int j = indiceEliminar; j < contadorClientes - 1; j++) {
                clientes[j] = clientes[j + 1];
            }
            clientes[contadorClientes - 1] = null;
            contadorClientes--;
            JOptionPane.showMessageDialog(null, "Cliente eliminado correctamente");
        } else {
            JOptionPane.showMessageDialog(null, "No se encontró el cliente a eliminar");
        }

        limpiarCamposClienteEliminar(tx_documento_eliminar, tx_nombre_eliminar, tx_apellido_eliminar,
            tx_tipo_eliminar, tx_telefono_eliminar, tx_correo_eliminar, tx_direccion_eliminar, tx_docu_buscar_elim);
    }

    public static void buscarClienteActualizar(
            JTextField tx_docu_buscar_actualizar,
            JTextField tx_nombreCambiar,
            JTextField tx_apellidoCambiar,
            JTextField tx_tipo1,
            JTextField tx_identificacionCambiar,
            JTextField tx_telefonoCambiar,
            JTextField tx_correoCambiar,
            JTextField tx_direccionCambiar
    ) {
        String docBuscar = tx_docu_buscar_actualizar.getText().trim();

        if (docBuscar.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingrese el documento del cliente a buscar.");
            return;
        }

        int indice = buscarIndiceClientePorIdentificacion(docBuscar);
        if (indice == -1) {
            JOptionPane.showMessageDialog(null, "No se encontró ningún cliente con ese documento.");
            limpiarCamposClienteActualizar(tx_nombreCambiar, tx_apellidoCambiar, tx_tipo1,
                    tx_identificacionCambiar, tx_telefonoCambiar, tx_correoCambiar, tx_direccionCambiar, tx_docu_buscar_actualizar);
            return;
        }

        cliente c = clientes[indice];
        tx_nombreCambiar.setText(c.getNombre());
        tx_apellidoCambiar.setText(c.getApellido());
        tx_tipo1.setText(c.getTipo());
        tx_identificacionCambiar.setText(c.getIdentificacion());
        tx_telefonoCambiar.setText(c.getTelefono());
        tx_correoCambiar.setText(c.getCorreo());
        tx_direccionCambiar.setText(c.getDireccion());

        // Limpiar el campo de búsqueda después de encontrar el cliente
        tx_docu_buscar_actualizar.setText("");

        JOptionPane.showMessageDialog(null, "Cliente encontrado. Puede modificar los datos y presionar 'Actualizar'.");
    }

    public static void actualizarCliente(
        JTextField tx_docu_buscar_actualizar, JTextField tx_nombreCambiar,
        JTextField tx_apellidosCambiar,
        JTextField tx_tipoDocuCambiar,
        JTextField tx_identificacionCambiar,
        JTextField tx_telefonoCambiar,
        JTextField tx_correoCambiar,
        JTextField tx_direccionCambiar,
        JTextField tx_nombreCambiar1,
        JTextField tx_apellidosCambiar1,
        JComboBox<String> jtipo_cambiar,
        JTextField tx_identificacionCambiar1,
        JTextField tx_telefonoCambiar1,
        JTextField tx_correoCambiar1,
        JTextField tx_direccionCambiar1,
        JPasswordField tx_passwordCambiar1
) {
    String docBuscar = tx_identificacionCambiar.getText().trim();

    if (docBuscar.isEmpty()) {
        docBuscar = tx_docu_buscar_actualizar.getText().trim();
    }

    if (docBuscar.isEmpty()) {
        JOptionPane.showMessageDialog(null, "Por favor, busque un cliente antes de actualizar.");
        return;
    }

    int indice = buscarIndiceClientePorIdentificacion(docBuscar);
    if (indice == -1) {
        JOptionPane.showMessageDialog(null, "Cliente no encontrado.");
        return;
    }

    cliente c = clientes[indice];

    String nuevoNombre = tx_nombreCambiar1.getText().trim();
    String nuevoApellido = tx_apellidosCambiar1.getText().trim();
    String nuevoTipo = (jtipo_cambiar.getSelectedItem() != null)
            ? jtipo_cambiar.getSelectedItem().toString().trim()
            : "";
    String nuevoIdentificacion = tx_identificacionCambiar1.getText().trim();
    String nuevoTelefono = tx_telefonoCambiar1.getText().trim();
    String nuevoCorreo = tx_correoCambiar1.getText().trim();
    String nuevoDireccion = tx_direccionCambiar1.getText().trim();
    String nuevoPassword = new String(tx_passwordCambiar1.getPassword()).trim();

    if (!nuevoIdentificacion.isEmpty() && !nuevoIdentificacion.equals(c.getIdentificacion())) {
        if (!validarIdentificacion(nuevoIdentificacion)) {
            JOptionPane.showMessageDialog(null, "La nueva identificación no es válida.");
            return;
        }
        if (identificacionExisteEnOtroCliente(nuevoIdentificacion, indice)) {
            JOptionPane.showMessageDialog(null, "La identificación ya está registrada en otro cliente.");
            return;
        }
        c.setIdentificacion(nuevoIdentificacion);
    }

    if (!nuevoNombre.isEmpty()) {
        c.setNombre(nuevoNombre);
    }
    if (!nuevoApellido.isEmpty()) {
        c.setApellido(nuevoApellido);
    }
    if (!nuevoTipo.isEmpty()) {
        c.setTipo(nuevoTipo);
    }
    if (!nuevoTelefono.isEmpty()) {
        if (!validarTelefono(nuevoTelefono)) {
            JOptionPane.showMessageDialog(null, "El teléfono debe ser numérico y tener 10 dígitos.");
            return;
        }
        c.setTelefono(nuevoTelefono);
    }
    if (!nuevoCorreo.isEmpty()) {
        if (!validarCorreo(nuevoCorreo)) {
            JOptionPane.showMessageDialog(null, "El correo tiene un formato inválido.");
            return;
        }
        if (correoExisteEnOtroCliente(nuevoCorreo, indice)) {
            JOptionPane.showMessageDialog(null, "Ya existe otro cliente con este correo.");
            return;
        }
        c.setCorreo(nuevoCorreo);
    }
    if (!nuevoDireccion.isEmpty()) {
        c.setDireccion(nuevoDireccion);
    }
    if (!nuevoPassword.isEmpty()) {
        if (!validarPassword(nuevoPassword)) {
            JOptionPane.showMessageDialog(null, "La contraseña debe tener al menos 6 caracteres e incluir letra y numero.");
            return;
        }
        c.setContrasena(nuevoPassword);
    }

    JOptionPane.showMessageDialog(null, "Cliente actualizado correctamente.");

    tx_nombreCambiar.setText(c.getNombre());
    tx_apellidosCambiar.setText(c.getApellido());
    jtipo_cambiar.setSelectedItem(c.getTipo());
    tx_identificacionCambiar.setText(c.getIdentificacion());
    tx_telefonoCambiar.setText(c.getTelefono());
    tx_correoCambiar.setText(c.getCorreo());
    tx_direccionCambiar.setText(c.getDireccion());
    limpiarCamposEdicionActualizar(tx_nombreCambiar1, tx_apellidosCambiar1, jtipo_cambiar,
            tx_identificacionCambiar1, tx_telefonoCambiar1, tx_correoCambiar1, tx_direccionCambiar1, tx_passwordCambiar1);
}
    

    public static void buscarCliente(JTextField tx_docu_buscar, JTextField tx_nombre_buscar, JTextField tx_apellido_buscar, JTextField tx_tipo_buscar,
            JTextField tx_documento_buscar, JTextField tx_telefono_buscar, JTextField tx_correo_buscar, JTextField tx_direccion_buscar) {

        String identificacion = tx_docu_buscar.getText().trim();

        if (identificacion.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor ingrese la identificación del cliente a buscar.");
            return;
        }

        boolean encontrado = false;

        for (int i = 0; i < contadorClientes; i++) {
            cliente c = clientes[i];
            if (c != null && c.getIdentificacion().equalsIgnoreCase(identificacion)) {

                tx_nombre_buscar.setText(c.getNombre());
                tx_apellido_buscar.setText(c.getApellido());
                tx_tipo_buscar.setText(c.getTipo());
                tx_documento_buscar.setText(c.getIdentificacion());
                tx_telefono_buscar.setText(c.getTelefono());
                tx_correo_buscar.setText(c.getCorreo());
                tx_direccion_buscar.setText(c.getDireccion());

                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            JOptionPane.showMessageDialog(null, "No se encontró ningún cliente con esa identificación.");

            limpiarCamposBuscarCliente(tx_nombre_buscar, tx_apellido_buscar, tx_tipo_buscar,
                    tx_documento_buscar, tx_telefono_buscar, tx_correo_buscar, tx_direccion_buscar, tx_docu_buscar);
            return;
        }

        tx_docu_buscar.setText("");
    }

    public static void listarCliente(JTable Tab_Client) {
        DefaultTableModel modelo = (DefaultTableModel) Tab_Client.getModel();
        modelo.setRowCount(0); 

        boolean hayClientes = false;

        for (int i = 0; i < contadorClientes; i++) {
            if (clientes[i] != null) {
                modelo.addRow(new Object[]{
                    clientes[i].getTipo(),
                    clientes[i].getIdentificacion(),
                    clientes[i].getNombre(),
                    clientes[i].getApellido(),
                    clientes[i].getTelefono(),
                    clientes[i].getCorreo(),
                    clientes[i].getDireccion()
                });
                hayClientes = true;
            }
        }

        if (!hayClientes) {
            JOptionPane.showMessageDialog(null, "No hay clientes registrados.");
        }
    }

    private static String obtenerPlanInicialCliente() {
        Planes planInicial = crud_planes.obtenerPrimerPlanDisponible();
        if (planInicial != null) {
            return planInicial.getNombrePlan();
        }
        return "Sin plan";
    }

    private static void limpiarCamposAgregarCliente(JTextField tx_nombres, JTextField tx_apellidos, JComboBox<String> cb_tipo,
            JTextField tx_identificacion, JTextField tx_telefono, JTextField tx_correo, JTextField tx_direccion,
            JPasswordField tx_contrasena, JPasswordField tx_confContrasena) {
        tx_nombres.setText("");
        tx_apellidos.setText("");
        cb_tipo.setSelectedIndex(0);
        tx_identificacion.setText("");
        tx_telefono.setText("");
        tx_correo.setText("");
        tx_direccion.setText("");
        tx_contrasena.setText("");
        tx_confContrasena.setText("");
    }

    private static void limpiarCamposClienteEliminar(JTextField tx_documento_eliminar, JTextField tx_nombre_eliminar,
            JTextField tx_apellido_eliminar, JTextField tx_tipo_eliminar, JTextField tx_telefono_eliminar,
            JTextField tx_correo_eliminar, JTextField tx_direccion_eliminar, JTextField tx_docu_buscar_elim) {
        tx_documento_eliminar.setText("");
        tx_nombre_eliminar.setText("");
        tx_apellido_eliminar.setText("");
        tx_tipo_eliminar.setText("");
        tx_telefono_eliminar.setText("");
        tx_correo_eliminar.setText("");
        tx_direccion_eliminar.setText("");
        tx_docu_buscar_elim.setText("");
    }

    private static void limpiarCamposClienteActualizar(JTextField tx_nombreCambiar, JTextField tx_apellidoCambiar,
            JTextField tx_tipo1, JTextField tx_identificacionCambiar, JTextField tx_telefonoCambiar,
            JTextField tx_correoCambiar, JTextField tx_direccionCambiar, JTextField tx_docu_buscar_actualizar) {
        tx_nombreCambiar.setText("");
        tx_apellidoCambiar.setText("");
        tx_tipo1.setText("");
        tx_identificacionCambiar.setText("");
        tx_telefonoCambiar.setText("");
        tx_correoCambiar.setText("");
        tx_direccionCambiar.setText("");
        tx_docu_buscar_actualizar.setText("");
    }

    private static void limpiarCamposEdicionActualizar(JTextField tx_nombreCambiar1, JTextField tx_apellidosCambiar1,
            JComboBox<String> jtipo_cambiar, JTextField tx_identificacionCambiar1, JTextField tx_telefonoCambiar1,
            JTextField tx_correoCambiar1, JTextField tx_direccionCambiar1, JPasswordField tx_passwordCambiar1) {
        tx_nombreCambiar1.setText("");
        tx_apellidosCambiar1.setText("");
        if (jtipo_cambiar.getItemCount() > 0) {
            jtipo_cambiar.setSelectedIndex(0);
        }
        tx_identificacionCambiar1.setText("");
        tx_telefonoCambiar1.setText("");
        tx_correoCambiar1.setText("");
        tx_direccionCambiar1.setText("");
        tx_passwordCambiar1.setText("");
    }

    private static void limpiarCamposBuscarCliente(JTextField tx_nombre_buscar, JTextField tx_apellido_buscar,
            JTextField tx_tipo_buscar, JTextField tx_documento_buscar, JTextField tx_telefono_buscar,
            JTextField tx_correo_buscar, JTextField tx_direccion_buscar, JTextField tx_docu_buscar) {
        tx_nombre_buscar.setText("");
        tx_apellido_buscar.setText("");
        tx_tipo_buscar.setText("");
        tx_documento_buscar.setText("");
        tx_telefono_buscar.setText("");
        tx_correo_buscar.setText("");
        tx_direccion_buscar.setText("");
        tx_docu_buscar.setText("");
    }

    public static cliente consultaDetallada(String identificacion) {
        if (identificacion == null || identificacion.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingrese la identificación del cliente.");
            return null;
        }

        String idBuscada = identificacion.trim();
        for (int i = 0; i < contadorClientes; i++) {
            cliente c = clientes[i];
            if (c != null && c.getIdentificacion().equals(idBuscada)) {
                return c;
            }
        }

        JOptionPane.showMessageDialog(null, "No se encontró un cliente con esa identificación.");
        return null;
    }


    ///////////////////////////
}
