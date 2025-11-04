package controlador;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 *
 * @author EUMAPE
 */
public class correoSoporte {

    public static boolean enviarCorreo(String destinatario, String asunto, String mensaje) {
        final String correoEnvia = "mauriciomaldonadomercado@gmail.com";
        final String contrasena = "ryfq bhpb sonr ulqd";

        Properties propiedad = new Properties();
        propiedad.setProperty("mail.smtp.auth", "true");
        propiedad.setProperty("mail.smtp.starttls.enable", "true");
        propiedad.setProperty("mail.smtp.host", "smtp.gmail.com");
        propiedad.setProperty("mail.smtp.port", "587");

        Session session = Session.getInstance(propiedad, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(correoEnvia, contrasena);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(correoEnvia));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(asunto);
            message.setText(mensaje);

            Transport.send(message);

            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}