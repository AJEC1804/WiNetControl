package vistas;

import controladores.FacturaController;
import entidades.Factura;
import entidades.Usuario;
import utils.Respuesta;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Vista Gráfica de Facturación y Comprobantes (Pila LIFO) - Tarea de Juan Esteban Navia (Issue #10).
 * Implementa la interfaz Swing conectada a FacturaController.
 */
public class FacturasView extends JPanel {

    private final FacturaController facturaController;
    private Usuario usuarioLogueado;

    // Componentes de la interfaz Swing
    private JLabel lblCimaId;
    private JLabel lblCimaSuscriptor;
    private JLabel lblCimaPlan;
    private JLabel lblCimaFecha;
    private JLabel lblCimaMonto;
    private JLabel lblCimaEstado;

    private JTable tablaFacturas;
    private DefaultTableModel modeloTabla;
    private JButton btnAbrirPdf;
    private JButton btnDesapilar;
    private JButton btnEmitirManual;
    private JButton btnRecargar;

    // Colores oficiales del proyecto (GUIA_DE_ESTILOS.md)
    private static final Color COLOR_PRIMARIO = new Color(37, 99, 235);
    private static final Color COLOR_FONDO = new Color(241, 245, 249);
    private static final Color COLOR_TEXTO = new Color(30, 41, 59);
    private static final Color COLOR_TEXTO_SUAVE = new Color(100, 116, 139);
    private static final Color COLOR_EXITO = new Color(22, 163, 74);
    private static final Color COLOR_PELIGRO = new Color(220, 38, 38);
    private static final Color COLOR_BORDE = new Color(226, 232, 240);

    private static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FUENTE_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FUENTE_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    public FacturasView() {
        this(null, new FacturaController());
    }

    public FacturasView(Usuario usuarioLogueado) {
        this(usuarioLogueado, new FacturaController());
    }

    public FacturasView(Usuario usuarioLogueado, FacturaController facturaController) {
        this.usuarioLogueado = (usuarioLogueado != null) ? usuarioLogueado : crearUsuarioDemo();
        this.facturaController = (facturaController != null) ? facturaController : new FacturaController();

        inicializarComponentes();
        cargarDatos();
    }

    private Usuario crearUsuarioDemo() {
        Usuario demo = new Usuario();
        demo.setIdentificacion("1045678901");
        demo.setNombre("Alejandro");
        demo.setApellido("Escudero");
        demo.setCorreo("alejandrocastellar04@gmail.com");
        demo.setRol("ADMIN");
        return demo;
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(15, 15));
        setBackground(COLOR_FONDO);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. Panel Superior (Encabezado y Tarjeta de la Cima LIFO)
        JPanel pnlSuperior = new JPanel(new BorderLayout(10, 10));
        pnlSuperior.setBackground(COLOR_FONDO);

        // Encabezado
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(12, 15, 12, 15)
        ));

        JLabel lblTitulo = new JLabel("📄 Módulo de Facturación y Comprobantes (Pila LIFO)");
        lblTitulo.setFont(FUENTE_TITULO);
        lblTitulo.setForeground(COLOR_PRIMARIO);

        JLabel lblSubtitulo = new JLabel("— Estructura LIFO: la última factura emitida se ubica siempre en la cima");
        lblSubtitulo.setFont(FUENTE_REGULAR);
        lblSubtitulo.setForeground(COLOR_TEXTO_SUAVE);

        pnlHeader.add(lblTitulo);
        pnlHeader.add(lblSubtitulo);
        pnlSuperior.add(pnlHeader, BorderLayout.NORTH);

        // Tarjeta Cima de la Pila
        JPanel pnlCima = new JPanel(new BorderLayout(10, 10));
        pnlCima.setBackground(Color.WHITE);
        pnlCima.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_PRIMARIO, 2, true),
                new EmptyBorder(15, 18, 15, 18)
        ));

        JPanel pnlCimaTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlCimaTitulo.setBackground(Color.WHITE);
        JLabel lblTituloCima = new JLabel("📌 Cima de la Pila (Última Factura Emitida en el Sistema)");
        lblTituloCima.setFont(FUENTE_SUBTITULO);
        lblTituloCima.setForeground(COLOR_TEXTO);

        JLabel lblBadgeCima = new JLabel(" TOPE LIFO ");
        lblBadgeCima.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadgeCima.setOpaque(true);
        lblBadgeCima.setBackground(new Color(239, 246, 255));
        lblBadgeCima.setForeground(COLOR_PRIMARIO);
        lblBadgeCima.setBorder(new LineBorder(new Color(191, 219, 254), 1, true));

        pnlCimaTitulo.add(lblTituloCima);
        pnlCimaTitulo.add(lblBadgeCima);
        pnlCima.add(pnlCimaTitulo, BorderLayout.NORTH);

        // Grid con datos de la cima
        JPanel pnlCimaDatos = new JPanel(new GridLayout(2, 3, 15, 10));
        pnlCimaDatos.setBackground(Color.WHITE);

        lblCimaId = crearLabelValor("Cargando...");
        lblCimaSuscriptor = crearLabelValor("Cargando...");
        lblCimaPlan = crearLabelValor("Cargando...");
        lblCimaFecha = crearLabelValor("Cargando...");
        lblCimaMonto = crearLabelValor("Cargando...");
        lblCimaMonto.setForeground(COLOR_EXITO);
        lblCimaEstado = crearLabelValor("Cargando...");

        pnlCimaDatos.add(crearCajaDato("Factura N°:", lblCimaId));
        pnlCimaDatos.add(crearCajaDato("Suscriptor:", lblCimaSuscriptor));
        pnlCimaDatos.add(crearCajaDato("Plan Contratado:", lblCimaPlan));
        pnlCimaDatos.add(crearCajaDato("Fecha de Emisión:", lblCimaFecha));
        pnlCimaDatos.add(crearCajaDato("Monto Total:", lblCimaMonto));
        pnlCimaDatos.add(crearCajaDato("Estado:", lblCimaEstado));

        pnlCima.add(pnlCimaDatos, BorderLayout.CENTER);
        pnlSuperior.add(pnlCima, BorderLayout.CENTER);

        add(pnlSuperior, BorderLayout.NORTH);

        // 2. Panel Central (Tabla con Historial de Facturas)
        JPanel pnlCentro = new JPanel(new BorderLayout(10, 10));
        pnlCentro.setBackground(Color.WHITE);
        pnlCentro.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblHistorial = new JLabel("📋 Historial de Facturas Registradas en la Pila");
        lblHistorial.setFont(FUENTE_SUBTITULO);
        lblHistorial.setForeground(COLOR_TEXTO);
        pnlCentro.add(lblHistorial, BorderLayout.NORTH);

        String[] columnas = {"Posición", "ID Factura", "Suscriptor", "Plan", "Fecha", "Monto", "Estado", "Ruta PDF"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo lectura
            }
        };

        tablaFacturas = new JTable(modeloTabla);
        tablaFacturas.setFont(FUENTE_REGULAR);
        tablaFacturas.setRowHeight(28);
        tablaFacturas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaFacturas.getTableHeader().setFont(FUENTE_BOLD);
        tablaFacturas.getTableHeader().setBackground(new Color(248, 250, 252));
        tablaFacturas.getTableHeader().setForeground(COLOR_TEXTO);

        // Centrar columnas clave
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tablaFacturas.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tablaFacturas.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tablaFacturas.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tablaFacturas.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        JScrollPane scrollTabla = new JScrollPane(tablaFacturas);
        scrollTabla.setBorder(new LineBorder(COLOR_BORDE, 1));
        pnlCentro.add(scrollTabla, BorderLayout.CENTER);

        add(pnlCentro, BorderLayout.CENTER);

        // 3. Panel Inferior (Barra de Botones de Acción)
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        pnlBotones.setBackground(COLOR_FONDO);

        btnAbrirPdf = new JButton("📄 Ver / Abrir PDF");
        estilizarBoton(btnAbrirPdf, COLOR_PRIMARIO, Color.WHITE);
        btnAbrirPdf.addActionListener(e -> abrirPdfSeleccionado());

        btnDesapilar = new JButton("⬇️ Desapilar Última Factura (Pop LIFO)");
        estilizarBoton(btnDesapilar, COLOR_PELIGRO, Color.WHITE);
        btnDesapilar.addActionListener(e -> desapilarUltimaFactura());

        btnEmitirManual = new JButton("➕ Emitir Nueva Factura (Push)");
        estilizarBoton(btnEmitirManual, COLOR_EXITO, Color.WHITE);
        btnEmitirManual.addActionListener(e -> emitirFacturaManual());

        btnRecargar = new JButton("🔄 Recargar");
        estilizarBoton(btnRecargar, new Color(226, 232, 240), COLOR_TEXTO);
        btnRecargar.addActionListener(e -> cargarDatos());

        pnlBotones.add(btnAbrirPdf);
        pnlBotones.add(btnEmitirManual);
        pnlBotones.add(btnDesapilar);
        pnlBotones.add(btnRecargar);

        add(pnlBotones, BorderLayout.SOUTH);
    }

    private JPanel crearCajaDato(String etiqueta, JLabel lblValor) {
        JPanel pnl = new JPanel(new BorderLayout(4, 4));
        pnl.setBackground(new Color(248, 250, 252));
        pnl.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel lblEtiq = new JLabel(etiqueta);
        lblEtiq.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblEtiq.setForeground(COLOR_TEXTO_SUAVE);

        pnl.add(lblEtiq, BorderLayout.NORTH);
        pnl.add(lblValor, BorderLayout.CENTER);
        return pnl;
    }

    private JLabel crearLabelValor(String textoInicial) {
        JLabel lbl = new JLabel(textoInicial);
        lbl.setFont(FUENTE_BOLD);
        lbl.setForeground(COLOR_TEXTO);
        return lbl;
    }

    private void estilizarBoton(JButton btn, Color colorFondo, Color colorTexto) {
        btn.setFont(FUENTE_BOLD);
        btn.setBackground(colorFondo);
        btn.setForeground(colorTexto);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Carga y sincroniza la cima de la pila y la tabla con FacturaController.
     */
    public void cargarDatos() {
        // 1. Consultar Cima de la Pila (Peek)
        Respuesta<Factura> respCima = facturaController.consultarCima();
        if (respCima.isExito() && respCima.getDatos() != null) {
            Factura cima = respCima.getDatos();
            lblCimaId.setText("#00" + cima.getIdFactura());
            lblCimaSuscriptor.setText(cima.getUsuario());
            lblCimaPlan.setText(cima.getNombrePlan());
            lblCimaFecha.setText(cima.getFecha());
            lblCimaMonto.setText(String.format("$%,.0f COP", cima.getTotal()));
            lblCimaEstado.setText(cima.getEstado() + " ✅");
        } else {
            lblCimaId.setText("N/A");
            lblCimaSuscriptor.setText("Pila vacía");
            lblCimaPlan.setText("—");
            lblCimaFecha.setText("—");
            lblCimaMonto.setText("$0");
            lblCimaEstado.setText("Sin facturas");
        }

        // 2. Cargar Historial en la Tabla
        modeloTabla.setRowCount(0);
        Respuesta<List<Factura>> respListado;

        // Si es administrador muestra todas; si es cliente muestra las suyas
        if (usuarioLogueado != null && "CLIENTE".equalsIgnoreCase(usuarioLogueado.getRol())) {
            respListado = facturaController.obtenerFacturasUsuario(usuarioLogueado.getIdentificacion());
        } else {
            respListado = facturaController.listarTodasFacturas();
        }

        if (respListado.isExito() && respListado.getDatos() != null) {
            List<Factura> lista = respListado.getDatos();
            for (int i = 0; i < lista.size(); i++) {
                Factura f = lista.get(i);
                String pos = (i == 0) ? "CIMA (Top)" : "#" + (i + 1);
                modeloTabla.addRow(new Object[]{
                        pos,
                        "#00" + f.getIdFactura(),
                        f.getUsuario(),
                        f.getNombrePlan(),
                        f.getFecha(),
                        String.format("$%,.0f", f.getTotal()),
                        f.getEstado(),
                        f.getArchivoPDF()
                });
            }
        }
    }

    /**
     * Abre físicamente el archivo PDF generado en disco usando java.awt.Desktop.
     */
    private void abrirPdfSeleccionado() {
        int fila = tablaFacturas.getSelectedRow();
        String rutaPDF = null;

        if (fila >= 0) {
            rutaPDF = (String) modeloTabla.getValueAt(fila, 7);
        } else {
            // Si no seleccionó fila, intenta abrir la factura de la cima
            Respuesta<Factura> respCima = facturaController.consultarCima();
            if (respCima.isExito() && respCima.getDatos() != null) {
                rutaPDF = respCima.getDatos().getArchivoPDF();
            }
        }

        if (rutaPDF == null || rutaPDF.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione una factura de la tabla para ver su comprobante.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Apertura física con java.awt.Desktop
        try {
            File archivo = new File(rutaPDF);
            if (archivo.exists()) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(archivo);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "La función Desktop no es soportada en este entorno.",
                            "Aviso", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "El archivo físico no se encontró en disco:\n" + archivo.getAbsolutePath(),
                        "Archivo no encontrado", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al abrir la factura PDF: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Desapila la última factura emitida (Pop LIFO) conectando con el controlador.
     */
    private void desapilarUltimaFactura() {
        Respuesta<Factura> respCima = facturaController.consultarCima();
        if (!respCima.isExito() || respCima.getDatos() == null) {
            JOptionPane.showMessageDialog(this,
                    "La pila de facturas ya está vacía.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Factura cima = respCima.getDatos();
        int opt = JOptionPane.showConfirmDialog(this,
                "¿Desea desapilar (Pop LIFO) la factura #00" + cima.getIdFactura() + " de " + cima.getUsuario() + "?\nEsta acción retirará la última factura de la cima.",
                "Confirmar Desapilado",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (opt == JOptionPane.YES_OPTION) {
            Respuesta<Factura> respDesapilar = facturaController.desapilarUltimaFactura();
            if (respDesapilar.isExito()) {
                JOptionPane.showMessageDialog(this,
                        "Factura #00" + respDesapilar.getDatos().getIdFactura() + " desapilada exitosamente.\nLa pila retrocedió a la factura anterior.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarDatos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al desapilar: " + respDesapilar.getMensaje(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Emite una nueva factura (Push LIFO) y la apila en el sistema.
     */
    private void emitirFacturaManual() {
        String idCliente = JOptionPane.showInputDialog(this,
                "Ingrese el número de cédula o identificación del suscriptor a facturar:",
                "Emitir Factura Manual",
                JOptionPane.QUESTION_MESSAGE);

        if (idCliente == null || idCliente.trim().isEmpty()) return;

        Respuesta<Factura> resp = facturaController.generarFactura(idCliente.trim(), null);
        if (resp.isExito()) {
            JOptionPane.showMessageDialog(this,
                    "¡Factura #00" + resp.getDatos().getIdFactura() + " emitida y apilada en la cima exitosamente!",
                    "Factura Emitida", JOptionPane.INFORMATION_MESSAGE);
            cargarDatos();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo emitir la factura:\n" + resp.getMensaje(),
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void setUsuarioLogueado(Usuario usuarioLogueado) {
        this.usuarioLogueado = usuarioLogueado;
        cargarDatos();
    }

    /**
     * Permite probar y visualizar la vista directamente de forma independiente.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("WiNet Control - Facturación (LIFO)");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(950, 680);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new FacturasView());
            frame.setVisible(true);
        });
    }
}
