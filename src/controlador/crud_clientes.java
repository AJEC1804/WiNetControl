/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import model.cliente;



/**
 *
 * @author Gyanela Meza
 */
public class crud_clientes {
    

    static final int ELEMENTOS = 10;
    public static cliente.Cliente[] clientes = new cliente.Cliente[ELEMENTOS]; 
    public static int contadorClientes = 0;
    
    public static void prueba(){
    /*String nombre = tx_nombre.getText();
        String apellido = tx_apellido.getText();
        String telefono = tx_telefono.getText();
        String documento = tx_documento.getText();
        String email = tx_email.getText();
        
        Cliente nuevo = new Cliente(nombre, apellido, telefono, documento, email);
        boolean guardado = crud_clientes.agregarCliente(nuevo);
        if (guardado) {
        JOptionPane.showMessageDialog(null, "Cliente agregado correctamente");
        tx_nombre.setText("");
        tx_apellido.setText("");
        tx_telefono.setText("");
        tx_documento.setText("");
        tx_email.setText("");
    } else {
        JOptionPane.showMessageDialog(null, "Error: límite de clientes alcanzado");
    }
    }  */
    
    
    /*public static boolean agregarCliente(cliente.Cliente newCliente) {
        if (contadorClientes < ELEMENTOS) {
            clientes[contadorClientes] = newCliente;
              contadorClientes++;
            return true;
        } else {
            return false; 
        }
    }

    public static boolean eliminarCliente(String documento) {
    for (int i = 0; i < contadorClientes; i++) {
        if (clientes[i] != null && clientes[i].getDocumento().equals(documento)) {
            for (int j = i; j < contadorClientes - 1; j++) {
                clientes[j] = clientes[j + 1];
            }
            clientes[contadorClientes - 1] = null;
            contadorClientes--;
            return true;
        }
    }
    return false;
}
    
    public static cliente.Cliente buscarCliente(String documento) {
    for (int i = 0; i < contadorClientes; i++) {
        if (clientes[i] != null && clientes[i].getDocumento().equals(documento)) {
            return clientes[i];
        }
    }
    return null;*/  
}

}
