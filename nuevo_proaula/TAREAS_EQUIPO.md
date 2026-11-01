# 📋 Plan de Trabajo y Asignación de Tareas del Frontend y Entregables

**Proyecto:** WiNetControl (ProAula - Estructuras de Datos / MVC)  
**Repositorio GitHub:** [AJEC1804/WiNetControl](https://github.com/AJEC1804/WiNetControl)  
**Despliegue en Línea:** [https://ajec1804.github.io/WiNetControl/](https://ajec1804.github.io/WiNetControl/)

---

## 👥 Equipo de Desarrollo y Asignación de Issues en GitHub

| Responsable | Correo | Usuario GitHub | Issues Asignados | Módulos y Responsabilidades |
| :--- | :--- | :--- | :---: | :--- |
| **Alejandro Escudero** | `alejandrocastellar04@gmail.com` | [@AJEC1804](https://github.com/AJEC1804) | [#7](https://github.com/AJEC1804/WiNetControl/issues/7) | **Autenticación, Sesión, Menú Principal (Dashboard) y Despliegue en Línea** |
| **Gyanela Meza** | `gianelameza@gmail.com` | [@Gyanelameza](https://github.com/Gyanelameza) | [#8](https://github.com/AJEC1804/WiNetControl/issues/8) | **Gestión y CRUD de Clientes con Control de Estados (Activo/Inactivo)** |
| **Yeini Álvarez** | `yeinypao17@gmail.com` | [@Yeisitaaaa](https://github.com/Yeisitaaaa) | [#9](https://github.com/AJEC1804/WiNetControl/issues/9), [#11](https://github.com/AJEC1804/WiNetControl/issues/11) | **Catálogo de Planes / Contratación + 📝 Trabajo Escrito Oficial del Proaula** |
| **Juan Esteban Navia** | `juannavia2008@gmail.com` | [@JuanesNavia08](https://github.com/JuanesNavia08) | [#10](https://github.com/AJEC1804/WiNetControl/issues/10), [#12](https://github.com/AJEC1804/WiNetControl/issues/12) | **Facturación (LIFO), Soporte (FIFO) + 📊 Diapositivas para la Sustentación** |

---

## 📌 Detalle de Tareas por Integrante

### 1. Alejandro Escudero ([#7](https://github.com/AJEC1804/WiNetControl/issues/7))
*   **Vistas:** `login.html`, `registro.html`, `dashboard.html`.
*   **Lógica / Controladores:** `app.js` / `UsuarioController`.
*   **Tareas:**
    - [x] Login universal con soporte para 3 roles: Superusuario, Administrador Local y Cliente.
    - [x] Validación de seguridad y bloqueo para usuarios en estado `INACTIVO`.
    - [x] Formulario de autorregistro de suscriptores.
    - [x] Dashboard central responsivo adaptativo según el rol sin nombres personales.
    - [x] Despliegue en vivo en GitHub Pages ([https://ajec1804.github.io/WiNetControl/](https://ajec1804.github.io/WiNetControl/)).

---

### 2. Gyanela Meza ([#8](https://github.com/AJEC1804/WiNetControl/issues/8))
*   **Vistas:** `ClientesView` / Sección Clientes en Dashboard.
*   **Controladores:** `UsuarioController`.
*   **Tareas:**
    - [ ] Tabla de clientes listando los datos y su estado (`ACTIVO` / `INACTIVO`) mediante `UsuarioController.listarClientes()`.
    - [ ] Búsqueda de clientes por cédula con `UsuarioController.buscarCliente(...)`.
    - [ ] Edición de datos personales con `UsuarioController.actualizarCliente(...)`.
    - [ ] Botón de **Activar / Desactivar** con `UsuarioController.cambiarEstadoCliente(...)`.
    - [ ] Botón de eliminación con confirmación `UsuarioController.eliminarCliente(...)`.

---

### 3. Yeini Álvarez ([#9](https://github.com/AJEC1804/WiNetControl/issues/9) y [#11](https://github.com/AJEC1804/WiNetControl/issues/11))
*   **Módulo Técnico:** Catálogo de Planes y Contratación.
*   **Entregable Académico:** 📝 **Trabajo Escrito Oficial del Proaula (Informe Final)**.
*   **Tareas de Desarrollo:**
    - [ ] Tabla del catálogo de planes mostrando código, nombre, velocidad, precio, descripción y estado.
    - [ ] Formulario para crear y actualizar planes con `PlanController.crearPlan(...)` y `actualizarPlan(...)`.
    - [ ] Botón para **Activar / Desactivar** planes con `PlanController.cambiarEstadoPlan(...)`.
    - [ ] Vista de contratación: cargar planes activos en un selector con `PlanController.listarPlanesActivos()` y contratar con `UsuarioController.asignarPlan(...)`.
*   **Tareas de Documentación (Issue #11):**
    - [ ] Redacción de la Portada, Introducción, Justificación y Objetivos (General y Específicos).
    - [ ] Documentación del Marco Teórico: Patrón MVC y Estructuras de Datos (Lista Simple, Lista Doble, Pila LIFO, Cola FIFO).
    - [ ] Diagramas de paquetes y arquitectura del sistema.
    - [ ] Descripción de casos de uso, reglas de negocio y control de acceso de los 3 roles.
    - [ ] Registro de evidencias de pruebas automatizadas y capturas de la plataforma web en línea.
    - [ ] Conclusiones y referencias bibliográficas en normas IEEE/APA.

---

### 4. Juan Esteban Navia ([#10](https://github.com/AJEC1804/WiNetControl/issues/10) y [#12](https://github.com/AJEC1804/WiNetControl/issues/12))
*   **Módulos Técnicos:** Facturación (Pila LIFO) y Soporte Técnico (Cola FIFO).
*   **Entregable Académico:** 📊 **Diapositivas para la Sustentación del Proaula**.
*   **Tareas de Desarrollo:**
    - [ ] **Facturación (Pila LIFO):**
        - [ ] Tabla con historial de facturas del cliente usando `FacturaController.obtenerFacturasUsuario(...)`.
        - [ ] Visualización de la última factura en la cima de la pila (`FacturaController.consultarCima()`).
        - [ ] Botón "Abrir PDF" para abrir o descargar el comprobante generado.
    - [ ] **Soporte Técnico (Cola FIFO):**
        - [ ] Formulario de radicación de incidencias técnicas con `SoporteController.radicarTicket(...)`.
        - [ ] Panel administrativo con la lista de tickets en espera (`SoporteController.listarTicketsPendientes()`).
        - [ ] Botón **"Atender Siguiente Ticket"** que desencola en orden FIFO con `SoporteController.atenderSiguienteTicket()`.
*   **Tareas de Presentación (Issue #12):**
    - [ ] Diseño visual de diapositivas (PowerPoint / Canva / Google Slides).
    - [ ] Diapositivas de Problema, Justificación y Solución WiNet Control.
    - [ ] Explicación de la Arquitectura MVC y las 4 Estructuras de Datos utilizadas.
    - [ ] Demostración en vivo de los 3 Roles del Sistema y el despliegue en línea.
    - [ ] Resumen de resultados de pruebas y conclusiones finales para la sustentación.

---

## 🌿 Flujo de Ramas en Git
Cada integrante debe crear y trabajar en su propia rama:
*   Alejandro: `git checkout -b feature/login-dashboard`
*   Gyanela: `git checkout -b feature/gestion-clientes`
*   Yeini: `git checkout -b feature/gestion-planes`
*   Juan: `git checkout -b feature/facturacion-soporte`
