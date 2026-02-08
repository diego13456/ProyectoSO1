package unimet.proyectoso1.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import unimet.proyectoso1.estructuras.Cola;
import unimet.proyectoso1.modelo.PCB;
import unimet.proyectoso1.sistema.Nucleo;
import unimet.proyectoso1.sistema.ManejadorHardware;

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
    
    private ManejadorHardware manejadorHardware;

    public VentanaPrincipal() {
        this.manejadorHardware = new ManejadorHardware(this);
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
        
        JButton btnTareaIndividual = new JButton("➕ AÑADIR URGENTE");
        btnTareaIndividual.setBackground(new Color(255, 140, 0)); 
        btnTareaIndividual.setForeground(Color.WHITE);

        JButton btnEmergencia = new JButton("EMERGENCY INTERRUPTION");
        btnEmergencia.setBackground(new Color(200, 0, 0));
        btnEmergencia.setForeground(Color.WHITE);
        btnEmergencia.setFocusPainted(false);
        btnEmergencia.setFont(new Font("Arial", Font.BOLD, 12));

        String[] algos = {"FCFS", "Prioridad", "Round Robin", "SRT", "EDF"};
        comboAlgoritmo = new JComboBox<>(algos);
        
        sliderVelocidad = new JSlider(100, 2000, 1000);
        sliderVelocidad.setOpaque(false);

        pnlControles.add(btnAleatorios);
        pnlControles.add(btnTareaIndividual); 
        pnlControles.add(btnEmergencia);      
        pnlControles.add(crearLabelSimple("Algoritmo:"));
        pnlControles.add(comboAlgoritmo);
        pnlControles.add(crearLabelSimple("Velocidad (ms):"));
        pnlControles.add(sliderVelocidad);

        lblReloj = new JLabel("MISSION CLOCK: Cycle 0000");
        lblReloj.setFont(new Font("Monospaced", Font.BOLD, 24));
        lblReloj.setForeground(COLOR_CIAN);

        pnlNorte.add(pnlControles, BorderLayout.WEST);
        pnlNorte.add(lblReloj, BorderLayout.EAST);

        JPanel pnlCentro = new JPanel(new GridLayout(2, 3, 20, 20));
        pnlCentro.setOpaque(false);
        pnlCentro.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        modListos = new DefaultTableModel(new String[]{"Process [ID]", "Priority"}, 0);
        modNuevos = new DefaultTableModel(new String[]{"Process [ID]", "Priority"}, 0);
        modBloqueados = new DefaultTableModel(new String[]{"Process [ID]", "Priority"}, 0);
        modSuspBloq = new DefaultTableModel(new String[]{"Process [ID]", "Priority"}, 0);

        pnlCentro.add(crearPanelTabla("READY QUEUE (RAM)", modListos));
        pnlCentro.add(crearPanelCPU());
        pnlCentro.add(crearPanelTabla("BLOCKED QUEUE (I/O)", modBloqueados));
        pnlCentro.add(crearPanelTabla("READY-SUSPENDED (DISK)", modNuevos));
        pnlCentro.add(crearPanelLog()); 
        pnlCentro.add(crearPanelTabla("BLOCKED-SUSPENDED", modSuspBloq));

        add(pnlNorte, BorderLayout.NORTH);
        add(pnlCentro, BorderLayout.CENTER);

        btnAleatorios.addActionListener(e -> generarProcesosLote(20));
        btnTareaIndividual.addActionListener(e -> inyectarTareaUrgente());
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
        lblNombre = new JLabel("CPU IDLE", SwingConstants.CENTER);
        lblNombre.setForeground(Color.WHITE);
        barraProgresoCPU = new JProgressBar(0, 100);
        barraProgresoCPU.setStringPainted(true);
        lblDeadline = new JLabel("Deadline: --", SwingConstants.CENTER);
        lblDeadline.setForeground(Color.YELLOW);
        p.add(titulo); p.add(lblNombre); p.add(barraProgresoCPU); p.add(lblDeadline);
        return p;
    }

    private JPanel crearPanelLog() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_CIAN), "LOG", 0,0,null, COLOR_CIAN));
        areaLog = new JTextArea();
        areaLog.setBackground(Color.BLACK);
        areaLog.setForeground(new Color(0, 255, 100));
        areaLog.setEditable(false);
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
                    int pc = actual.getProgramCounter();
                    int total = actual.getInstruccionesTotales();
                    int porcentaje = (total > 0) ? (pc * 100) / total : 0;
                    barraProgresoCPU.setValue(porcentaje);
                } else {
                    lblNombre.setText("SYSTEM IDLE");
                    barraProgresoCPU.setValue(0);
                }
                actualizarTablaManual(modListos, Nucleo.colaListos);
                actualizarTablaManual(modNuevos, Nucleo.colaNuevos); 
            } catch (Exception e) { e.printStackTrace(); } 
            finally { Nucleo.mutex.release(); }
        });
    }

    private void actualizarTablaManual(DefaultTableModel modelo, Cola<PCB> cola) {
        modelo.setRowCount(0); 
        for (int i = 0; i < cola.getTamano(); i++) {
            PCB p = cola.obtenerPorIndice(i); 
            if (p != null) {
                modelo.addRow(new Object[]{p.getNombre() + " [" + p.getId() + "]", p.getPrioridad()});
            }
        }
    }

    public void generarProcesosLote(int cantidad) {
        try {
            Nucleo.mutex.acquire();
            for (int i = 0; i < cantidad; i++) {
                inyectarUnProceso("Tsk_");
            }
            log("SISTEMA: Inyectado lote de " + cantidad + " misiones.");
        } catch (Exception e) { e.printStackTrace(); }
        finally { Nucleo.mutex.release(); refrescarTodo(); }
    }

    private void inyectarTareaUrgente() {
    try {
        Nucleo.mutex.acquire();
        
        int id = (int)(Math.random() * 90) + 900;
        int inst = 8; 
        int prio = 5; 
        int dead = 15; 
        
        PCB urgente = new PCB(id, "URGENTE_" + id, prio, inst, dead, 0);
        

        Nucleo.colaNuevos.encolarAlInicio(urgente);
        
        log("CRÍTICO: Tarea de emergencia " + id + " inyectada al INICIO de la cola.");
        
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        Nucleo.mutex.release();
        refrescarTodo();
    }
}

    private void inyectarUnProceso(String prefijo) {
        int id = (int)(Math.random() * 900) + 100;
        int inst = 10 + (int)(Math.random() * 15);
        int prio = 1 + (int)(Math.random() * 5);
        int dead = 30 + (int)(Math.random() * 50);
        PCB p = new PCB(id, prefijo + id, prio, inst, dead, 0);
        Nucleo.colaNuevos.encolar(p);
    }

    private void activarEmergencia() {
        manejadorHardware.activarInterrupcion();
    }

    public void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            areaLog.append("> " + msg + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }

    public JComboBox<String> getComboAlgoritmo() { return comboAlgoritmo; }
    public JSlider getSliderVelocidad() { return sliderVelocidad; }
}