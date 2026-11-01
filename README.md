# 🌐 WiNet Control - Sistema de Gestión de Telecomunicaciones

Plataforma integral de gestión de telecomunicaciones e internet para proveedores de servicios (ISP). Implementada bajo el patrón arquitectónico **Modelo-Vista-Controlador (MVC)**, con estructuras de datos enlazadas nativas y control de acceso basado en roles.

---

## 🏛️ Estructura del Proyecto

```text
WiNetControl/
├── nuevo_proaula/
│   ├── backend/                      # Núcleo de lógica y estructuras en Java (MVC)
│   │   ├── lib/                      # Librerías (PDF, Email, UI Layout)
│   │   └── src/
│   │       ├── controladores/        # Intermediarios de peticiones (Usuario, Plan, Factura, Soporte)
│   │       ├── entidades/            # Objetos de negocio (Usuario, Administrador, Cliente, Plan, Factura, Soporte)
│   │       ├── metodos/              # Estructuras de datos puras por punteros (ListaSimple, ListaDoble, Pila LIFO, Cola FIFO)
│   │       ├── modelos/              # Lógica de negocio y reglas comerciales
│   │       ├── utils/                # Cifrado SHA-256, PDF, correo, validadores y formateadores
│   │       └── MainBackendTest.java  # Suite de verificación y pruebas automatizadas (100% OK)
│   │
│   └── frontend/                     # Interfaz de usuario web moderna (HTML5, CSS3, JavaScript)
│       ├── css/styles.css            # Estilos visuales, sistema de badges y diseño responsivo
│       ├── js/app.js                 # Lógica de sesión, roles en tiempo real y persistencia local
│       ├── login.html                # Formulario de inicio de sesión universal
│       ├── registro.html             # Formulario de registro de clientes suscriptores
│       └── dashboard.html            # Panel central de control adaptativo según el rol
│
└── README.md
```

---

## 🔑 Roles y Cuentas Preconfiguradas

El sistema implementa 3 niveles de permisos y control de acceso:

| Rol | Usuario / Cédula | Contraseña | Nivel de Acceso y Funcionalidades |
| :--- | :--- | :--- | :--- |
| 👑 **Superusuario** (`SUPERADMIN`) | `superadmin` / `9999999999` | `Super123!` | **Control Global Total:** Supervisa todas las sedes/sucursales, auditoría del sistema, catálogo de planes y administración de administradores locales. |
| 🏢 **Administrador Local** (`ADMIN`) | `admin123` / `1000000001` | `Admin123!` | **Gestión de Sucursal:** Búsqueda y registro de suscriptores, activación/desactivación de clientes y planes, cobros y cola de soporte (FIFO). |
| 👤 **Cliente** (`CLIENTE`) | `1045678901` | `Clave123` | **Portal del Suscriptor:** Consulta de estado del servicio, velocidad contratada, catálogo comercial, descarga de facturas en PDF y radicación de tickets. |
| 🚫 **Cliente Inactivo** *(Prueba)* | `1098765432` | `Clave456` | Demuestra la regla de negocio que **rechaza el ingreso** de cuentas suspendidas o inactivas. |

---

## 🧩 Estructuras de Datos Nativas Implementadas

1. **Lista Simplemente Enlazada (`ListaClientes`):** Registro de clientes y suscriptores con punteros de inicio a fin.
2. **Lista Doblemente Enlazada (`ListaPlanes`):** Catálogo comercial de planes con recorrido bidireccional (`adelante` y `atrás`).
3. **Pila LIFO (`PilaFacturas`):** Historial de facturas donde la última emitida siempre está en la cima (`Top`) para cobro inmediato.
4. **Cola FIFO (`ColaSoporte`):** Mesa de ayuda y soporte técnico con atención en estricto orden de llegada.

---

## 🧪 Ejecución de Pruebas del Backend (Java)

Para compilar y ejecutar toda la suite de pruebas unitarias e integrales del backend:

```bash
# Compilar todas las clases del backend
javac -cp "nuevo_proaula/backend/lib/*;nuevo_proaula/backend/src" -d bin $(Get-ChildItem -Path nuevo_proaula/backend/src -Recurse -Filter *.java | ForEach-Object { $_.FullName })

# Ejecutar el test principal
java -cp "bin;nuevo_proaula/backend/lib/*" MainBackendTest
```

---

## 👥 Equipo de Trabajo y Asignación de Tareas

* **Alejandro Escudero (Lead / Auth / Dashboard):** Módulo de Login universal, registro de suscriptores, sesión, arquitectura de 3 roles y despliegue en línea (Issue [#7](https://github.com/AJEC1804/WiNetControl/issues/7)).
* **Gyanela Meza:** Módulo de Gestión de Clientes (búsqueda, edición y control de estado activo/inactivo) (Issue [#8](https://github.com/AJEC1804/WiNetControl/issues/8)).
* **Yeini Álvarez:** Catálogo de Planes de Telecomunicaciones (Issue [#9](https://github.com/AJEC1804/WiNetControl/issues/9)) + 📝 **Elaboración del Trabajo Escrito Oficial del Proaula** (Issue [#11](https://github.com/AJEC1804/WiNetControl/issues/11)).
* **Juan Esteban Navia:** Módulos de Facturación y Soporte (Issue [#10](https://github.com/AJEC1804/WiNetControl/issues/10)) + 📊 **Diseño de Diapositivas para la Sustentación** (Issue [#12](https://github.com/AJEC1804/WiNetControl/issues/12)).
