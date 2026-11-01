package vistas;

import entidades.Usuario;

/**
 * Vista del Menú Principal / Dashboard - Tarea de Alejandro Escudero (Issue #7).
 * Mantiene la sesión activa y sirve de marco contenedor para integrar
 * los módulos de los demás compañeros del equipo:
 * - Clientes: Gyanela Meza (Issue #8)
 * - Planes: Yeini Álvarez (Issue #9)
 * - Facturación y Soporte: Juan Esteban Navia (Issue #10)
 */
public class DashboardView {

    private Usuario usuarioSesion;

    public DashboardView() {
    }

    public DashboardView(Usuario usuarioSesion) {
        this.usuarioSesion = usuarioSesion;
    }

    public Usuario getUsuarioSesion() {
        return usuarioSesion;
    }

    public void setUsuarioSesion(Usuario usuarioSesion) {
        this.usuarioSesion = usuarioSesion;
    }

    public boolean haySesionActiva() {
        return usuarioSesion != null;
    }

    public void cerrarSesion() {
        this.usuarioSesion = null;
    }
}
