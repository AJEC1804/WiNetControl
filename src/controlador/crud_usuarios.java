/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;


import javax.swing.*;
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

        
        cliente nuevo = new cliente(nombre, apellido, tipo, identificacion, telefono, correo, direccion, contrasena, confContrasena);

        if (contadorClientes < ELEMENTOS) {
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
    
    public static void listarCliente() {
        
    }
    
    
    public static void actualizarCliente() {
        
    }
    
    public static void buscarCliente(JTextField tx_docu_buscar, JTextField tx_nombre_buscar, JTextField tx_apellido_buscar, JTextField tx_tipo_buscar, JTextField tx_documento_buscar,JTextField tx_telefono_buscar,JTextField tx_correo_buscar, JTextField tx_direccion_buscar) {

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
    
    
    
    
   
    
    ///////////////////////////
}




        
    
   