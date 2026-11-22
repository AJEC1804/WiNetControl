package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FormateadorFecha {

    public static final String PATRON_FECHA = "dd/MM/yyyy";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(PATRON_FECHA);

    public static String fechaActualTexto() {
        return LocalDate.now().format(FORMATTER);
    }

    public static String formatear(LocalDate fecha) {
        if (fecha == null) return "";
        return fecha.format(FORMATTER);
    }

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
