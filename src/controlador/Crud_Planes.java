/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;


import javax.swing.*;
import javax.swing.table.*;
import model.*;

/**
 *
 * @author Gyanela Meza
 */
public class Crud_Planes {
    static final int ELEMENTOS = 3;
    public static Planes[] plan = new Planes[ELEMENTOS];
    public static int contadorPlanes = 0;
    
    
    public static void agregarPlan(JTextField tx_cod_plan, JTextField tx_nombre_plan, JTextField tx_velocidad_plan,
        JTextField tx_precio_plan,JTextPane tx_descripcion_plan) {
        String codigoStr = tx_cod_plan.getText().trim();
        String nombre = tx_nombre_plan.getText().trim();
        String velocidad = tx_velocidad_plan.getText().trim();
        String precioStr = tx_precio_plan.getText().trim();
        String descripcion = tx_descripcion_plan.getText().trim();

        if (codigoStr.isEmpty() || nombre.isEmpty() || velocidad.isEmpty() || precioStr.isEmpty() || descripcion.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Es obligatorio llenar todos los campos.");
            return;
        }

        try {
            int codigo = Integer.parseInt(codigoStr);
            int precio = Integer.parseInt(precioStr);

            for (int i = 0; i < contadorPlanes; i++) {
                if (plan[i] != null && plan[i].getCodigo() == codigo) {
                    JOptionPane.showMessageDialog(null, "Ya existe un plan con este código.");
                    return;
                }
            }

            Planes nuevo = new Planes(codigo, nombre, velocidad, precio, descripcion);
            plan[contadorPlanes] = nuevo;
            contadorPlanes++;

            JOptionPane.showMessageDialog(null, "Plan agregado correctamente.");

            tx_cod_plan.setText("");
            tx_nombre_plan.setText("");
            tx_velocidad_plan.setText("");
            tx_precio_plan.setText("");
            tx_descripcion_plan.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, " El código y el precio deben ser numéricos.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al agregar el plan: " + e.getMessage());
        }
    }
    public static void listarPlanes(JTable Tab_Plan) {
    DefaultTableModel modelo = (DefaultTableModel) Tab_Plan.getModel();
    modelo.setRowCount(0); 

    for (int i = 0; i < contadorPlanes; i++) {
        Planes p = plan[i];
        if (p != null) {
            Object[] fila = {
                p.getCodigo(),
                p.getNombre(),
                p.getVelocidad(),
                p.getPrecio(),
                p.getDescripcion()
            };
            modelo.addRow(fila);
        }
    }
}
    public static void buscarPlan(JTextField tx_idPlan_buscar, JTextField tx_codigo_buscado,JTextField tx_nombrePlan_buscado, 
        JTextField tx_vel_buscar, JTextField tx_precio_buscar, JTextPane tx_descripcion_plan1) {

    String codigoStr = tx_idPlan_buscar.getText().trim();

    if (codigoStr.isEmpty()) {
        JOptionPane.showMessageDialog(null, "Por favor, ingrese el código del plan que desea buscar.");
        return;
    }

    boolean encontrado = false;

    try {
        int codigo = Integer.parseInt(codigoStr);

        for (int i = 0; i < contadorPlanes; i++) {
            Planes p = plan[i];
            if (p != null && p.getCodigo() == codigo) {

                tx_codigo_buscado.setText(String.valueOf(p.getCodigo()));
                tx_nombrePlan_buscado.setText(p.getNombre());
                tx_vel_buscar.setText(p.getVelocidad());
                tx_precio_buscar.setText(String.valueOf(p.getPrecio()));
                tx_descripcion_plan1.setText(p.getDescripcion());

                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            JOptionPane.showMessageDialog(null, "No se encontró ningún plan con ese código.");

            tx_codigo_buscado.setText("");
            tx_nombrePlan_buscado.setText("");
            tx_vel_buscar.setText("");
            tx_precio_buscar.setText("");
            tx_descripcion_plan1.setText("");
        }

        tx_idPlan_buscar.setText("");

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "El código debe ser un número válido.");
    }
}
   public static void buscarPlanActualizar(JTextField tx_cod_buscar_actualizar_plan,JTextField tx_cod_plan_mostrar, JTextField tx_nombre_plan_mostrar,
    JTextField tx_velocidad_plan_mostrar,JTextField tx_precio_plan_mostrar,JTextPane tx_descripcion_plan2) {

    String codigoStr = tx_cod_buscar_actualizar_plan.getText().trim();

    if (codigoStr.isEmpty()) {
        JOptionPane.showMessageDialog(null, "Por favor ingrese el código del plan que desea buscar.");
        return;
    }

    try {
        int codigo = Integer.parseInt(codigoStr);
        boolean encontrado = false;

        for (int i = 0; i < contadorPlanes; i++) {
            Planes p = plan[i];
            if (p != null && p.getCodigo() == codigo) {

               
                tx_cod_plan_mostrar.setText(String.valueOf(p.getCodigo()));
                tx_nombre_plan_mostrar.setText(p.getNombre());
                tx_velocidad_plan_mostrar.setText(p.getVelocidad());
                tx_precio_plan_mostrar.setText(String.valueOf(p.getPrecio()));
                tx_descripcion_plan2.setText(p.getDescripcion());

                JOptionPane.showMessageDialog(null, "Deje en blanco los campos que no desea actualizar.");
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            JOptionPane.showMessageDialog(null, "No se encontró ningún plan con ese código.");
            tx_cod_plan_mostrar.setText("");
            tx_nombre_plan_mostrar.setText("");
            tx_velocidad_plan_mostrar.setText("");
            tx_precio_plan_mostrar.setText("");
            tx_descripcion_plan2.setText("");
        }

        tx_cod_buscar_actualizar_plan.setText("");

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, " El código debe ser un número válido.");
    }
}
   public static void actualizarPlan(
    JTextField tx_cod_plan_mostrar,
    JTextField tx_nombre_plan_mostrar,
    JTextField tx_velocidad_plan_mostrar,
    JTextField tx_precio_plan_mostrar,
    JTextPane tx_descripcion_plan2,
    JTextField tx_cod_plan_act,
    JTextField tx_nombre_plan_act,
    JTextField tx_velocidad_plan_act,
    JTextField tx_precio_plan_act,
    JTextPane tx_descripcion_plan_act
) {
    String codigoBuscarStr = tx_cod_plan_mostrar.getText().trim();

    if (codigoBuscarStr.isEmpty()) {
        JOptionPane.showMessageDialog(null, "Por favor, busque un plan antes de actualizar.");
        return;
    }

    try {
        int codigoBuscar = Integer.parseInt(codigoBuscarStr);
        boolean encontrado = false;

        for (int i = 0; i < contadorPlanes; i++) {
            Planes p = plan[i];

            if (p != null && p.getCodigo() == codigoBuscar) {
                encontrado = true;

                
                String nuevoCodigoStr = tx_cod_plan_act.getText().trim();
                String nuevoNombre = tx_nombre_plan_act.getText().trim();
                String nuevaVelocidad = tx_velocidad_plan_act.getText().trim();
                String nuevoPrecioStr = tx_precio_plan_act.getText().trim();
                String nuevaDescripcion = tx_descripcion_plan_act.getText().trim();

                
                if (!nuevoCodigoStr.isEmpty()) {
                    try {
                        p.setCodigo(Integer.parseInt(nuevoCodigoStr));
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "El código debe ser un número válido.");
                        return;
                    }
                }

                if (!nuevoNombre.isEmpty()) p.setNombre(nuevoNombre);
                if (!nuevaVelocidad.isEmpty()) p.setVelocidad(nuevaVelocidad);
                if (!nuevoPrecioStr.isEmpty()) {
                    try {
                        p.setPrecio(Integer.parseInt(nuevoPrecioStr));
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "El precio debe ser un número entero válido.");
                        return;
                    }
                }
                if (!nuevaDescripcion.isEmpty()) p.setDescripcion(nuevaDescripcion);

                
                JOptionPane.showMessageDialog(null, "Plan actualizado correctamente.");

                
                tx_cod_plan_mostrar.setText(String.valueOf(p.getCodigo()));
                tx_nombre_plan_mostrar.setText(p.getNombre());
                tx_velocidad_plan_mostrar.setText(p.getVelocidad());
                tx_precio_plan_mostrar.setText(String.valueOf(p.getPrecio()));
                tx_descripcion_plan2.setText(p.getDescripcion());

                
                tx_cod_plan_act.setText("");
                tx_nombre_plan_act.setText("");
                tx_velocidad_plan_act.setText("");
                tx_precio_plan_act.setText("");
                tx_descripcion_plan_act.setText("");

                break;
            }
        }

        if (!encontrado) {
            JOptionPane.showMessageDialog(null, "Plan no encontrado.");
        }

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "El código debe ser un número válido.");
    }
}
   public static void buscarPlanEliminar(
    JTextField tx_idPlan_buscar1,
    JTextField tx_cod_plan_mostrar1,
    JTextField tx_velocidad_plan_mostrar1,
    JTextField tx_precio_plan_mostrar1,
    JTextPane tx_descripcion_plan3
) {
    String codigoBuscar = tx_idPlan_buscar1.getText().trim();
    boolean encontrado = false;

    for (int i = 0; i < contadorPlanes; i++) {
        if (plan[i] != null && String.valueOf(plan[i].getCodigo()).equals(codigoBuscar)) {
            tx_cod_plan_mostrar1.setText(String.valueOf(plan[i].getCodigo()));
            tx_velocidad_plan_mostrar1.setText(plan[i].getVelocidad());
            tx_precio_plan_mostrar1.setText(String.valueOf(plan[i].getPrecio()));
            tx_descripcion_plan3.setText(plan[i].getDescripcion());
            encontrado = true;
            break;
        }
    }

    if (!encontrado) {
        JOptionPane.showMessageDialog(null, "Plan no encontrado");
    }
}
public static void eliminarPlan(
    JTextField tx_cod_plan_mostrar1,
    JTextField tx_velocidad_plan_mostrar1,
    JTextField tx_precio_plan_mostrar1,
    JTextPane tx_descripcion_plan3,
    JTextField tx_idPlan_buscar1
) {
    String codigoEliminar = tx_cod_plan_mostrar1.getText().trim();
    boolean eliminado = false;

    for (int i = 0; i < contadorPlanes; i++) {
        if (plan[i] != null && String.valueOf(plan[i].getCodigo()).equals(codigoEliminar)) {
            
            for (int j = i; j < contadorPlanes - 1; j++) {
                plan[j] = plan[j + 1];
            }
            plan[contadorPlanes - 1] = null;
            contadorPlanes--;
            eliminado = true;
            JOptionPane.showMessageDialog(null, "Plan eliminado correctamente");
            break;
        }
    }

    if (!eliminado) {
        JOptionPane.showMessageDialog(null, "No se encontró el plan a eliminar");
    }

    
    tx_cod_plan_mostrar1.setText("");
    tx_velocidad_plan_mostrar1.setText("");
    tx_precio_plan_mostrar1.setText("");
    tx_descripcion_plan3.setText("");
    tx_idPlan_buscar1.setText("");
}

    ///////////////

}


