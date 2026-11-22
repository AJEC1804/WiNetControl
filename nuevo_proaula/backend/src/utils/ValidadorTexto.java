package utils;

import java.util.regex.Pattern;

public class ValidadorTexto {

    private static final Pattern PATRON_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static boolean esNoVacio(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    public static boolean validarIdentificacion(String identificacion) {
        if (!esNoVacio(identificacion)) return false;
        String limpia = identificacion.trim();
        return limpia.length() >= 8 && limpia.length() <= 10 && limpia.matches("\\d+");
    }

    public static boolean validarCorreo(String correo) {
        if (!esNoVacio(correo)) return false;
        String limpio = correo.trim();
        return PATRON_EMAIL.matcher(limpio).matches() || (limpio.contains("@") && limpio.contains("."));
    }

    public static boolean validarTelefono(String telefono) {
        if (!esNoVacio(telefono)) return false;
        String limpio = telefono.trim();
        return limpio.length() == 10 && limpio.matches("\\d+");
    }

    public static boolean validarPassword(String password) {
        if (!esNoVacio(password)) return false;
        String limpio = password.trim();
        return limpio.length() >= 6
                && limpio.matches(".*[A-Za-z].*")
                && limpio.matches(".*\\d.*");
    }

    public static Integer parseEntero(String valor) {
        if (!esNoVacio(valor)) return null;
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Double parseDouble(String valor) {
        if (!esNoVacio(valor)) return null;
        try {
            return Double.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
