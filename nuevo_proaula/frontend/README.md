# 🖥️ FRONTEND - Guía de Integración con el Backend (MVC)

El **Backend** está 100% listo, probado y estructurado con lo básico y esencial del proyecto para que puedan empezar directamente a diseñar la interfaz gráfica.

---

## 🏗️ Arquitectura Limpia (MVC)
- **Controladores (`controladores.*`)**: Intermediarios directos entre la interfaz gráfica y los datos.
  - `UsuarioController`: Registro, login, actualizar, eliminar, buscar y asignación de planes.
  - `PlanController`: Crear plan, actualizar, eliminar, buscar y catálogo.
  - `FacturaController`: Generar factura, consultar historial y desapilar (Pila LIFO).
  - `SoporteController`: Radicar tickets y atender en orden de llegada (Cola FIFO).
- **Modelos (`modelos.*`)**: `ClienteModel`, `PlanModel`, `FacturaModel`, `SoporteModel`.
- **Entidades (`entidades.*`)**: `Cliente`, `Plan`, `Factura`, `Soporte`, `Usuario`.
- **Métodos (`metodos.*`)**: Punteros directos en `ListaClientes`, `ListaPlanes`, `PilaFacturas`, `ColaSoporte`, `ListaSimple`, `ListaDoble`.
- **Utils (`utils.*`)**: `Respuesta`, `ValidadorTexto`, `FormateadorFecha`, `EncriptadorUtil`, `GeneradorPdfUtil`.

---

## 🚀 Uso Rápido desde la Interfaz Gráfica

### 1. Inicialización
```java
import controladores.*;

UsuarioController usuarioCtrl = new UsuarioController();
PlanController planCtrl = new PlanController();
FacturaController facturaCtrl = new FacturaController();
SoporteController soporteCtrl = new SoporteController();
```

---

### 2. Login de Usuario
```java
String id = txtIdentificacion.getText().trim();
String clave = new String(txtPassword.getPassword());

Respuesta<Cliente> resp = usuarioCtrl.login(id, clave);

if (resp.isExito()) {
    Cliente cliente = resp.getDatos();
    System.out.println("Bienvenido: " + cliente.getNombreCompleto());
} else {
    System.err.println("Error: " + resp.getMensaje());
}
```

---

### 3. Registro de Cliente
```java
Respuesta<Cliente> resp = usuarioCtrl.registrarCliente(
    txtNombre.getText(),
    txtApellido.getText(),
    cbTipo.getSelectedItem().toString(),
    txtIdentificacion.getText(),
    txtTelefono.getText(),
    txtCorreo.getText(),
    txtDireccion.getText(),
    new String(txtClave.getPassword()),
    new String(txtConfirmarClave.getPassword())
);
```

---

### 4. Asignación de Plan (Emite Factura Automática)
```java
Respuesta<Cliente> resp = usuarioCtrl.asignarPlan("1045678901", "Plan Fibra 100M");
```

---

### 5. Activar / Desactivar Clientes y Planes
```java
// Desactivar un cliente (le impedirá iniciar sesión y contratar planes)
usuarioCtrl.desactivarCliente("1045678901");

// Reactivar cliente
usuarioCtrl.activarCliente("1045678901");

// Desactivar un plan (no aparecerá disponible para contratación)
planCtrl.desactivarPlan("101");

// Reactivar un plan
planCtrl.activarPlan("101");

// Obtener solo los planes activos para cargar en un JComboBox / lista:
Respuesta<List<Plan>> planesActivos = planCtrl.listarPlanesActivos();
```

---

### 6. Facturación (Pila LIFO)
```java
// Consultar facturas del cliente
Respuesta<List<Factura>> facturas = facturaCtrl.obtenerFacturasUsuario("1045678901");

// Consultar última factura emitida
Respuesta<Factura> ultima = facturaCtrl.consultarCima();
```

---

### 6. Soporte Técnico (Cola FIFO)
```java
// Radicar requerimiento (se encola al final)
soporteCtrl.radicarTicket("1045678901", "correo@ejemplo.com", "Sin señal", "No enciende la luz de internet", false);

// Atender ticket (se desencola del frente en orden de llegada)
Respuesta<Soporte> atendido = soporteCtrl.atenderSiguienteTicket();
```
