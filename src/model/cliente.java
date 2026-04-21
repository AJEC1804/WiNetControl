/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Gyanela Meza
 */
public class cliente extends Usuario {
    private String confContrasena;
    private String planActual;

    public cliente(String nombre, String apellido, String tipo, String identificacion,
            String telefono, String correo, String direccion, String contrasena,
            String confContrasena, String planActual) {
        super(nombre, apellido, tipo, identificacion, telefono, correo, direccion, contrasena);
        setConfContrasena(confContrasena);
        setPlanActual(planActual);
    }

    public String getConfContrasena() {
        return confContrasena;
    }

    public final void setConfContrasena(String confContrasena) {
        if (confContrasena != null && confContrasena.trim().equals(this.contrasena)) {
            this.confContrasena = confContrasena.trim();
        } else {
            this.confContrasena = this.contrasena;
        }
    }

    public String getPlanActual() {
        return planActual;
    }

    public void setPlanActual(Planes planSeleccionado) {
        if (planSeleccionado != null) {
            setPlanActual(planSeleccionado.getNombrePlan());
        } else {
            setPlanActual("Sin plan");
        }
    }

    public final void setPlanActual(String planActual) {
        if (planActual != null && !planActual.trim().isEmpty()) {
            this.planActual = planActual.trim();
        } else {
            this.planActual = "Oro";
        }
    }

    @Override
    public String getTipoUsuario() {
        return "Cliente";
    }

    @Override
    public String toString() {
        return nombre + " " + apellido + " - " + tipo + "." + identificacion;
    }
}

