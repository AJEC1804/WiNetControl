package entidades;

import model.Administrador;
import model.Planes;
import model.cliente;

/**
 * Arreglos holds static arrays for administrators, clients, and plans.
 * Added default entries for a demo admin, a demo client, and three subscription plans.
 */
public class Arreglos {
    public static final int ELEMENTOS = 100;
    public static cliente[] clientes = new cliente[ELEMENTOS];
    public static int contadorClientes = 0;
    public static Administrador[] administrador = new Administrador[ELEMENTOS];
    public static int contadorAdministrador = 0;
    public static Planes[] plan = new Planes[ELEMENTOS];
    public static int contadorPlanes = 0;

    // Static initializer to populate the arrays with demo data
    static {
        // Default administrator
        Administrador admin = new Administrador("Admin", "Sistema", "CC", "admin", "0999999999", "admin@winet.com", "Oficina Central", "admin");
        administrador[contadorAdministrador++] = admin;

        // Default client (usuario)
        cliente clienteDemo = new cliente("Demo", "User", "CC", "user", "0999999999", "user@winet.com", "Direccion Demo", "user", "user", "Básico");
        clientes[contadorClientes++] = clienteDemo;

        // Subscription plans
        Planes planBasico = new Planes(1, "Básico", "10 Mbps", 10.0, "Plan Básico");
        plan[contadorPlanes++] = planBasico;

        Planes planPremium = new Planes(2, "Premium", "50 Mbps", 25.0, "Plan Premium");
        plan[contadorPlanes++] = planPremium;

        Planes planEnterprise = new Planes(3, "Enterprise", "100 Mbps", 50.0, "Plan Enterprise");
        plan[contadorPlanes++] = planEnterprise;
    }

    // Utility method to find a client by username
    public static cliente buscarCliente(String usuario) {
        for (int i = 0; i < contadorClientes; i++) {
            if (clientes[i] != null && (clientes[i].getCorreo().equals(usuario) || clientes[i].getIdentificacion().equals(usuario))) {
                return clientes[i];
            }
        }
        return null;
    }
}
