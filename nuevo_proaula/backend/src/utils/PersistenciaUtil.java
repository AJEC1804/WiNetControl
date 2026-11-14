package utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Utilidad de persistencia en archivos binarios (.dat) usando serialización de Java.
 *
 * <p>Guarda y recupera estructuras completas (listas, pilas y colas enlazadas) en la
 * carpeta {@code datos/}, ubicada junto al directorio desde el que se ejecuta el
 * programa. Al escribir se usa un archivo temporal y se renombra al final, de modo
 * que una caída a mitad de la escritura no deje el archivo corrupto.</p>
 *
 * <p><b>Importante:</b> la persistencia está desactivada por defecto. Se activa con
 * {@link #activar()} desde {@code ApiServer.main}, de manera que las pruebas
 * automatizadas ({@code MainBackendTest}) trabajen siempre con datos limpios en
 * memoria y no dependan de lo que haya en disco.</p>
 */
public final class PersistenciaUtil {

    private static final String CARPETA = "datos";

    private static volatile boolean activa = false;

    private PersistenciaUtil() {
        // clase de utilidades: no se instancia
    }

    /** Activa la lectura y escritura de archivos. */
    public static void activar() {
        activa = true;
    }

    /** Desactiva la persistencia (todo queda en memoria). */
    public static void desactivar() {
        activa = false;
    }

    public static boolean estaActiva() {
        return activa;
    }

    /**
     * Guarda un objeto serializable en {@code datos/<nombreArchivo>}.
     * Si la persistencia está desactivada, el estado es nulo o hay un error de
     * entrada/salida, no se lanza excepción: solo se informa por consola.
     */
    public static void guardar(Serializable estado, String nombreArchivo) {
        if (!activa || estado == null) {
            return;
        }

        File carpeta = new File(CARPETA);
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            System.err.println("[persistencia] No se pudo crear la carpeta " + CARPETA);
            return;
        }

        File destino = new File(carpeta, nombreArchivo);
        File temporal = new File(carpeta, nombreArchivo + ".tmp");

        // Se declaran los dos recursos: si el constructor del ObjectOutputStream falla,
        // el FileOutputStream tambien se cierra y el archivo no queda bloqueado.
        try (FileOutputStream archivoSalida = new FileOutputStream(temporal);
             ObjectOutputStream salida = new ObjectOutputStream(
                     new BufferedOutputStream(archivoSalida))) {
            salida.writeObject(estado);
        } catch (IOException e) {
            System.err.println("[persistencia] Error al guardar " + nombreArchivo + ": " + e.getMessage());
            temporal.delete();
            return;
        }

        // Se reemplaza el archivo de forma atómica (funciona en Windows aunque exista)
        try {
            Files.move(temporal.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("[persistencia] No se pudo reemplazar " + nombreArchivo + ": " + e.getMessage());
            temporal.delete();
        }
    }

    /**
     * Carga el objeto guardado en {@code datos/<nombreArchivo>}.
     * Devuelve {@code null} si la persistencia está desactivada, el archivo no
     * existe, está corrupto o su contenido no corresponde al tipo esperado.
     */
    public static <T> T cargar(String nombreArchivo, Class<T> tipo) {
        if (!activa) {
            return null;
        }

        File origen = new File(CARPETA, nombreArchivo);
        if (!origen.exists()) {
            return null;
        }

        // Igual que al escribir: el FileInputStream se declara aparte para que se
        // cierre aunque el constructor del ObjectInputStream falle (archivo corrupto).
        try (FileInputStream archivoEntrada = new FileInputStream(origen);
             ObjectInputStream entrada = new ObjectInputStream(
                     new BufferedInputStream(archivoEntrada))) {
            Object leido = entrada.readObject();
            if (tipo.isInstance(leido)) {
                return tipo.cast(leido);
            }
            System.err.println("[persistencia] " + nombreArchivo + " no contiene un "
                    + tipo.getSimpleName() + " válido; se aparta y se empieza vacío.");
            apartar(origen, nombreArchivo);
            return null;
        } catch (InvalidClassException e) {
            System.err.println("[persistencia] " + nombreArchivo + " fue guardado con una versión "
                    + "anterior de las clases (cambió algún campo o el serialVersionUID).\n"
                    + "               Se aparta el archivo para no perderlo y se arranca con datos vacíos.");
            apartar(origen, nombreArchivo);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[persistencia] Error al leer " + nombreArchivo + ": " + e.getMessage()
                    + " -> se aparta el archivo y se arranca vacío.");
            apartar(origen, nombreArchivo);
        }
        return null;
    }

    /**
     * Aparta un archivo dañado o incompatible en vez de borrarlo, para que el
     * usuario pueda inspeccionarlo. Queda como {@code nombre.incompatible-<fecha>.bak}.
     */
    private static void apartar(File archivo, String nombreArchivo) {
        String marca = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date());
        File respaldo = new File(archivo.getParentFile(), nombreArchivo + ".incompatible-" + marca + ".bak");
        try {
            Files.move(archivo.toPath(), respaldo.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.err.println("[persistencia] Archivo apartado como: " + respaldo.getName());
        } catch (IOException e) {
            System.err.println("[persistencia] No se pudo apartar " + nombreArchivo
                    + " (" + e.getMessage() + "). Borralo a mano desde la carpeta datos/ para volver a empezar.");
        }
    }

    /** Elimina un archivo de datos. Devuelve true si existía y se pudo borrar. */
    public static boolean borrar(String nombreArchivo) {
        File archivo = new File(CARPETA, nombreArchivo);
        return archivo.exists() && archivo.delete();
    }

    /** Indica si existe un archivo de datos con ese nombre. */
    public static boolean existe(String nombreArchivo) {
        return new File(CARPETA, nombreArchivo).exists();
    }
}
