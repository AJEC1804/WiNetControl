package utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Utilidad reutilizable para la generación física de facturas y reportes en formato PDF.
 */
public class GeneradorPdfUtil {

    /**
     * Genera un archivo PDF para la factura especificada.
     *
     * @param rutaDestino    Ruta de salida (por ejemplo: "facturas/factura_123.pdf")
     * @param idFactura      Identificador numérico de la factura
     * @param nombreCliente  Nombre o identificación del cliente
     * @param nombrePlan     Nombre del plan contratado
     * @param velocidadPlan  Velocidad del plan (ej: 100 Mbps)
     * @param total          Monto a facturar
     * @param fecha          Fecha de generación (ej: dd/MM/yyyy)
     * @return true si se generó con éxito, false en caso contrario.
     */
    public static boolean generarFacturaPdf(String rutaDestino, int idFactura, String nombreCliente,
                                           String nombrePlan, String velocidadPlan, double total, String fecha) {
        try {
            File archivo = new File(rutaDestino);
            File directorio = archivo.getParentFile();
            if (directorio != null && !directorio.exists()) {
                directorio.mkdirs();
            }

            Document documento = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(documento, new FileOutputStream(archivo));
            documento.open();

            // Título
            Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.DARK_GRAY);
            Paragraph titulo = new Paragraph("WiNet Control - FACTURA DE SERVICIO", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);

            documento.add(new Paragraph(" "));

            // Datos de la factura
            Font fuenteNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            Font fuenteNormal = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);

            documento.add(new Paragraph("Factura No.: " + idFactura, fuenteNegrita));
            documento.add(new Paragraph("Fecha de Emisión: " + fecha, fuenteNormal));
            documento.add(new Paragraph("Cliente / Titular: " + nombreCliente, fuenteNormal));
            documento.add(new Paragraph("Plan Contratado: " + nombrePlan, fuenteNormal));
            if (velocidadPlan != null && !velocidadPlan.isEmpty()) {
                documento.add(new Paragraph("Velocidad: " + velocidadPlan, fuenteNormal));
            }
            documento.add(new Paragraph(String.format("Total a Pagar: $%,.2f COP", total), fuenteNegrita));

            documento.add(new Paragraph(" "));
            documento.add(new Paragraph("----------------------------------------------------------------------------------------------------"));
            documento.add(new Paragraph(" "));

            // Mensaje de cierre
            Font fuentePie = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, BaseColor.GRAY);
            Paragraph pie = new Paragraph("¡Gracias por confiar en WiNet Control! Recuerda realizar tus pagos oportunamente.", fuentePie);
            pie.setAlignment(Element.ALIGN_CENTER);
            documento.add(pie);

            documento.close();
            return true;
        } catch (Exception e) {
            System.err.println("[GeneradorPdfUtil] Error generando PDF: " + e.getMessage());
            return false;
        }
    }
}
