package controlador;

import entidades.Nodos;
import model.factura;
import model.Planes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.File;
import java.io.FileOutputStream;

/**
 *
 * @author Gyanela Meza
 */
public class ctrlFactura {

    // Cima de la pila
    private static Nodos.NodoFactura cima = null;
    private static int IdFactura = 1;
    private static final String RUTA = "facturas/";

    // Metodo para apilar una factura en la cima (LIFO)
    public static void apilar(factura nueva) {
        if (nueva == null) {
            return;
        }
        Nodos.NodoFactura nuevo = new Nodos.NodoFactura(nueva);
        if (cima == null) {
            cima = nuevo;
        } else {
            nuevo.setSiguiente(cima);
            cima = nuevo;
        }
    }

    // Metodo para desapilar la factura de la cima
    public static factura desapilar() {
        if (cima == null) {
            return null;
        }
        Nodos.NodoFactura aux = cima;
        cima = cima.getSiguiente();
        return aux.getDato();
    }

    // Metodo para consultar la cima
    public static factura getCima() {
        return (cima != null) ? cima.getDato() : null;
    }

    // Crear una nueva factura y apilarla
    public static factura crearFactura(String nombreUsuario, Planes planUsuario, String archivoPDF) {
        LocalDate hoy = LocalDate.now();
        String fecha = hoy.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        factura f = new factura(IdFactura++, nombreUsuario, planUsuario.getIdPlan(), fecha, planUsuario.getPrecio(), archivoPDF);
        apilar(f);
        return f;
    }

    // Buscar factura por mes y ano recorriendo la pila desde la cima
    public static factura buscarFacturaMes(String nombreCompleto, int mes, int ano) {
        String token = "-" + mes + "-" + ano + ".pdf";
        Nodos.NodoFactura aux = cima;
        while (aux != null) {
            factura f = aux.getDato();
            if (f != null && f.getUsuario().equalsIgnoreCase(nombreCompleto) && f.getArchivoPDF().contains(token)) {
                return f;
            }
            aux = aux.getSiguiente();
        }
        return null;
    }

    // Obtener facturas del usuario (la mas reciente primero)
    public static factura[] obtenerFacturasUsuario(String usuario) {
        int c = 0;
        Nodos.NodoFactura aux = cima;
        while (aux != null) {
            if (aux.getDato() != null && aux.getDato().getUsuario().equalsIgnoreCase(usuario)) {
                c++;
            }
            aux = aux.getSiguiente();
        }

        factura[] resultado = new factura[c];
        aux = cima;
        int i = 0;
        while (aux != null) {
            if (aux.getDato() != null && aux.getDato().getUsuario().equalsIgnoreCase(usuario)) {
                resultado[i++] = aux.getDato();
            }
            aux = aux.getSiguiente();
        }
        return resultado;
    }

    public static String rutaFacturaEsperada(String usuario, int mes, int ano) {
        return RUTA + "factura_" + usuario.replace(" ", "_") + "-" + mes + "-" + ano + ".pdf";
    }

    // Obtener todas las facturas apiladas
    public static factura[] getTodasFacturas() {
        int total = 0;
        Nodos.NodoFactura aux = cima;
        while (aux != null) {
            total++;
            aux = aux.getSiguiente();
        }
        factura[] resultado = new factura[total];
        aux = cima;
        int i = 0;
        while (aux != null) {
            resultado[i++] = aux.getDato();
            aux = aux.getSiguiente();
        }
        return resultado;
    }

    // Generar archivo PDF con la factura
    public static boolean generarFacturaPDF(factura f, Planes planUsuario) {
        try {
            File carpeta = new File("facturas");
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

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
