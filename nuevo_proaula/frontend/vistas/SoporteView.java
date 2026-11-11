package vistas;

import controladores.SoporteController;
import entidades.Soporte;
import entidades.Usuario;
import utils.Respuesta;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Vista Gráfica de Mesa de Ayuda y Soporte Técnico (Cola FIFO) - Tarea de Juan Esteban Navia (Issue #10).
 * Implementa la interfaz Swing conectada a SoporteController.
 */
public class SoporteView extends JPanel {

    private final SoporteController soporteController;
    private Usuario usuarioLogueado;

    // Componentes del Formulario de Radicación
    private JTextField txtUsuario;
    private JTextField txtCorreo;
    private JTextField txtAsunto;
    private JTextArea txtMensaje;
    private JComboBox<String> cbCategoria;
    private JButton btnRadicar;

    // Componentes del Panel Administrativo de la Cola FIFO
    private JLabel lblFrenteInfo;
    private JLabel lblTotalCola;
    private JTable tablaTickets;
    private DefaultTableModel modeloTabla;
    private JButton btnAtender;
    private JButton btnRecargar;

    // Colores oficiales del proyecto (GUIA_DE_ESTILOS.md)
    private static final Color COLOR_PRIMARIO = new Color(37, 99, 235);
    private static final Color COLOR_FONDO = new Color(241, 245, 249);
    private static final Color COLOR_TEXTO = new Color(30, 41, 59);
    private static final Color COLOR_TEXTO_SUAVE = new Color(100, 116, 139);
    private static final Color COLOR_EXITO = new Color(22, 163, 74);
    private static final Color COLOR_ALERTA = new Color(217, 119, 6);
    private static final Color COLOR_BORDE = new Color(226, 232, 240);

    private static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FUENTE_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FUENTE_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    public SoporteView() {
        this(null, new SoporteController());
    }

    public SoporteView(Usuario usuarioLogueado) {
        this(usuarioLogueado, new SoporteController());
    }

    public SoporteView(Usuario usuarioLogueado, SoporteController soporteController) {
        this.usuarioLogueado = (usuarioLogueado != null) ? usuarioLogueado : crearUsuarioDemo();
        this.soporteController = (soporteController != null) ? soporteController : new SoporteController();

        inicializarComponentes();
        cargarTicketsEnCola();
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

        // 1. Panel Superior: Encabezado
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(12, 15, 12, 15)
        ));

        JLabel lblTitulo = new JLabel("🎧 Mesa de Ayuda y Soporte Técnico (Cola FIFO)");
        lblTitulo.setFont(FUENTE_TITULO);
        lblTitulo.setForeground(COLOR_PRIMARIO);

        JLabel lblSubtitulo = new JLabel("— Estructura FIFO: los requerimientos se atienden en estricto orden de llegada");
        lblSubtitulo.setFont(FUENTE_REGULAR);
        lblSubtitulo.setForeground(COLOR_TEXTO_SUAVE);

        pnlHeader.add(lblTitulo);
        pnlHeader.add(lblSubtitulo);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Panel Central Dividido: Formulario (Izquierda) + Cola Administrativa (Derecha)
        JPanel pnlCentral = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlCentral.setBackground(COLOR_FONDO);

        // --- COLUMNA IZQUIERDA: FORMULARIO DE RADICACIÓN ---
        JPanel pnlFormulario = new JPanel(new BorderLayout(10, 12));
        pnlFormulario.setBackground(Color.WHITE);
        pnlFormulario.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel lblTituloForm = new JLabel("📝 Radicar Incidencia Técnica (Encolar)");
        lblTituloForm.setFont(FUENTE_SUBTITULO);
        lblTituloForm.setForeground(COLOR_TEXTO);
        pnlFormulario.add(lblTituloForm, BorderLayout.NORTH);

        JPanel pnlCampos = new JPanel(new GridLayout(5, 1, 8, 8));
        pnlCampos.setBackground(Color.WHITE);

        txtUsuario = new JTextField(usuarioLogueado.getNombre() + " " + usuarioLogueado.getApellido());
        txtUsuario.setFont(FUENTE_REGULAR);
        txtUsuario.setEditable(false);
        txtUsuario.setBackground(new Color(248, 250, 252));

        txtCorreo = new JTextField(usuarioLogueado.getCorreo());
        txtCorreo.setFont(FUENTE_REGULAR);

        cbCategoria = new JComboBox<>(new String[]{
                "Falla de Conexión / Sin Señal",
                "Lentitud o Intermitencia",
                "Configuración de Router / WiFi",
                "Consulta de Facturación",
                "Traslado de Servicio"
        });
        cbCategoria.setFont(FUENTE_REGULAR);
        cbCategoria.setBackground(Color.WHITE);

        txtAsunto = new JTextField();
        txtAsunto.setFont(FUENTE_REGULAR);

        pnlCampos.add(crearCampoFormulario("Nombre del Solicitante:", txtUsuario));
        pnlCampos.add(crearCampoFormulario("Correo Electrónico:", txtCorreo));
        pnlCampos.add(crearCampoFormulario("Categoría del Problema:", cbCategoria));
        pnlCampos.add(crearCampoFormulario("Asunto de la Incidencia:", txtAsunto));

        JPanel pnlMensaje = new JPanel(new BorderLayout(4, 4));
        pnlMensaje.setBackground(Color.WHITE);
        JLabel lblMensaje = new JLabel("Descripción Detallada del Problema:");
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMensaje.setForeground(COLOR_TEXTO);

        txtMensaje = new JTextArea(4, 20);
        txtMensaje.setFont(FUENTE_REGULAR);
        txtMensaje.setLineWrap(true);
        txtMensaje.setWrapStyleWord(true);
        JScrollPane scrollMensaje = new JScrollPane(txtMensaje);
        scrollMensaje.setBorder(new LineBorder(COLOR_BORDE, 1));

        pnlMensaje.add(lblMensaje, BorderLayout.NORTH);
        pnlMensaje.add(scrollMensaje, BorderLayout.CENTER);

        JPanel pnlCamposYMensaje = new JPanel(new BorderLayout(0, 10));
        pnlCamposYMensaje.setBackground(Color.WHITE);
        pnlCamposYMensaje.add(pnlCampos, BorderLayout.NORTH);
        pnlCamposYMensaje.add(pnlMensaje, BorderLayout.CENTER);

        pnlFormulario.add(pnlCamposYMensaje, BorderLayout.CENTER);

        btnRadicar = new JButton("📩 Radicar Ticket (Encolar al Final)");
        estilizarBoton(btnRadicar, COLOR_PRIMARIO, Color.WHITE);
        btnRadicar.addActionListener(e -> radicarTicket());
        pnlFormulario.add(btnRadicar, BorderLayout.SOUTH);

        pnlCentral.add(pnlFormulario);

        // --- COLUMNA DERECHA: PANEL ADMINISTRATIVO DE LA COLA FIFO ---
        JPanel pnlCola = new JPanel(new BorderLayout(10, 12));
        pnlCola.setBackground(Color.WHITE);
        pnlCola.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDE, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JPanel pnlColaHeader = new JPanel(new BorderLayout());
        pnlColaHeader.setBackground(Color.WHITE);

        JLabel lblTituloCola = new JLabel("👥 Cola de Solicitudes en Espera (FIFO)");
        lblTituloCola.setFont(FUENTE_SUBTITULO);
        lblTituloCola.setForeground(COLOR_TEXTO);

        lblTotalCola = new JLabel("0 en espera");
        lblTotalCola.setFont(FUENTE_BOLD);
        lblTotalCola.setForeground(COLOR_ALERTA);

        pnlColaHeader.add(lblTituloCola, BorderLayout.WEST);
        pnlColaHeader.add(lblTotalCola, BorderLayout.EAST);

        // Tarjeta info del frente
        JPanel pnlFrenteCaja = new JPanel(new BorderLayout(8, 4));
        pnlFrenteCaja.setBackground(new Color(239, 246, 255));
        pnlFrenteCaja.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel lblFrenteEtiq = new JLabel("🥇 FRENTE DE LA COLA (Primer ticket a ser atendido):");
        lblFrenteEtiq.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblFrenteEtiq.setForeground(COLOR_PRIMARIO);

        lblFrenteInfo = new JLabel("No hay tickets en espera.");
        lblFrenteInfo.setFont(FUENTE_REGULAR);
        lblFrenteInfo.setForeground(COLOR_TEXTO);

        pnlFrenteCaja.add(lblFrenteEtiq, BorderLayout.NORTH);
        pnlFrenteCaja.add(lblFrenteInfo, BorderLayout.CENTER);

        JPanel pnlColaNorte = new JPanel(new BorderLayout(0, 8));
        pnlColaNorte.setBackground(Color.WHITE);
        pnlColaNorte.add(pnlColaHeader, BorderLayout.NORTH);
        pnlColaNorte.add(pnlFrenteCaja, BorderLayout.CENTER);

        pnlCola.add(pnlColaNorte, BorderLayout.NORTH);

        // Tabla con la cola FIFO
        String[] columnas = {"Posición", "N° Ticket", "Solicitante", "Asunto", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaTickets = new JTable(modeloTabla);
        tablaTickets.setFont(FUENTE_REGULAR);
        tablaTickets.setRowHeight(26);
        tablaTickets.getTableHeader().setFont(FUENTE_BOLD);
        tablaTickets.getTableHeader().setBackground(new Color(248, 250, 252));
        tablaTickets.getTableHeader().setForeground(COLOR_TEXTO);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        tablaTickets.getColumnModel().getColumn(0).setCellRenderer(center);
        tablaTickets.getColumnModel().getColumn(1).setCellRenderer(center);
        tablaTickets.getColumnModel().getColumn(4).setCellRenderer(center);

        JScrollPane scrollTabla = new JScrollPane(tablaTickets);
        scrollTabla.setBorder(new LineBorder(COLOR_BORDE, 1));
        pnlCola.add(scrollTabla, BorderLayout.CENTER);

        // Botón Atender Siguiente Ticket (Desencolar)
        JPanel pnlColaBotones = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlColaBotones.setBackground(Color.WHITE);

        btnAtender = new JButton("⚡ Atender Siguiente Ticket");
        estilizarBoton(btnAtender, COLOR_EXITO, Color.WHITE);
        btnAtender.addActionListener(e -> atenderSiguienteTicket());

        btnRecargar = new JButton("🔄 Actualizar Cola");
        estilizarBoton(btnRecargar, new Color(226, 232, 240), COLOR_TEXTO);
        btnRecargar.addActionListener(e -> cargarTicketsEnCola());

        pnlColaBotones.add(btnAtender);
        pnlColaBotones.add(btnRecargar);

        pnlCola.add(pnlColaBotones, BorderLayout.SOUTH);

        pnlCentral.add(pnlCola);
        add(pnlCentral, BorderLayout.CENTER);
    }

    private JPanel crearCampoFormulario(String etiqueta, JComponent campo) {
        JPanel pnl = new JPanel(new BorderLayout(3, 3));
        pnl.setBackground(Color.WHITE);

        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(COLOR_TEXTO);

        pnl.add(lbl, BorderLayout.NORTH);
        pnl.add(campo, BorderLayout.CENTER);
        return pnl;
    }

    private void estilizarBoton(JButton btn, Color colorFondo, Color colorTexto) {
        btn.setFont(FUENTE_BOLD);
        btn.setBackground(colorFondo);
        btn.setForeground(colorTexto);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(9, 16, 9, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Radica un nuevo ticket llamando a SoporteController.radicarTicket(..., false).
     */
    private void radicarTicket() {
        String usuario = txtUsuario.getText().trim();
        String correo = txtCorreo.getText().trim();
        String asunto = txtAsunto.getText().trim();
        String mensaje = txtMensaje.getText().trim();

        if (asunto.isEmpty() || mensaje.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor complete el asunto y la descripción del incidente.",
                    "Campos requeridos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Llamada requerida por la rúbrica
        Respuesta<Soporte> resp = soporteController.radicarTicket(usuario, correo, asunto, mensaje, false);

        if (resp.isExito() && resp.getDatos() != null) {
            Soporte ticket = resp.getDatos();
            JOptionPane.showMessageDialog(this,
                    "¡Ticket #" + ticket.getIdTicket() + " radicado exitosamente!\nHa sido encolado al final de la fila de atención.",
                    "Ticket Encolado (Enqueue)", JOptionPane.INFORMATION_MESSAGE);

            txtAsunto.setText("");
            txtMensaje.setText("");
            cargarTicketsEnCola();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error al radicar ticket:\n" + resp.getMensaje(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Atiende y desencola el ticket en el frente en estricto orden de llegada (FIFO).
     */
    private void atenderSiguienteTicket() {
        Respuesta<Soporte> respSiguiente = soporteController.consultarSiguienteTicket();
        if (!respSiguiente.isExito() || respSiguiente.getDatos() == null) {
            JOptionPane.showMessageDialog(this,
                    "No hay tickets pendientes en la cola de soporte.",
                    "Cola Vacía", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Soporte ticket = respSiguiente.getDatos();
        int opt = JOptionPane.showConfirmDialog(this,
                "¿Desea atender el ticket #" + ticket.getIdTicket() + " del frente de la cola?\n\n" +
                        "Solicitante: " + ticket.getUsuario() + "\n" +
                        "Asunto: " + ticket.getAsunto() + "\n" +
                        "Mensaje: " + ticket.getMensaje(),
                "Atender Ticket (FIFO)",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (opt == JOptionPane.YES_OPTION) {
            // Desencolar y atender
            Respuesta<Soporte> respAtendido = soporteController.atenderSiguienteTicket();
            if (respAtendido.isExito()) {
                JOptionPane.showMessageDialog(this,
                        "¡Ticket #" + ticket.getIdTicket() + " de " + ticket.getUsuario() + " ATENDIDO con éxito!\nSe retiró del frente de la cola (FIFO).",
                        "Ticket Atendido", JOptionPane.INFORMATION_MESSAGE);
                cargarTicketsEnCola();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al atender ticket: " + respAtendido.getMensaje(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Sincroniza la tabla de tickets con SoporteController.listarTicketsPendientes().
     */
    public void cargarTicketsEnCola() {
        modeloTabla.setRowCount(0);
        Respuesta<List<Soporte>> respPendientes = soporteController.listarTicketsPendientes();

        if (respPendientes.isExito() && respPendientes.getDatos() != null) {
            List<Soporte> lista = respPendientes.getDatos();
            lblTotalCola.setText(lista.size() + " en espera");

            if (!lista.isEmpty()) {
                Soporte frente = lista.get(0);
                lblFrenteInfo.setText("Ticket #" + frente.getIdTicket() + " — " + frente.getUsuario() + " (" + frente.getAsunto() + ")");
                btnAtender.setEnabled(true);
            } else {
                lblFrenteInfo.setText("🎉 No hay tickets pendientes en la cola.");
                btnAtender.setEnabled(false);
            }

            for (int i = 0; i < lista.size(); i++) {
                Soporte t = lista.get(i);
                String pos = (i == 0) ? "FRENTE (1°)" : "#" + (i + 1);
                modeloTabla.addRow(new Object[]{
                        pos,
                        "#" + t.getIdTicket(),
                        t.getUsuario(),
                        t.getAsunto(),
                        t.getEstado()
                });
            }
        } else {
            lblTotalCola.setText("0 en espera");
            lblFrenteInfo.setText("No hay tickets en espera.");
            btnAtender.setEnabled(false);
        }
    }

    public void setUsuarioLogueado(Usuario usuarioLogueado) {
        this.usuarioLogueado = usuarioLogueado;
        if (txtUsuario != null && usuarioLogueado != null) {
            txtUsuario.setText(usuarioLogueado.getNombre() + " " + usuarioLogueado.getApellido());
            txtCorreo.setText(usuarioLogueado.getCorreo());
        }
    }

    /**
     * Permite probar y visualizar la vista directamente de forma independiente.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("WiNet Control - Soporte Técnico (FIFO)");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1000, 680);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new SoporteView());
            frame.setVisible(true);
        });
    }
}
