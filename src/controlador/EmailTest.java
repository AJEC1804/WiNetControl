package controlador;

public class EmailTest {
    public static void main(String[] args) {
        String destino = "alejandrocastellar04@gmail.com";
        String asunto = "Prueba desde WiNetControl";
        String cuerpo = "Este es un correo de prueba enviado desde la clase EmailTest.";

        boolean enviado = correoSoporte.enviarCorreo(destino, asunto, cuerpo);
        System.out.println("Resultado envio: " + enviado);
    }
}
