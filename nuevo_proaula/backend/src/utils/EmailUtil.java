package utils;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Utilidad desacoplada para envío de notificaciones y correos de soporte vía SMTP.
 */
public class EmailUtil {

    private static final String REMITENTE_DEFAULT = "naviavasquezjuanesteban@gmail.com";
    private static final String CLAVE_DEFAULT = "rvxq kmpq hiej bggm";

    /**
     * Envía un correo electrónico mediante el servidor SMTP configurado.
     * Retorna true si fue enviado exitosamente o false si ocurrió algún error de conexión.
     */
    public static boolean enviarCorreo(String destinatario, String asunto, String mensaje) {
        if (!ValidadorTexto.esNoVacio(destinatario)) {
            System.err.println("[EmailUtil] Destinatario no válido.");
            return false;
        }

        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.auth", "true");
        propiedades.put("mail.smtp.starttls.enable", "true");
        propiedades.put("mail.smtp.host", "smtp.gmail.com");
        propiedades.put("mail.smtp.port", "587");
        propiedades.put("mail.smtp.connectiontimeout", "3000");
        propiedades.put("mail.smtp.timeout", "3000");

        try {
            Session sesion = Session.getInstance(propiedades, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(REMITENTE_DEFAULT, CLAVE_DEFAULT);
                }
            });

            Message msg = new MimeMessage(sesion);
            msg.setFrom(new InternetAddress(REMITENTE_DEFAULT));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            msg.setSubject(asunto);
            msg.setText(mensaje);

            Transport.send(msg);
            System.out.println("[EmailUtil] Correo enviado exitosamente a: " + destinatario);
            return true;
        } catch (Exception e) {
            System.err.println("[EmailUtil] No se pudo enviar el correo (" + e.getMessage() + ").");
            return false;
        }
    }
}
