package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class EncriptadorUtil {

    private static final String PREFIJO_CON_SAL = "sha256$";
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final int BYTES_SAL = 16;

    private EncriptadorUtil() {

    }

    public static String encriptarContrasena(String textoPlano) {
        if (textoPlano == null) {
            return null;
        }
        String sal = salAleatoria();
        return PREFIJO_CON_SAL + sal + "$" + sha256Hex(textoPlano, sal);
    }

    public static String encriptarSHA256(String textoPlano) {
        if (textoPlano == null) {
            return null;
        }
        return sha256Hex(textoPlano, null);
    }

    public static boolean verificar(String textoPlano, String almacenado) {
        if (textoPlano == null || almacenado == null || almacenado.isEmpty()) {
            return false;
        }

        if (almacenado.startsWith(PREFIJO_CON_SAL)) {
            String[] partes = almacenado.split("\\$");
            if (partes.length != 3 || partes[1].isEmpty()) {
                return false;
            }
            String sal = partes[1];
            String esperado = partes[2];
            return sha256Hex(textoPlano, sal).equalsIgnoreCase(esperado);
        }

        return sha256Hex(textoPlano, null).equalsIgnoreCase(almacenado);
    }

    private static String salAleatoria() {
        byte[] bytes = new byte[BYTES_SAL];
        ALEATORIO.nextBytes(bytes);
        return aHexadecimal(bytes);
    }

    private static String sha256Hex(String texto, String sal) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            if (sal != null) {

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
