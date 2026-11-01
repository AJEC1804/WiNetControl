# 🎨 Guía de Estilos Visuales y Diseño - WiNet Control

**Autor:** Alejandro Escudero (Issue #7 - Autenticación, Login y Menú Principal)  
**Destinatarios:** Gyanela Meza, Yeini Álvarez, Juan Esteban Navia  
**Objetivo:** Mantener una identidad visual uniforme, moderna y profesional en todos los módulos del proyecto.

---

## 1. 🔤 Tipografía Oficial

Toda la aplicación utiliza la misma familia de fuentes del sistema para una lectura limpia y profesional:

* **Familia Tipográfica:** `'Segoe UI', Tahoma, Geneva, Verdana, sans-serif`
* **En Java Swing:** `new Font("Segoe UI", Font.PLAIN, 14)` o `Font.BOLD`

### Jerarquía y Tamaños Recomendados:
| Elemento | Tamaño Web (CSS) | Tamaño Swing (Java) | Peso / Estilo | Color |
| :--- | :---: | :---: | :--- | :--- |
| **Título Principal (H1)** | `26px` | `24pt - 26pt` | Bold (Negrita 700) | `#2563eb` (Azul) |
| **Título de Sección (H2 / H3)** | `18px - 20px` | `18pt - 20pt` | Semi-bold (600) | `#1e293b` (Oscuro) |
| **Subtítulos y Etiquetas (Label)** | `13px - 14px` | `13pt - 14pt` | Semi-bold (600) | `#1e293b` (Oscuro) |
| **Texto de Cuerpo / Párrafos** | `14px` | `13pt - 14pt` | Regular (Normal 400) | `#334155` (Gris texto) |
| **Notas y Ayudas Secundarias** | `12px` | `11pt - 12pt` | Regular (Normal 400) | `#64748b` (Gris suave) |

---

## 2. 🎨 Paleta de Colores Oficial

| Muestra | Nombre | Código HEX | Código RGB | En Java Swing (`Color`) | Uso Recomendado |
| :---: | :--- | :---: | :---: | :--- | :--- |
| 🟦 | **Azul Primario** | `#2563eb` | `rgb(37, 99, 235)` | `new Color(37, 99, 235)` | Botones principales, logos, títulos destacados |
| 🔷 | **Azul Hover** | `#1d4ed8` | `rgb(29, 78, 216)` | `new Color(29, 78, 216)` | Estado hover de botones y enlaces activos |
| 🌫️ | **Fondo General** | `#f1f5f9` | `rgb(241, 245, 249)` | `new Color(241, 245, 249)` | Fondo general de las ventanas y pantallas |
| ⬜ | **Fondo Tarjetas / Tablas**| `#ffffff` | `rgb(255, 255, 255)` | `Color.WHITE` | Contenedores, formularios y celdas de tablas |
| ⬛ | **Texto Principal** | `#1e293b` | `rgb(30, 41, 59)` | `new Color(30, 41, 59)` | Encabezados, títulos y textos de alta prioridad |
| 🔘 | **Texto Secundario** | `#64748b` | `rgb(100, 116, 139)`| `new Color(100, 116, 139)` | Subtítulos, descripciones secundarias y fechas |
| 🔲 | **Bordes de Cajas** | `#cbd5e1` | `rgb(203, 213, 225)`| `new Color(203, 213, 225)` | Bordes de inputs, tablas y divisiones |
| 🟩 | **Verde Éxito / Activo** | `#16a34a` | `rgb(22, 163, 74)` | `new Color(22, 163, 74)` | Badge de estado ACTIVO, operaciones exitosas |
| 🟥 | **Rojo Error / Inactivo** | `#dc2626` | `rgb(220, 38, 38)` | `new Color(220, 38, 38)` | Badge de estado INACTIVO, botones de eliminar |
| 🟨 | **Ámbar Alerta / Pendiente**| `#d97706` | `rgb(217, 119, 6)` | `new Color(217, 119, 6)` | Tickets en cola, avisos de facturas pendientes |

---

## 3. 🧩 Componentes y Clases CSS Listas para Usar

En los archivos HTML y CSS (`css/styles.css`), los compañeros pueden usar directamente estas clases:

### A. Botones
* **Botón Primario (Guardar, Crear, Buscar):**
  ```html
  <button class="btn btn-primary">Guardar Datos</button>
  ```
* **Botón Secundario (Limpiar, Cancelar):**
  ```html
  <button class="btn btn-secondary">Cancelar</button>
  ```
* **Botón de Peligro (Eliminar, Desactivar):**
  ```html
  <button class="btn btn-danger">Eliminar</button>
  ```

### B. Campos de Formulario (Inputs)
```html
<div class="form-group">
    <label for="campo">Nombre del Campo:</label>
    <input type="text" id="campo" placeholder="Escribe aquí...">
</div>
```

### C. Etiquetas de Estado (Badges)
* Estado Activo: `<span class="badge badge-activo">ACTIVO</span>`
* Estado Inactivo: `<span class="badge badge-inactivo">INACTIVO</span>`

### D. Tarjetas de Contenedor
```html
<div style="background: #ffffff; padding: 25px; border-radius: 10px; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);">
    <!-- Contenido de tu módulo aquí -->
</div>
```
