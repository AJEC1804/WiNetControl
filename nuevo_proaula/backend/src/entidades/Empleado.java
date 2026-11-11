package entidades;

/**
 * Entidad que representa a un Empleado (trabajador) del sistema WiNet Control.
 * Hereda de Usuario igual que Cliente y Administrador: la clase base aporta
 * los datos personales, las credenciales y el rol; aquí se agregan los datos
 * propios de un trabajador (cargo, sucursal y fecha de ingreso).
 *
 * Rol: EMPLEADO.
 * Permisos (definidos en la matriz de la capa de vista):
 * - Ver, buscar y registrar clientes.
 * - Editar datos de contacto de un cliente.
 * - Atender tickets de soporte.
 * NO puede eliminar clientes ni cambiar el estado del servicio.
 */
public class Empleado extends Usuario {

    private String cargo;         // Técnico, Atención al cliente, Supervisor, etc.
    private String sucursal;      // Sede donde trabaja
    private String fechaIngreso;  // dd/MM/yyyy

    public Empleado() {
        super();
        this.rol = "EMPLEADO";
        this.cargo = "Atención al cliente";
        this.sucursal = "Sucursal Local Norte";
        this.fechaIngreso = "";
    }

    public Empleado(String nombre, String apellido, String tipo, String identificacion,
                    String telefono, String correo, String direccion, String contrasena,
                    String cargo, String sucursal, String fechaIngreso) {

        super(nombre, apellido, tipo, identificacion, telefono, correo, direccion,
              contrasena, "EMPLEADO", true);

        this.cargo = (cargo != null && !cargo.trim().isEmpty()) ? cargo.trim() : "Atención al cliente";
        this.sucursal = (sucursal != null && !sucursal.trim().isEmpty()) ? sucursal.trim() : "Sucursal Local Norte";
        this.fechaIngreso = (fechaIngreso != null) ? fechaIngreso.trim() : "";
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
