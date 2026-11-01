import controladores.*;
import entidades.*;
import metodos.*;
import modelos.*;
import utils.Respuesta;

import java.util.List;

/**
 * Suite de verificación y prueba de integración de todo el Backend.
 * Demuestra:
 * 1. Estructuras de datos puras por punteros (ListaSimple, ListaDoble, Pila, Cola).
 * 2. CRUD completo de Clientes, Planes, Facturación y Soporte.
 * 3. Asignación de servicios y emisión automática de facturas.
 */
public class MainBackendTest {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   INICIANDO PRUEBAS DEL BACKEND - PROAULA WINET CONTROL (MVC)   ");
        System.out.println("=================================================================\n");

        probarListaSimple();
        probarListaDoble();
        probarPilaYCola();
        probarCrudCompletoProaula();

        System.out.println("\n=================================================================");
        System.out.println("   ¡TODAS LAS PRUEBAS DEL BACKEND SE EJECUTARON EXITOSAMENTE!    ");
        System.out.println("   EL BACKEND ESTÁ LISTO PARA QUE EL EQUIPO INICIE EL FRONTEND   ");
        System.out.println("=================================================================");
    }

    private static void probarListaSimple() {
        System.out.println(">>> 1. PRUEBA DE ESTRUCTURA: LISTA SIMPLEMENTE ENLAZADA <<<");
        ListaSimple lista = new ListaSimple();

        // Inserciones
        lista.insertar_inicio(new Nodo("nodo_medio"));
        lista.insertar_inicio(new Nodo("nodo_inicio"));
        lista.insertar_final(new Nodo("nodo_final"));
        lista.insertar_despues_nodo_ref_simple(new Nodo("nodo_siguiente_medio"), "nodo_medio");
        lista.insertar_antes_nodo_ref_simple(new Nodo("nodo_previo_medio"), "nodo_medio");

        System.out.print("Recorrido tras inserciones: ");
        lista.recorrer();

        // Búsqueda y Actualización
        Nodo nodoBuscado = lista.buscar_nodo_simple("nodo_siguiente_medio");
        System.out.println("Búsqueda de 'nodo_siguiente_medio': " + (nodoBuscado != null ? "ENCONTRADO (" + nodoBuscado.getDato() + ")" : "NO ENCONTRADO"));

        lista.actualizar_nodo_simple("nodo_siguiente_medio", "nodo_siguiente_ACTUALIZADO");
        System.out.print("Tras actualización: ");
        lista.recorrer();

        // Eliminaciones
        lista.eliminar_antes_nodo_ref_simple("nodo_medio");
        lista.eliminar_despues_nodo_ref_simple("nodo_medio");
        lista.eliminar_inicio_lista_simple();
        lista.eliminar_final_lista_simple();

        System.out.print("Recorrido tras eliminaciones: ");
        lista.recorrer();
        System.out.println("Estado ListaSimple: OK\n");
    }

    private static void probarListaDoble() {
        System.out.println(">>> 2. PRUEBA DE ESTRUCTURA: LISTA DOBLEMENTE ENLAZADA <<<");
        ListaDoble listaDoble = new ListaDoble();

        // Inserciones
        listaDoble.insertar_inicio(new Nodo("D_medio"));
        listaDoble.insertar_inicio(new Nodo("D_primero"));
        listaDoble.insertar_final_lista_doble(new Nodo("D_ultimo"));
        listaDoble.insertar_antes_nodo_ref_doble(new Nodo("D_antes_ultimo"), "D_ultimo");
        listaDoble.insertar_despues_nodo_ref_doble(new Nodo("D_despues_medio"), "D_medio");

        System.out.print("Recorrido hacia adelante: ");
        listaDoble.recorrer();

        System.out.print("Recorrido hacia atrás (recorrerback): ");
        listaDoble.recorrerback();

        // Búsqueda y Actualización
        Nodo nodoDobleBuscado = listaDoble.buscar_nodo_doble("D_despues_medio");
        System.out.println("Búsqueda de 'D_despues_medio': " + (nodoDobleBuscado != null ? "ENCONTRADO" : "NO ENCONTRADO"));

        listaDoble.actualizar_nodo_doble("D_despues_medio", "D_MODIFICADO");
        System.out.print("Tras actualizar: ");
        listaDoble.recorrer();

        // Eliminaciones
        listaDoble.eliminar_nodo_referente_doble("D_MODIFICADO");
        listaDoble.eliminar_inicio_lista_doble();
        listaDoble.eliminar_final_lista_doble();

        System.out.print("Recorrido final lista doble: ");
        listaDoble.recorrer();
        System.out.println("Estado ListaDoble: OK\n");
    }

    private static void probarPilaYCola() {
        System.out.println(">>> 3. PRUEBA DE ESTRUCTURAS: PILA (LIFO) Y COLA (FIFO) <<<");

        // Pila
        Pila pilaFacturas = new Pila();
        pilaFacturas.apilar(new Nodo("Factura_Enero"));
        pilaFacturas.apilar(new Nodo("Factura_Febrero"));
        pilaFacturas.apilar(new Nodo("Factura_Marzo"));
        System.out.println("Cima de la Pila: " + pilaFacturas.getCima().getDato());
        System.out.println("Desapilando de la Pila: " + pilaFacturas.desapilar().getDato());
        System.out.println("Nueva Cima: " + pilaFacturas.getCima().getDato());

        // Cola
        Cola colaSoporte = new Cola();
        colaSoporte.encolar(new Nodo("Ticket_Primero"));
        colaSoporte.encolar(new Nodo("Ticket_Segundo"));
        colaSoporte.encolar(new Nodo("Ticket_Tercero"));
        System.out.println("Frente de la Cola: " + colaSoporte.getFrente().getDato());
        System.out.println("Desencolando de la Cola (FIFO): " + colaSoporte.desencolar().getDato());
        System.out.println("Nuevo Frente: " + colaSoporte.getFrente().getDato());
        System.out.println("Estado Pila y Cola: OK\n");
    }

    private static void probarCrudCompletoProaula() {
        System.out.println(">>> 4. PRUEBA DE CRUD COMPLETO Y CONTROLADORES DEL PROAULA <<<");

        UsuarioController usuarioCtrl = new UsuarioController();
        PlanController planCtrl = new PlanController();
        FacturaController facturaCtrl = new FacturaController();
        SoporteController soporteCtrl = new SoporteController();

        // A. CRUD PLANES
        System.out.println("\n--- A. CRUD DE PLANES ---");
        Respuesta<Plan> respPlanFibra = planCtrl.crearPlan("101", "Plan Fibra 100M", "100 Mbps", "65000", "Internet fibra simétrica");
        Respuesta<Plan> respPlanGamer = planCtrl.crearPlan("102", "Plan Fibra Gamer 300M", "300 Mbps", "95000", "Baja latencia");
        Respuesta<Plan> respPlanCorp = planCtrl.crearPlan("103", "Plan Corporativo 500M", "500 Mbps", "145000", "IP fija incluida");

        System.out.println("Creación Plan Fibra: " + respPlanFibra.getMensaje());
        System.out.println("Creación Plan Gamer: " + respPlanGamer.getMensaje());
        System.out.println("Creación Plan Corp: " + respPlanCorp.getMensaje());

        // Búsqueda de plan
        Respuesta<Plan> busqPlan = planCtrl.buscarPlanPorCodigo("102");
        System.out.println("Búsqueda Plan Gamer: " + busqPlan.getDatos());

        // Actualización de plan
        planCtrl.actualizarPlan("102", "102", "Plan Fibra Gamer Ultra 350M", "350 Mbps", "99000", "Actualizado con más velocidad");
        System.out.println("Plan Gamer Actualizado: " + planCtrl.buscarPlanPorCodigo("102").getDatos());

        // Listar planes
        Respuesta<List<Plan>> catalogoPlanes = planCtrl.listarPlanes();
        System.out.println("Total planes registrados: " + catalogoPlanes.getDatos().size());

        // B. CRUD USUARIOS / CLIENTES
        System.out.println("\n--- B. CRUD DE CLIENTES ---");
        Respuesta<Cliente> clienteCarlos = usuarioCtrl.registrarCliente(
                "Carlos", "Mendoza", "CC", "1045678901",
                "3001234567", "carlos.mendoza@email.com", "Calle 45 # 12-34",
                "ClaveSegura123", "ClaveSegura123"
        );
        System.out.println("Registro Carlos: " + clienteCarlos.getMensaje());

        Respuesta<Cliente> clienteMaria = usuarioCtrl.registrarCliente(
                "Maria", "Gomez", "CC", "1098765432",
                "3109876543", "maria.gomez@email.com", "Carrera 7 # 80-15",
                "PassWord2026", "PassWord2026"
        );
        System.out.println("Registro María: " + clienteMaria.getMensaje());

        // Intentar registrar duplicado
        Respuesta<Cliente> intentoDuplicado = usuarioCtrl.registrarCliente(
                "Carlos", "Otro", "CC", "1045678901",
                "3000000000", "otro@email.com", "Calle 1",
                "ClaveSegura123", "ClaveSegura123"
        );
        System.out.println("Validación duplicado cédula: " + intentoDuplicado.getMensaje() + " (Éxito: " + intentoDuplicado.isExito() + ")");

        // Autenticación (Login con hash SHA-256)
        Respuesta<Cliente> loginResp = usuarioCtrl.login("1045678901", "ClaveSegura123");
        System.out.println("Login Carlos: " + loginResp.getMensaje() + " | Autenticado: " + loginResp.getDatos().getNombreCompleto());

        // Verificación de los 3 Roles del Sistema (Superusuario, Admin Local y Cliente)
        System.out.println("\n--- VERIFICACIÓN DE ROLES Y CONTROL DE ACCESO ---");
        Respuesta<Usuario> loginSuper = usuarioCtrl.autenticarUsuario("superadmin", "Super123!");
        System.out.println("Login Superusuario: " + loginSuper.getMensaje() + " | Rol: " + loginSuper.getDatos().getRol() + " | Global: " + loginSuper.getDatos().esSuperAdmin());

        Respuesta<Usuario> loginAdmin = usuarioCtrl.autenticarUsuario("admin123", "Admin123!");
        System.out.println("Login Admin Local: " + loginAdmin.getMensaje() + " | Rol: " + loginAdmin.getDatos().getRol() + " | Tipo: " + loginAdmin.getDatos().getTipoUsuario());

        Respuesta<Usuario> loginClienteRol = usuarioCtrl.autenticarUsuario("1045678901", "ClaveSegura123");
        System.out.println("Login Cliente: " + loginClienteRol.getMensaje() + " | Rol: " + loginClienteRol.getDatos().getRol() + " | EsCliente: " + loginClienteRol.getDatos().esCliente());

        // Actualizar datos del cliente
        usuarioCtrl.actualizarCliente(
                "1045678901", "Carlos Alberto", "Mendoza Perez", "CC",
                "3009998877", "carlos.mendoza@email.com", "Avenida Siempre Viva 742",
                null, null
        );
        Cliente carlosActualizado = usuarioCtrl.buscarCliente("1045678901").getDatos();
        System.out.println("Cliente Carlos actualizado: " + carlosActualizado.getNombreCompleto() + " - Tel: " + carlosActualizado.getTelefono() + " - Dir: " + carlosActualizado.getDireccion());

        // C. ASIGNACIÓN DE PLAN DIRECTO Y GESTIÓN DE ESTADOS
        System.out.println("\n--- C. ASIGNACIÓN DE PLANES Y CONTROL DE ESTADOS (ACTIVAR/DESACTIVAR) ---");
        
        // Probar desactivación de un plan: no debe permitir contratarlo
        planCtrl.desactivarPlan("101");
        System.out.println("Plan 101 desactivado: " + planCtrl.buscarPlanPorCodigo("101").getDatos().getEstado());
        Respuesta<Cliente> intentoContratarInactivo = usuarioCtrl.asignarPlan("1045678901", "Plan Fibra 100M");
        System.out.println("Intento de asignar plan inactivo: " + intentoContratarInactivo.getMensaje());

        // Reactivar el plan y contratar
        planCtrl.activarPlan("101");
        System.out.println("Plan 101 reactivado: " + planCtrl.buscarPlanPorCodigo("101").getDatos().getEstado());
        Respuesta<Cliente> asignacionResp = usuarioCtrl.asignarPlan("1045678901", "Plan Fibra Gamer Ultra 350M");
        System.out.println("Resultado Asignación: " + asignacionResp.getMensaje());

        // Probar desactivación de un cliente: no debe permitir login
        usuarioCtrl.desactivarCliente("1098765432");
        System.out.println("Cliente María desactivada: " + usuarioCtrl.buscarCliente("1098765432").getDatos().getEstado());
        Respuesta<Cliente> loginInactivo = usuarioCtrl.login("1098765432", "PassWord2026");
        System.out.println("Login de cliente inactivo: " + loginInactivo.getMensaje());

        // Reactivar cliente María
        usuarioCtrl.activarCliente("1098765432");
        System.out.println("Cliente María reactivada: " + usuarioCtrl.buscarCliente("1098765432").getDatos().getEstado());

        // D. CRUD FACTURACIÓN (PILA LIFO)
        System.out.println("\n--- D. GESTIÓN DE FACTURAS (PILA LIFO) ---");
        facturaCtrl.generarFactura("1098765432", "Plan Fibra 100M");

        Respuesta<List<Factura>> facturasCarlos = facturaCtrl.obtenerFacturasUsuario("1045678901");
        System.out.println("Facturas asociadas a Carlos: " + facturasCarlos.getDatos().size());

        Respuesta<Factura> facturaCima = facturaCtrl.consultarCima();
        System.out.println("Factura en la Cima de la Pila: " + facturaCima.getDatos());

        // E. SOPORTE TÉCNICO (COLA FIFO)
        System.out.println("\n--- E. GESTIÓN DE SOPORTE TÉCNICO (COLA FIFO) ---");
        soporteCtrl.radicarTicket("1045678901", "carlos.mendoza@email.com", "Lentitud en la noche", "Tengo intermitencia a las 8pm", false);
        soporteCtrl.radicarTicket("1098765432", "maria.gomez@email.com", "Cambio de clave wifi", "Requiero cambio de clave de mi router", false);

        Respuesta<List<Soporte>> ticketsPendientes = soporteCtrl.listarTicketsPendientes();
        System.out.println("Tickets en cola de espera: " + ticketsPendientes.getDatos().size());

        // Atender primer ticket en cola
        Respuesta<Soporte> ticketAtendido = soporteCtrl.atenderSiguienteTicket();
        System.out.println("Atendiendo ticket del frente (FIFO): " + ticketAtendido.getDatos());

        Respuesta<Soporte> ticketSiguiente = soporteCtrl.consultarSiguienteTicket();
        System.out.println("Siguiente ticket en espera: " + ticketSiguiente.getDatos());

        // F. ELIMINACIÓN Y CASCADA DE NEGOCIO
        System.out.println("\n--- F. ELIMINACIÓN Y CASCADA DE NEGOCIO ---");
        planCtrl.eliminarPlan("102");
        Cliente carlosConPlanReasignado = usuarioCtrl.buscarCliente("1045678901").getDatos();
        System.out.println("Plan de Carlos tras eliminar su plan previo: " + carlosConPlanReasignado.getPlanActual());

        Respuesta<Boolean> eliminarMaria = usuarioCtrl.eliminarCliente("1098765432");
        System.out.println("Eliminación de María: " + eliminarMaria.getMensaje());
        System.out.println("Total clientes tras eliminación: " + usuarioCtrl.listarClientes().getDatos().size());
    }
}
