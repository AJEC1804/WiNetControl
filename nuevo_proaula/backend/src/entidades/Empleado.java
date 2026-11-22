package entidades;

import java.io.Serializable;

public class Empleado extends Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private String cargo;
    private String sucursal;
    private String fechaIngreso;
    private String empresaId;

    public Empleado() {
        super();
        this.rol = "EMPLEADO";
        this.cargo = "Atención al cliente";
        this.sucursal = "Sucursal Local Norte";
        this.fechaIngreso = "";
        this.empresaId = "EMP-001";
    }

    public Empleado(String nombre, String apellido, String tipo, String identificacion,
                    String telefono, String correo, String direccion, String contrasena,
                    String cargo, String sucursal, String fechaIngreso) {
        this(nombre, apellido, tipo, identificacion, telefono, correo, direccion, contrasena,
             cargo, sucursal, fechaIngreso, "EMP-001");
    }

    public Empleado(String nombre, String apellido, String tipo, String identificacion,
                    String telefono, String correo, String direccion, String contrasena,
                    String cargo, String sucursal, String fechaIngreso, String empresaId) {

        super(nombre, apellido, tipo, identificacion, telefono, correo, direccion,
              contrasena, "EMPLEADO", true);

        this.cargo = (cargo != null && !cargo.trim().isEmpty()) ? cargo.trim() : "Atención al cliente";
        this.sucursal = (sucursal != null && !sucursal.trim().isEmpty()) ? sucursal.trim() : "Sucursal Local Norte";
        this.fechaIngreso = (fechaIngreso != null) ? fechaIngreso.trim() : "";
        this.empresaId = (empresaId != null && !empresaId.trim().isEmpty()) ? empresaId.trim() : "EMP-001";
    }

    public String getEmpresaId() {
        return (empresaId != null && !empresaId.trim().isEmpty()) ? empresaId : "EMP-001";
    }

    public void setEmpresaId(String empresaId) {
        this.empresaId = (empresaId != null && !empresaId.trim().isEmpty()) ? empresaId.trim() : "EMP-001";
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = (cargo != null && !cargo.trim().isEmpty()) ? cargo.trim() : "Atención al cliente";
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = (sucursal != null && !sucursal.trim().isEmpty()) ? sucursal.trim() : "Sucursal Local Norte";
    }

    public String getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(String fechaIngreso) {
        this.fechaIngreso = (fechaIngreso != null) ? fechaIngreso.trim() : "";
    }

    @Override
    public String getTipoUsuario() {
        return "Empleado";
    }

    @Override
    public String toString() {
        return super.toString() + " | Cargo: " + cargo + " | Sucursal: " + sucursal;
    }
}
