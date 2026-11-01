# ⚙️ BACKEND - WiNet Control ProAula (Arquitectura MVC Limpia)

Backend modular desarrollado en **Java 21** para el proyecto de aula de Estructuras de Datos, implementando el patrón **Modelo-Vista-Controlador (MVC)** con estructuras dinámicas construidas mediante nodos y punteros desde cero.

---

## 📁 Estructura del Backend

```
nuevo_proaula/backend/
├── facturas/                     # Salida de facturas físicas generadas en PDF
├── lib/                          # Librerías externas (iTextPDF, JavaMail, Activation)
│   ├── AbsoluteLayout.jar
│   ├── activation-1.1.1 (1).jar
│   ├── itextpdf-5.5.13.3.jar
│   └── javax.mail-1.6.2 (1).jar
└── src/
    ├── MainBackendTest.java      # Suite de pruebas automatizadas del Backend
    ├── entidades/                # Representaciones de negocio
    │   ├── Usuario.java          # Entidad base
    │   ├── Cliente.java          # Entidad cliente suscriptor
    │   ├── Plan.java             # Entidad plan de internet
    │   ├── Factura.java          # Entidad factura emitida
    │   └── Soporte.java          # Entidad ticket de soporte técnico
    ├── metodos/                  # Estructuras de datos por punteros y operaciones CRUD
    │   ├── Nodo.java             # Nodo base con referencias anterior y siguiente
    │   ├── ListaSimple.java      # Lista simplemente enlazada completa de clase
    │   ├── ListaDoble.java       # Lista doblemente enlazada completa de clase
    │   ├── Pila.java             # Pila genérica (LIFO) por punteros
    │   ├── Cola.java             # Cola genérica (FIFO) por punteros
    │   ├── ListaClientes.java    # Lista doble de clientes con CRUD completo
    │   ├── ListaPlanes.java      # Lista doble de planes con CRUD completo
    │   ├── PilaFacturas.java     # Pila LIFO de facturas
    │   └── ColaSoporte.java      # Cola FIFO de soporte técnico
    ├── modelos/                  # Lógica de negocio y reglas de estructuración
    │   ├── ClienteModel.java     # Reglas comerciales, validaciones, login y asignación de plan
    │   ├── PlanModel.java        # Reglas de planes y reasignación en cascada
    │   ├── FacturaModel.java     # Emisión de facturas y generación de PDF
    │   └── SoporteModel.java     # Radicación y atención FIFO de tickets
    ├── controladores/            # Intermediarios limpios desacoplados de la UI
    │   ├── UsuarioController.java    # Login, Registro, CRUD y Asignación de plan
    │   ├── PlanController.java       # Catálogo y CRUD de planes
    │   ├── FacturaController.java    # Facturación y descarga
    │   └── SoporteController.java    # Tickets y atención en orden de llegada
    └── utils/                    # Funciones de ayuda reutilizables
        ├── Respuesta.java        # Contenedor uniforme de respuestas (DTO)
        ├── ValidadorTexto.java   # Validaciones de cédula, celular, correo y clave
        ├── FormateadorFecha.java # Manejo de fechas en formato dd/MM/yyyy
        ├── EncriptadorUtil.java  # Hashing y verificación con SHA-256
        ├── GeneradorPdfUtil.java # Creación de facturas PDF con iText
        └── EmailUtil.java        # Notificaciones por correo vía SMTP
```
