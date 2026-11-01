package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utilidades para manejo y formateo de fechas.
 */
public class FormateadorFecha {

    public static final String PATRON_FECHA = "dd/MM/yyyy";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(PATRON_FECHA);

    /**
     * Retorna la fecha actual como cadena con formato dd/MM/yyyy.
     */
    public static String fechaActualTexto() {
        return LocalDate.now().format(FORMATTER);
    }

    /**
     * Formatea un objeto LocalDate al formato dd/MM/yyyy.
     */
    public static String formatear(LocalDate fecha) {
        if (fecha == null) return "";
        return fecha.format(FORMATTER);
    }

    /**
     * Parsea un texto a LocalDate usando el formato dd/MM/yyyy.
     */
    public static LocalDate parsear(String textoFecha) {
        if (textoFecha == null || textoFecha.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(textoFecha.trim(), FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static int getMesActual() {
        return LocalDate.now().getMonthValue();
    }

    public static int getAnoActual() {
        return LocalDate.now().getYear();
    }
}
