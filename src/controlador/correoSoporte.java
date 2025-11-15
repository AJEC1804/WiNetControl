package controlador;

import com.sun.jdi.connect.Transport;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * 
 *
 * @author EUMAPE
 */
public class correoSoporte {

    public static boolean enviarCorreo(String destinatario, String asunto, String mensaje) {
        final String correoEnvia = "mauriciomaldonadomercado@gmail.com";
        final String contrasena = "ryfq bhpb sonr ulqd"; // Contraseña de aplicación

        Properties propiedad = new Properties();
        propiedad.put("mail.smtp.auth", "true");
        propiedad.put("mail.smtp.starttls.enable", "true");
        propiedad.put("mail.smtp.host", "smtp.gmail.com");
        propiedad.put("mail.smtp.port", "587");

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

            // Usa el Transport correcto de javax.mail
            Transport.send(message);

            System.out.println("Correo enviado correctamente a " + destinatario);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
