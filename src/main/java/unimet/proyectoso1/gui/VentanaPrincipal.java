package unimet.proyectoso1.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import unimet.proyectoso1.estructuras.Cola;
import unimet.proyectoso1.modelo.PCB;
import unimet.proyectoso1.sistema.Nucleo;

public class VentanaPrincipal extends JFrame {

    private final Color COLOR_FONDO = new Color(10, 10, 25);
    private final Color COLOR_NEON_PURPURA = new Color(150, 0, 255);
    private final Color COLOR_CIAN = new Color(0, 255, 255);
    private final Font FUENTE_MONO = new Font("Monospaced", Font.BOLD, 14);

    private DefaultTableModel modListos, modBloqueados, modNuevos, modSuspBloq;
    
    private JLabel lblNombre, lblPC, lblDeadline, lblReloj;
    private JProgressBar barraProgresoCPU;
    private JTextArea areaLog;
    private JComboBox<String> comboAlgoritmo;
    private JSlider sliderVelocidad;

    public VentanaPrincipal() {
        configurarVentana();
        inicializarComponentes();
        this.setLocationRelativeTo(null); 
        this.setVisible(true); 
    }

    private void configurarVentana() {
        setTitle("UNIMET-Sat RTOS Simulator - Mission Control");
        setSize(1350, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout(15, 15));
    }

    private void inicializarComponentes() {
        JPanel pnlNorte = new JPanel(new BorderLayout());
        pnlNorte.setOpaque(false);
        pnlNorte.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel pnlControles = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pnlControles.setOpaque(false);

        JButton btnAleatorios = new JButton("Generar 20 Procesos");
        JButton btnEmergencia = new JButton("EMERGENCY INTERRUPTION");
        btnEmergencia.setBackground(new Color(200, 0, 0));
        btnEmergencia.setForeground(Color.WHITE);

        String[] algos = {"FCFS", "Prioridad", "Round Robin", "SRT", "EDF"};
        comboAlgoritmo = new JComboBox<>(algos);
        
        sliderVelocidad = new JSlider(100, 2000, 1000);
        sliderVelocidad.setOpaque(false);

        pnlControles.add(btnAleatorios);
        pnlControles.add(btnEmergencia);
        pnlControles.add(crearLabelSimple("Algoritmo:"));
        pnlControles.add(comboAlgoritmo);
        pnlControles.add(crearLabelSimple("Velocidad:"));
        pnlControles.add(sliderVelocidad);

        lblReloj = new JLabel("MISSION CLOCK: Cycle 0000");
        lblReloj.setFont(new Font("Monospaced", Font.BOLD, 24));
        lblReloj.setForeground(COLOR_CIAN);

        pnlNorte.add(pnlControles, BorderLayout.WEST);
        pnlNorte.add(lblReloj, BorderLayout.EAST);

        JPanel pnlCentro = new JPanel(new GridLayout(2, 3, 20, 20));
        pnlCentro.setOpaque(false);
        pnlCentro.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Inicializar modelos
        modListos = new DefaultTableModel(new String[]{"Process [ID]", "Priority"}, 0);
        modNuevos = new DefaultTableModel(new String[]{"Process [ID]", "Priority"}, 0);
        modBloqueados = new DefaultTableModel(new String[]{"Process [ID]", "Priority"}, 0);
        modSuspBloq = new DefaultTableModel(new String[]{"Process [ID]", "Priority"}, 0);

        pnlCentro.add(crearPanelTabla("READY QUEUE (RAM)", modListos));
        pnlCentro.add(crearPanelCPU());
        pnlCentro.add(crearPanelTabla("BLOCKED QUEUE (I/O)", modBloqueados));

        pnlCentro.add(crearPanelTabla("READY-SUSPENDED (DISK)", modNuevos));
        pnlCentro.add(crearPanelLog()); // Panel central inferior para mensajes del sistema
        pnlCentro.add(crearPanelTabla("BLOCKED-SUSPENDED", modSuspBloq));

        add(pnlNorte, BorderLayout.NORTH);
        add(pnlCentro, BorderLayout.CENTER);

        btnAleatorios.addActionListener(e -> generarProcesosLote(20));
        btnEmergencia.addActionListener(e -> activarEmergencia());
    }

    private JPanel crearPanelTabla(String titulo, DefaultTableModel modelo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COLOR_FONDO);
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_NEON_PURPURA, 2), 
                titulo, 0, 0, null, COLOR_NEON_PURPURA));

        JTable t = new JTable(modelo);
        t.setBackground(new Color(20, 20, 45));
        t.setForeground(Color.WHITE);
        t.setFont(new Font("Arial", Font.PLAIN, 12));
        t.setRowHeight(22);
        t.setFillsViewportHeight(true);
        
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        return p;
    }

    private JPanel crearPanelCPU() {
        JPanel p = new JPanel(new GridLayout(4, 1, 10, 10));
        p.setBackground(new Color(15, 15, 40));
        p.setBorder(BorderFactory.createLineBorder(COLOR_CIAN, 3));

        JLabel titulo = new JLabel("RUNNING PROCESS (CPU)", SwingConstants.CENTER);
        titulo.setForeground(COLOR_CIAN);
        titulo.setFont(new Font("Arial", Font.BOLD, 16));

        lblNombre = new JLabel("CPU IDLE", SwingConstants.CENTER);
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setFont(FUENTE_MONO);

        barraProgresoCPU = new JProgressBar(0, 100);
        barraProgresoCPU.setStringPainted(true);
        barraProgresoCPU.setForeground(COLOR_CIAN);
        barraProgresoCPU.setBackground(Color.BLACK);

        lblDeadline = new JLabel("Deadline: --", SwingConstants.CENTER);
        lblDeadline.setForeground(Color.YELLOW);

        p.add(titulo);
        p.add(lblNombre);
        p.add(barraProgresoCPU);
        p.add(lblDeadline);
        
        return p;
    }

    private JPanel crearPanelLog() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COLOR_FONDO);
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_CIAN), "MISSION CONTROL LOG", 0, 0, null, COLOR_CIAN));
        
        areaLog = new JTextArea();
        areaLog.setBackground(Color.BLACK);
        areaLog.setForeground(new Color(0, 255, 100));
        areaLog.setEditable(false);
        areaLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        
        p.add(new JScrollPane(areaLog), BorderLayout.CENTER);
        return p;
    }

    private JLabel crearLabelSimple(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Color.WHITE);
        return l;
    }


    public void refrescarTodo() {
        SwingUtilities.invokeLater(() -> {
            try {
                Nucleo.mutex.acquire();

                lblReloj.setText(String.format("MISSION CLOCK: Cycle %04d", Nucleo.relojDelSistema));

                PCB actual = Nucleo.procesoEnEjecucion;
                if (actual != null) {
                    lblNombre.setText(actual.getNombre() + " [ID: " + actual.getId() + "]");
                    lblDeadline.setText("Deadline in: " + actual.getDeadline() + " cycles");
                    
                    int porcentaje = (actual.getProgramCounter() * 100) / actual.getInstruccionesTotales();
                    barraProgresoCPU.setValue(porcentaje);
                    barraProgresoCPU.setString(porcentaje + "% (PC: " + actual.getProgramCounter() + ")");
                } else {
                    lblNombre.setText("SYSTEM IDLE");
                    barraProgresoCPU.setValue(0);
                    barraProgresoCPU.setString("0%");
                    lblDeadline.setText("Waiting for processes...");
                }

                actualizarTablaManual(modListos, Nucleo.colaListos);
                actualizarTablaManual(modNuevos, Nucleo.colaNuevos); 
                

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                Nucleo.mutex.release();
            }
        });
    }

    private void actualizarTablaManual(DefaultTableModel modelo, Cola<PCB> cola) {
        modelo.setRowCount(0); 
        for (int i = 0; i < cola.getTamano(); i++) {
            PCB p = cola.obtenerPorIndice(i); 
            if (p != null) {
                modelo.addRow(new Object[]{
                    p.getNombre() + " [ID:" + p.getId() + "]", 
                    p.getPrioridad()
                });
            }
        }
    }

    private void generarProcesosLote(int cantidad) {
        try {
            Nucleo.mutex.acquire();
            for (int i = 0; i < cantidad; i++) {
                int id = (int)(Math.random() * 900) + 100;
                PCB nuevo = new PCB(id, "P_Job", (int)(Math.random() * 5) + 1, 10 + (int)(Math.random() * 10), 100, 0);
                Nucleo.colaNuevos.encolar(nuevo);
            }
            log("Injected " + cantidad + " satellite tasks into Disk Queue.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            Nucleo.mutex.release();
            refrescarTodo(); 
        }
    }

    private void activarEmergencia() {
        log("!!! EMERGENCY INTERRUPTION: COLLISION ALERT !!!");
    }

    public void log(String msg) {
        areaLog.append("> " + msg + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }
}