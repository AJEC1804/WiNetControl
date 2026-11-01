package utils;

/**
 * Contenedor genérico de respuesta para la capa de Controladores.
 * Permite desacoplar el backend del frontend (Swing, Web, JavaFX, etc.),
 * devolviendo el estado de la operación, un mensaje y los datos resultantes.
 *
 * @param <T> Tipo de dato devuelto en la respuesta
 */
public class Respuesta<T> {

    private boolean exito;
    private String mensaje;
    private T datos;
    private int codigo;

    public Respuesta(boolean exito, String mensaje, T datos, int codigo) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.datos = datos;
        this.codigo = codigo;
    }

    public static <T> Respuesta<T> exito(String mensaje, T datos) {
        return new Respuesta<>(true, mensaje, datos, 200);
    }

    public static <T> Respuesta<T> exito(String mensaje) {
        return new Respuesta<>(true, mensaje, null, 200);
    }

    public static <T> Respuesta<T> error(String mensaje, int codigo) {
        return new Respuesta<>(false, mensaje, null, codigo);
    }

    public static <T> Respuesta<T> error(String mensaje) {
        return new Respuesta<>(false, mensaje, null, 400);
    }

    public boolean isExito() {
        return exito;
    }

    public void setExito(boolean exito) {
        this.exito = exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public T getDatos() {
        return datos;
    }

    public void setDatos(T datos) {
        this.datos = datos;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    @Override
    public String toString() {
        return "Respuesta{" +
                "exito=" + exito +
                ", mensaje='" + mensaje + '\'' +
                ", datos=" + datos +
                ", codigo=" + codigo +
                '}';
    }
}
