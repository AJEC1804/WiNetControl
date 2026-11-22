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

public final class PersistenciaUtil {

    private static final String CARPETA = "datos";

    private static volatile boolean activa = false;

    private PersistenciaUtil() {

    }

    public static void activar() {
        activa = true;
    }

    public static void desactivar() {
        activa = false;
    }

    public static boolean estaActiva() {
        return activa;
    }

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

        try (FileOutputStream archivoSalida = new FileOutputStream(temporal);
             ObjectOutputStream salida = new ObjectOutputStream(
                     new BufferedOutputStream(archivoSalida))) {
            salida.writeObject(estado);
        } catch (IOException e) {
            System.err.println("[persistencia] Error al guardar " + nombreArchivo + ": " + e.getMessage());
            temporal.delete();
            return;
        }

        try {
            Files.move(temporal.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("[persistencia] No se pudo reemplazar " + nombreArchivo + ": " + e.getMessage());
            temporal.delete();
        }
    }

    public static <T> T cargar(String nombreArchivo, Class<T> tipo) {
        if (!activa) {
            return null;
        }

        File origen = new File(CARPETA, nombreArchivo);
        if (!origen.exists()) {
            return null;
        }

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

    public static boolean borrar(String nombreArchivo) {
        File archivo = new File(CARPETA, nombreArchivo);
        return archivo.exists() && archivo.delete();
    }

    public static boolean existe(String nombreArchivo) {
        return new File(CARPETA, nombreArchivo).exists();
    }
}
