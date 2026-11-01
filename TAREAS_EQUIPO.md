# 📋 Plan de Trabajo y Asignación de Tareas del Frontend

**Proyecto:** WiNetControl (ProAula - Estructuras de Datos / MVC)  
**Repositorio GitHub:** [AJEC1804/WiNetControl](https://github.com/AJEC1804/WiNetControl)

---

## 👥 Equipo de Desarrollo y Asignación de Issues en GitHub

| Responsable | Correo | Usuario GitHub | Issue Asignado | Módulo Frontend |
| :--- | :--- | :--- | :---: | :--- |
| **Alejandro Escudero** | `alejandrocastellar04@gmail.com` | [@AJEC1804](https://github.com/AJEC1804) | [#7](https://github.com/AJEC1804/WiNetControl/issues/7) | **Autenticación, Sesión y Menú Principal (Dashboard)** |
| **Gyanela Meza** | `gianelameza@gmail.com` | [@Gyanelameza](https://github.com/Gyanelameza) | [#8](https://github.com/AJEC1804/WiNetControl/issues/8) | **Gestión y CRUD de Clientes con Control de Estados** |
| **Yeini Álvarez** | `yeinypao17@gmail.com` | [@Yeisitaaaa](https://github.com/Yeisitaaaa) | [#9](https://github.com/AJEC1804/WiNetControl/issues/9) | **Catálogo de Planes y Contratación de Servicios** |
| **Juan Esteban Navia** | `juannavia2008@gmail.com` | [@JuanesNavia08](https://github.com/JuanesNavia08) | [#10](https://github.com/AJEC1804/WiNetControl/issues/10) | **Facturación (Pila LIFO) y Soporte Técnico (Cola FIFO)** |

---

## 📌 Detalle de Tareas por Integrante

### 1. Alejandro Escudero ([#7](https://github.com/AJEC1804/WiNetControl/issues/7))
*   **Vistas:** `LoginView`, `RegistroView`, `DashboardView`.
*   **Controladores:** `UsuarioController`.
*   **Tareas:**
    - [ ] Maquetar la ventana de Login conectando con `UsuarioController.login(...)`.
    - [ ] Almacenar el objeto `Cliente` autenticado en memoria para compartir la sesión.
    - [ ] Mostrar alertas de error si la contraseña es incorrecta o el cliente está `INACTIVO`.
    - [ ] Formulario de autorregistro con `UsuarioController.registrarCliente(...)`.
    - [ ] Ventana principal con barra de navegación para dar acceso a los módulos de los compañeros.

---

### 2. Gyanela Meza ([#8](https://github.com/AJEC1804/WiNetControl/issues/8))
*   **Vistas:** `ClientesView` (con `JTable` y formulario de edición).
*   **Controladores:** `UsuarioController`.
*   **Tareas:**
    - [ ] Tabla de clientes listando los datos y su estado (`ACTIVO` / `INACTIVO`) mediante `UsuarioController.listarClientes()`.
    - [ ] Búsqueda de clientes por cédula con `UsuarioController.buscarCliente(...)`.
    - [ ] Edición de datos personales con `UsuarioController.actualizarCliente(...)`.
    - [ ] Botón de **Activar / Desactivar** con `UsuarioController.cambiarEstadoCliente(...)`.
    - [ ] Botón de eliminación con diálogo de confirmación `UsuarioController.eliminarCliente(...)`.

---

### 3. Yeini Álvarez ([#9](https://github.com/AJEC1804/WiNetControl/issues/9))
*   **Vistas:** `PlanesView` (Catálogo / CRUD), `ContratarPlanView`.
*   **Controladores:** `PlanController`, `UsuarioController`.
*   **Tareas:**
    - [ ] Tabla del catálogo de planes mostrando código, nombre, velocidad, precio, descripción y estado.
    - [ ] Formulario para crear y actualizar planes con `PlanController.crearPlan(...)` y `actualizarPlan(...)`.
    - [ ] Botón para **Activar / Desactivar** planes con `PlanController.cambiarEstadoPlan(...)`.
    - [ ] Eliminación de planes con reasignación automática usando `PlanController.eliminarPlan(...)`.
    - [ ] Vista de contratación: cargar planes activos en un `JComboBox` con `PlanController.listarPlanesActivos()` y contratar con `UsuarioController.asignarPlan(...)`.

---

### 4. Juan Esteban Navia ([#10](https://github.com/AJEC1804/WiNetControl/issues/10))
*   **Vistas:** `FacturasView`, `SoporteView`.
*   **Controladores:** `FacturaController`, `SoporteController`.
*   **Tareas:**
    - [ ] **Facturación (Pila LIFO):**
        - [ ] Tabla con historial de facturas del cliente usando `FacturaController.obtenerFacturasUsuario(...)`.
        - [ ] Visualización de la última factura en la cima de la pila (`FacturaController.consultarCima()`).
        - [ ] Botón "Abrir PDF" para abrir el archivo con `java.awt.Desktop.getDesktop().open(new File(ruta))`.
    - [ ] **Soporte Técnico (Cola FIFO):**
        - [ ] Formulario de radicación de tickets conectando con `SoporteController.radicarTicket(...)`.
        - [ ] Panel administrativo con la lista de tickets en espera (`SoporteController.listarTicketsPendientes()`).
        - [ ] Botón **"Atender Siguiente Ticket"** que desencola en orden FIFO con `SoporteController.atenderSiguienteTicket()`.

---

## 🌿 Flujo de Ramas en Git
Cada integrante debe crear y trabajar en su propia rama:
*   Alejandro: `git checkout -b feature/login-dashboard`
*   Gyanela: `git checkout -b feature/gestion-clientes`
*   Yeini: `git checkout -b feature/gestion-planes`
*   Juan: `git checkout -b feature/facturacion-soporte`
