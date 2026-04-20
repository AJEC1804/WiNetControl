package controlador;

import model.factura;
import model.Planes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.File;
import java.io.FileOutputStream;

public class ctrlFactura {

    private static final int MAX = 100;
    private static factura[] facturas = new factura[MAX];
    private static int contador = 0;
    private static int IdFactura = 1;
    private static final String RUTA = "facturas/";

    // Crear una nueva factura
    public static factura crearFactura(String nombreUsuario, Planes planUsuario, String archivoPDF) {
    if (contador >= MAX) return null;

    LocalDate hoy = LocalDate.now();
    String fecha = hoy.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

    factura f = new factura(IdFactura++, nombreUsuario, planUsuario.getIdPlan(), fecha, planUsuario.getPrecio(), archivoPDF);
    facturas[contador++] = f;
    return f;
}

    public static factura buscarFacturaMes(String nombreCompleto, int mes, int ano) {
        String token = "-" + mes + "-" + ano + ".pdf";
        for (int i = 0; i < contador; i++) {
            factura f = facturas[i];
            if (f != null && f.getUsuario().equalsIgnoreCase(nombreCompleto) && f.getArchivoPDF().contains(token)) {
                return f;
            }
        }
        return null;
    }

    public static factura[] obtenerFacturasUsuario(String usuario) {
        factura[] temp = new factura[MAX];
        int c = 0;
        for (int i = 0; i < contador; i++) {
            if (facturas[i].getUsuario().equalsIgnoreCase(usuario)) {
                temp[c++] = facturas[i];
            }
        }
        factura[] resultado = new factura[c];
        System.arraycopy(temp, 0, resultado, 0, c);
        return resultado;
    }

    public static String rutaFacturaEsperada(String usuario, int mes, int ano) {
        return RUTA + "factura_" + usuario.replace(" ", "_") + "_" + mes + "_" + ano + ".pdf";
    }

    public static int getContador() {
        return contador;
    }

    public static factura[] getTodasFacturas() {
        factura[] resultado = new factura[contador];
        System.arraycopy(facturas, 0, resultado, 0, contador);
        return resultado;
    }
    
    public static boolean generarFacturaPDF(factura f, Planes planUsuario) {
    try {
        File carpeta = new File("facturas");
        if (!carpeta.exists()) carpeta.mkdirs();

        Document doc = new Document();
        PdfWriter.getInstance(doc, new FileOutputStream(f.getArchivoPDF()));
        doc.open();

        doc.add(new Paragraph("WiNet Control- FACTURA DE SERVICIO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("Factura numero: " + f.getIdFactura()));
        doc.add(new Paragraph("Fecha en que se genera: " + f.getFecha()));
        doc.add(new Paragraph("Usuario: " + f.getUsuario()));
        doc.add(new Paragraph("ID Plan: " + planUsuario.getIdPlan()));
        doc.add(new Paragraph("Nombre del Plan: " + planUsuario.getNombrePlan()));
        doc.add(new Paragraph("Valor mensual: $" + planUsuario.getPrecio()));
        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("¡Gracias por confiar en nosotros, recuerda hacer tus pagos a tiempo!", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        doc.close();
        return true;

    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
}