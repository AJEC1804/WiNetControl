package utils;

import java.util.regex.Pattern;

/**
 * Utilidades para validación de entradas de texto y reglas de formato comunes.
 */
public class ValidadorTexto {

    private static final Pattern PATRON_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Valida si un texto no es nulo ni está vacío.
     */
    public static boolean esNoVacio(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    /**
     * Valida identificación numérica colombiana estándar (entre 8 y 10 dígitos numéricos).
     */
    public static boolean validarIdentificacion(String identificacion) {
        if (!esNoVacio(identificacion)) return false;
        String limpia = identificacion.trim();
        return limpia.length() >= 8 && limpia.length() <= 10 && limpia.matches("\\d+");
    }

    /**
     * Valida formato de correo electrónico.
     */
    public static boolean validarCorreo(String correo) {
        if (!esNoVacio(correo)) return false;
        String limpio = correo.trim();
        return PATRON_EMAIL.matcher(limpio).matches() || (limpio.contains("@") && limpio.contains("."));
    }

    /**
     * Valida número telefónico móvil (10 dígitos numéricos).
     */
    public static boolean validarTelefono(String telefono) {
        if (!esNoVacio(telefono)) return false;
        String limpio = telefono.trim();
        return limpio.length() == 10 && limpio.matches("\\d+");
    }

    /**
     * Valida seguridad mínima de contraseña: al menos 6 caracteres, al menos una letra y un dígito.
     */
    public static boolean validarPassword(String password) {
        if (!esNoVacio(password)) return false;
        String limpio = password.trim();
        return limpio.length() >= 6
                && limpio.matches(".*[A-Za-z].*")
                && limpio.matches(".*\\d.*");
    }

    /**
     * Convierte un texto a número entero; retorna null si es inválido.
     */
    public static Integer parseEntero(String valor) {
        if (!esNoVacio(valor)) return null;
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Convierte un texto a número decimal; retorna null si es inválido.
     */
    public static Double parseDouble(String valor) {
        if (!esNoVacio(valor)) return null;
        try {
            return Double.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
