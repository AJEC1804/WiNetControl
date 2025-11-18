/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import static entidades.Arreglos.listaApellidos;
import static entidades.Arreglos.listaContrasena;
import static entidades.Arreglos.listaCorreo;
import static entidades.Arreglos.listaDireccion;
import static entidades.Arreglos.listaIdentificacion;
import static entidades.Arreglos.listaNombres;
import static entidades.Arreglos.listaTelefono;
import static entidades.Arreglos.listaTipoId;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.*;

/**
 *
 * @author Gyanela Meza
 */
public class crud_usuarios {

    static final int ELEMENTOS = 3;
    public static cliente[] clientes = new cliente[ELEMENTOS];

    public static int contadorClientes = 0;
    public static Administrador[] administrador = new Administrador[ELEMENTOS];
    public static int contadorAdministrador = 0;
    



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
            JOptionPane.showMessageDialog(null, "Las contraseña debe tener al menos 6 caracteres.");
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

        cliente nuevo = new cliente(nombre, apellido, tipo, identificacion, telefono, correo, direccion, contrasena, confContrasena, "Oro");

        if (contadorClientes < ELEMENTOS) {
            
            listaNombres.add(nombre);
            listaApellidos.add(apellido);
            listaTipoId.add(tipo);
            listaIdentificacion.add(identificacion);
            listaTelefono.add(telefono);
            listaCorreo.add(correo);
            listaDireccion.add(direccion);
            listaContrasena.add(contrasena);
            clientes[contadorClientes] = nuevo;
            contadorClientes++;
            JOptionPane.showMessageDialog(null, "Cliente agregado correctamente.");

            tx_nombres.setText("");
            tx_apellidos.setText("");
            cb_tipo.setSelectedIndex(0);
            tx_identificacion.setText("");
            tx_telefono.setText("");
            tx_correo.setText("");
            tx_direccion.setText("");
            tx_contrasena.setText("");
            tx_confContrasena.setText("");

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
        return contrasena.length() >= 6;
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
        boolean encontrado = false;

        for (int i = 0; i < contadorClientes; i++) {
            if (clientes[i] != null && clientes[i].getIdentificacion().equals(docBuscar)) {
                tx_nombre_eliminar.setText(clientes[i].getNombre());
                tx_apellido_eliminar.setText(clientes[i].getApellido());
                tx_tipo_eliminar.setText(clientes[i].getTipo());
                tx_documento_eliminar.setText(clientes[i].getIdentificacion());
                tx_telefono_eliminar.setText(clientes[i].getTelefono());
                tx_correo_eliminar.setText(clientes[i].getCorreo());
                tx_direccion_eliminar.setText(clientes[i].getDireccion());
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            JOptionPane.showMessageDialog(null, "Cliente no encontrado");
        }
    }

    public static void eliminarCliente(JTextField tx_documento_eliminar, JTextField tx_nombre_eliminar, JTextField tx_apellido_eliminar,
            JTextField tx_tipo_eliminar, JTextField tx_telefono_eliminar, JTextField tx_correo_eliminar, JTextField tx_direccion_eliminar,
            JTextField tx_docu_buscar_elim) {
        String docEliminar = tx_documento_eliminar.getText().trim();
        boolean eliminado = false;

        for (int i = 0; i < contadorClientes; i++) {
            if (clientes[i] != null && clientes[i].getIdentificacion().equals(docEliminar)) {
                for (int j = i; j < contadorClientes - 1; j++) {
                    clientes[j] = clientes[j + 1];
                }
                clientes[contadorClientes - 1] = null;
                contadorClientes--;
                eliminado = true;
                JOptionPane.showMessageDialog(null, "Cliente eliminado correctamente");
                break;
            }
        }

        if (!eliminado) {
            JOptionPane.showMessageDialog(null, "No se encontró el cliente a eliminar");
        }
        tx_documento_eliminar.setText("");
        tx_nombre_eliminar.setText("");
        tx_apellido_eliminar.setText("");
        tx_tipo_eliminar.setText("");
        tx_telefono_eliminar.setText("");
        tx_correo_eliminar.setText("");
        tx_direccion_eliminar.setText("");
        tx_docu_buscar_elim.setText("");
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
        String documento = tx_docu_buscar_actualizar.getText().trim();

        if (documento.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingrese el documento del cliente a buscar.");
            return;
        }

        boolean encontrado = false;

        for (int i = 0; i < contadorClientes; i++) {
            cliente c = clientes[i];

            if (c != null && c.getIdentificacion().equals(documento)) {
                encontrado = true;

                tx_nombreCambiar.setText(c.getNombre());
                tx_apellidoCambiar.setText(c.getApellido());
                tx_tipo1.setText(c.getTipo());
                tx_identificacionCambiar.setText(c.getIdentificacion());
                tx_telefonoCambiar.setText(c.getTelefono());
                tx_correoCambiar.setText(c.getCorreo());
                tx_direccionCambiar.setText(c.getDireccion());

                JOptionPane.showMessageDialog(null, "Cliente encontrado. Puede modificar los datos y presionar 'Actualizar'.");
                break;
            }
        }

        if (!encontrado) {
            JOptionPane.showMessageDialog(null, "No se encontró ningún cliente con ese documento.");
        }
    }

    public static void actualizarCliente(
            JTextField tx_docu_buscar_actualizar,
            JTextField tx_nombreCambiar1,
            JTextField tx_apellidosCambiar1,
            JComboBox<String> jtipo_cambiar,
            JTextField tx_identificacionCambiar1,
            JTextField tx_telefonoCambiar1,
            JTextField tx_correoCambiar1,
            JTextField tx_direccionCambiar1,
            JPasswordField tx_passwordCambiar1
    ) {
        String documentoBuscar = tx_docu_buscar_actualizar.getText().trim();

        if (documentoBuscar.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor, busque un cliente antes de actualizar.");
            return;
        }

        boolean encontrado = false;

        for (int i = 0; i < contadorClientes; i++) {
            cliente c = clientes[i];

            if (c != null && c.getIdentificacion().equals(documentoBuscar)) {
                encontrado = true;

                String idOriginal = c.getIdentificacion();

                String nuevoNombre = tx_nombreCambiar1.getText().trim();
                String nuevoApellido = tx_apellidosCambiar1.getText().trim();
                String nuevoTipo = (jtipo_cambiar.getSelectedItem() != null)
                        ? jtipo_cambiar.getSelectedItem().toString()
                        : "";
                String nuevoIdentificacion = tx_identificacionCambiar1.getText().trim();
                String nuevoTelefono = tx_telefonoCambiar1.getText().trim();
                String nuevoCorreo = tx_correoCambiar1.getText().trim();
                String nuevoDireccion = tx_direccionCambiar1.getText().trim();
                String nuevoPassword = new String(tx_passwordCambiar1.getPassword()).trim();

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
                    c.setTelefono(nuevoTelefono);
                }
                if (!nuevoCorreo.isEmpty()) {
                    c.setCorreo(nuevoCorreo);
                }
                if (!nuevoDireccion.isEmpty()) {
                    c.setDireccion(nuevoDireccion);
                }
                if (!nuevoPassword.isEmpty()) {
                    c.setContrasena(nuevoPassword);
                }

                c.setIdentificacion(idOriginal);

                JOptionPane.showMessageDialog(null, "Cliente actualizado correctamente.");

                tx_nombreCambiar1.setText(c.getNombre());
                tx_apellidosCambiar1.setText(c.getApellido());
                jtipo_cambiar.setSelectedItem(c.getTipo());
                tx_identificacionCambiar1.setText(c.getIdentificacion());
                tx_telefonoCambiar1.setText(c.getTelefono());
                tx_correoCambiar1.setText(c.getCorreo());
                tx_direccionCambiar1.setText(c.getDireccion());
                tx_passwordCambiar1.setText(c.getContrasena());

                break;
            }

        }

        if (!encontrado) {
            JOptionPane.showMessageDialog(null, "Cliente no encontrado.");
        }
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

            tx_nombre_buscar.setText("");
            tx_apellido_buscar.setText("");
            tx_tipo_buscar.setText("");
            tx_documento_buscar.setText("");
            tx_telefono_buscar.setText("");
            tx_correo_buscar.setText("");
            tx_direccion_buscar.setText("");
        }

        tx_docu_buscar.setText("");
    }

    public static void listarCliente(JTable Tab_Client) {
        DefaultTableModel modelo = (DefaultTableModel) Tab_Client.getModel();
        modelo.setRowCount(0); // Limpia la tabla antes de listar

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


///////////////////////////
}
