package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utilidades para el manejo de contraseñas.
 *
 * <p>Formato de almacenamiento actual (con sal):</p>
 * <pre>sha256$&lt;sal en hexadecimal&gt;$&lt;hash en hexadecimal&gt;</pre>
 *
 * <p>Cada contraseña usa una sal aleatoria distinta, de modo que dos usuarios con la
 * misma contraseña no producen el mismo valor almacenado y una tabla precalculada
 * (rainbow table) no sirve para atacarlas.</p>
 *
 * <p>Se mantiene la verificación del formato anterior (SHA-256 sin sal) para que las
 * contraseñas ya guardadas sigan funcionando; ese formato se conserva únicamente por
 * compatibilidad y no se usa para contraseñas nuevas.</p>
 */
public final class EncriptadorUtil {

    private static final String PREFIJO_CON_SAL = "sha256$";
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final int BYTES_SAL = 16;

    private EncriptadorUtil() {
        // clase de utilidades: no se instancia
    }

    /**
     * Genera el valor a almacenar para una contraseña nueva, aplicando SHA-256 con una
     * sal aleatoria: {@code sha256$<sal>$<hash>}.
     */
    public static String encriptarContrasena(String textoPlano) {
        if (textoPlano == null) {
            return null;
        }
        String sal = salAleatoria();
        return PREFIJO_CON_SAL + sal + "$" + sha256Hex(textoPlano, sal);
    }

    /**
     * SHA-256 sin sal. Se conserva solo para leer contraseñas guardadas con el formato
     * anterior; no debe usarse para almacenar contraseñas nuevas.
     */
    public static String encriptarSHA256(String textoPlano) {
        if (textoPlano == null) {
            return null;
        }
        return sha256Hex(textoPlano, null);
    }

    /**
     * Compara una contraseña en texto plano contra el valor almacenado.
     * Acepta el formato con sal ({@code sha256$sal$hash}) y el anterior sin sal.
     * Si la contraseña se guardó o no existe, devuelve {@code false}.
     */
    public static boolean verificar(String textoPlano, String almacenado) {
        if (textoPlano == null || almacenado == null || almacenado.isEmpty()) {
            return false;
        }

        // Formato actual: sha256$<sal>$<hash>
        if (almacenado.startsWith(PREFIJO_CON_SAL)) {
            String[] partes = almacenado.split("\\$");
            if (partes.length != 3 || partes[1].isEmpty()) {
                return false;
            }
            String sal = partes[1];
            String esperado = partes[2];
            return sha256Hex(textoPlano, sal).equalsIgnoreCase(esperado);
        }

        // Formato anterior: hash SHA-256 sin sal (solo lectura, por compatibilidad)
        return sha256Hex(textoPlano, null).equalsIgnoreCase(almacenado);
    }

    // ------------------------------------------------------------------
    // Internos
    // ------------------------------------------------------------------

    private static String salAleatoria() {
        byte[] bytes = new byte[BYTES_SAL];
        ALEATORIO.nextBytes(bytes);
        return aHexadecimal(bytes);
    }

    private static String sha256Hex(String texto, String sal) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            if (sal != null) {
                // La sal se antepone al texto antes de calcular el hash
                digest.update(sal.getBytes(StandardCharsets.UTF_8));
            }
            return aHexadecimal(digest.digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error en algoritmo de encriptación", e);
        }
    }

    private static String aHexadecimal(byte[] bytes) {
        StringBuilder salida = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                salida.append('0');
            }
            salida.append(hex);
        }
        return salida.toString();
    }
}
