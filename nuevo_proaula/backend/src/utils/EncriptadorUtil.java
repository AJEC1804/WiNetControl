package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Funciones de ayuda para encriptación / hashing de contraseñas.
 */
public class EncriptadorUtil {

    /**
     * Genera un hash SHA-256 en formato hexadecimal para una contraseña en texto plano.
     */
    public static String encriptarSHA256(String textoPlano) {
        if (textoPlano == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(textoPlano.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error en algoritmo de encriptación", e);
        }
    }

    /**
     * Compara una contraseña en texto plano contra un hash almacenado o contraseña previa.
     */
    public static boolean verificar(String textoPlano, String hashEsperado) {
        if (textoPlano == null || hashEsperado == null) return false;
        String hashGenerado = encriptarSHA256(textoPlano);
        return hashGenerado.equalsIgnoreCase(hashEsperado) || textoPlano.equals(hashEsperado);
    }
}
