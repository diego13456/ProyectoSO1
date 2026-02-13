package unimet.proyectoso1.gui;

import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import unimet.proyectoso1.estructuras.Cola;
import unimet.proyectoso1.modelo.PCB;
import unimet.proyectoso1.sistema.Nucleo;
import unimet.proyectoso1.sistema.ManejadorHardware;

public class VentanaPrincipal extends JFrame {

    private final Color COLOR_FONDO = new Color(10, 10, 25);
    private final Color COLOR_TABLA = new Color(20, 20, 45);
    private final Color COLOR_NEON_PURPURA = new Color(150, 0, 255);
    private final Color COLOR_CIAN = new Color(0, 255, 255);
    private final Color COLOR_TEXTO = new Color(220, 220, 220);

    private DefaultTableModel modListos, modBloqueados, modNuevos, modSuspBloq;
    private JLabel lblNombre, lblDeadline, lblReloj;
    private JLabel lblExito, lblThroughput, lblEspera;
    private XYSeries serieCPU; 
    private ChartPanel panelGrafica;

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
        setSize(1600, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout(10, 10));
    }

    private void inicializarComponentes() {
        JPanel pnlNorte = new JPanel(new BorderLayout());
        pnlNorte.setOpaque(false);
        pnlNorte.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));

        JPanel pnlControles = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pnlControles.setOpaque(false);

        JButton btnAleatorios = crearBotonEstilizado("Generar 20 Procesos", new Color(50, 50, 100));
        JButton btnTareaUrgente = crearBotonEstilizado("➕ AÑADIR URGENTE", new Color(255, 140, 0));
        JButton btnEmergencia = crearBotonEstilizado("INTERRUPCIÓN", new Color(200, 0, 0));

        comboAlgoritmo = new JComboBox<>(new String[]{"FCFS", "Prioridad", "Round Robin", "SRT", "EDF"});
        comboAlgoritmo.setBackground(COLOR_TABLA);
        comboAlgoritmo.setForeground(Color.WHITE);
        
        sliderVelocidad = new JSlider(100, 2000, 1000);
        sliderVelocidad.setOpaque(false);

        pnlControles.add(btnAleatorios);
        pnlControles.add(btnTareaUrgente); 
        pnlControles.add(btnEmergencia);      
        pnlControles.add(crearLabelSimple(" Algoritmo: "));
        pnlControles.add(comboAlgoritmo);
        pnlControles.add(crearLabelSimple(" Velocidad: "));
        pnlControles.add(sliderVelocidad);

        lblReloj = new JLabel("MISSION CLOCK: Cycle 0000");
        lblReloj.setFont(new Font("Monospaced", Font.BOLD, 22));
        lblReloj.setForeground(COLOR_CIAN);

        pnlNorte.add(pnlControles, BorderLayout.WEST);
        pnlNorte.add(lblReloj, BorderLayout.EAST);

        JPanel pnlCentro = new JPanel(new GridLayout(2, 3, 15, 15));
        pnlCentro.setOpaque(false);
        pnlCentro.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));

        modListos = new DefaultTableModel(new String[]{"ID", "Name", "Prio"}, 0);
        modNuevos = new DefaultTableModel(new String[]{"ID", "Name", "Prio"}, 0);
        modBloqueados = new DefaultTableModel(new String[]{"ID", "Name", "Prio"}, 0);
        modSuspBloq = new DefaultTableModel(new String[]{"ID", "Name", "Prio"}, 0);

        pnlCentro.add(crearPanelTabla("READY QUEUE (RAM)", modListos));
        pnlCentro.add(crearPanelCPU());
        pnlCentro.add(crearPanelTabla("BLOCKED QUEUE (I/O)", modBloqueados));
        pnlCentro.add(crearPanelTabla("READY-SUSPENDED (DISK)", modNuevos));
        pnlCentro.add(crearPanelLog()); 
        pnlCentro.add(crearPanelTabla("BLOCKED-SUSPENDED", modSuspBloq));

        JPanel pnlDerecho = crearPanelMetricasYRendimiento();

        add(pnlNorte, BorderLayout.NORTH);
        add(pnlCentro, BorderLayout.CENTER);
        add(pnlDerecho, BorderLayout.EAST);

        btnAleatorios.addActionListener(e -> generarProcesosLote(20));
        btnTareaUrgente.addActionListener(e -> inyectarTareaUrgente());
        btnEmergencia.addActionListener(e -> activarEmergencia());
    }

    private JPanel crearPanelMetricasYRendimiento() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setPreferredSize(new Dimension(380, 0));
        p.setBackground(COLOR_FONDO);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 20));

        serieCPU = new XYSeries("CPU Load");
        XYSeriesCollection dataset = new XYSeriesCollection(serieCPU);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Processor Utilization", "Cycle", "% Usage",
                dataset, PlotOrientation.VERTICAL, false, true, false);
        
        chart.setBackgroundPaint(COLOR_FONDO);
        chart.getTitle().setPaint(Color.WHITE);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(COLOR_TABLA);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, COLOR_CIAN);
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(renderer);

        panelGrafica = new ChartPanel(chart);
        panelGrafica.setOpaque(false);
        panelGrafica.setBackground(COLOR_FONDO);

        JPanel pnlTexto = new JPanel(new GridLayout(3, 1, 5, 5));
        pnlTexto.setOpaque(false);
        pnlTexto.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_CIAN), "PERFORMANCE", 0, 0, null, COLOR_CIAN));

        lblExito = crearLabelMetrica("Success Rate: 0.00%");
        lblThroughput = crearLabelMetrica("Throughput: 0.000 t/c");
        lblEspera = crearLabelMetrica("Avg Wait: 0.00 c");

        pnlTexto.add(lblExito); pnlTexto.add(lblThroughput); pnlTexto.add(lblEspera);

        p.add(panelGrafica, BorderLayout.CENTER);
        p.add(pnlTexto, BorderLayout.SOUTH);
        return p;
    }

    private JPanel crearPanelTabla(String titulo, DefaultTableModel modelo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_NEON_PURPURA, 2), titulo, 0, 0, null, COLOR_NEON_PURPURA));
        
        JTable t = new JTable(modelo);
        t.setBackground(COLOR_TABLA);
        t.setForeground(Color.WHITE);
        t.setGridColor(new Color(60, 60, 90));
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(COLOR_NEON_PURPURA);
        
        JTableHeader header = t.getTableHeader();
        header.setBackground(new Color(30, 30, 70));
        header.setForeground(COLOR_CIAN);
        header.setFont(new Font("Arial", Font.BOLD, 12));

        JScrollPane sp = new JScrollPane(t);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false); 
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(COLOR_FONDO);
        
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearPanelCPU() {
        JPanel p = new JPanel(new GridLayout(4, 1, 10, 10));
        p.setBackground(new Color(15, 15, 40));
        p.setBorder(BorderFactory.createLineBorder(COLOR_CIAN, 3));
        
        JLabel t = new JLabel("RUNNING PROCESS (CPU)", SwingConstants.CENTER);
        t.setForeground(COLOR_CIAN); t.setFont(new Font("Arial", Font.BOLD, 13));
        
        lblNombre = new JLabel("CPU IDLE", SwingConstants.CENTER);
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setFont(new Font("Arial", Font.BOLD, 16));
        
        barraProgresoCPU = new JProgressBar(0, 100);
        barraProgresoCPU.setStringPainted(true);
        barraProgresoCPU.setBackground(new Color(30, 30, 50));
        barraProgresoCPU.setForeground(COLOR_CIAN);
        barraProgresoCPU.setBorder(BorderFactory.createLineBorder(COLOR_CIAN));
        
        lblDeadline = new JLabel("Deadline: --", SwingConstants.CENTER);
        lblDeadline.setForeground(Color.YELLOW);
        
        p.add(t); p.add(lblNombre); p.add(barraProgresoCPU); p.add(lblDeadline);
        return p;
    }

    private JPanel crearPanelLog() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_CIAN), "SYSTEM LOG", 0,0,null, COLOR_CIAN));
        
        areaLog = new JTextArea();
        areaLog.setBackground(new Color(5, 5, 15));
        areaLog.setForeground(new Color(0, 255, 100));
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaLog.setEditable(false);
        
        JScrollPane sp = new JScrollPane(areaLog);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JButton crearBotonEstilizado(String t, Color c) {
        JButton b = new JButton(t);
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createRaisedBevelBorder());
        return b;
    }

    private JLabel crearLabelMetrica(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(new Color(0, 255, 100));
        l.setFont(new Font("Monospaced", Font.BOLD, 15));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private JLabel crearLabelSimple(String t) {
        JLabel l = new JLabel(t); l.setForeground(Color.WHITE); return l;
    }

    public void refrescarTodo() {
        SwingUtilities.invokeLater(() -> {
            try {
                Nucleo.mutex.acquire();
                lblReloj.setText(String.format("MISSION CLOCK: Cycle %04d", Nucleo.relojDelSistema));
                
                PCB actual = Nucleo.procesoEnEjecucion;
                if (actual != null) {
                    lblNombre.setText(actual.getNombre() + " [" + actual.getId() + "]");
                    lblDeadline.setText("Deadline: " + actual.getDeadline() + " cycles");
                    int porc = (actual.getInstruccionesTotales() > 0) ? (actual.getProgramCounter() * 100 / actual.getInstruccionesTotales()) : 0;
                    barraProgresoCPU.setValue(porc);
                } else {
                    lblNombre.setText("SYSTEM IDLE");
                    barraProgresoCPU.setValue(0);
                    lblDeadline.setText("Deadline: --");
                }

                lblExito.setText(String.format("Success Rate: %.2f%%", Nucleo.getTasaExito()));
                double throughput = (Nucleo.relojDelSistema == 0) ? 0 : (double) Nucleo.totalProcesosFinalizados / Nucleo.relojDelSistema;
                lblThroughput.setText(String.format("Throughput: %.3f t/c", throughput));
                lblEspera.setText(String.format("Avg Wait: %.2f c", Nucleo.getPromedioEspera()));

                serieCPU.add(Nucleo.relojDelSistema, Nucleo.getUtilizacionCPU());
                if (serieCPU.getItemCount() > 50) serieCPU.remove(0);

                actualizarTablaManual(modListos, Nucleo.colaListos);      
                actualizarTablaManual(modBloqueados, Nucleo.colaBloqueados); 
                actualizarTablaManual(modNuevos, Nucleo.colaReadySuspended); 
                actualizarTablaManual(modSuspBloq, Nucleo.colaBlockedSuspended); 

            } catch (Exception e) {} 
            finally { Nucleo.mutex.release(); }
        });
    }

    private void actualizarTablaManual(DefaultTableModel modelo, Cola<PCB> cola) {
        modelo.setRowCount(0); 
        for (int i = 0; i < cola.getTamano(); i++) {
            PCB p = cola.obtenerPorIndice(i); 
            if (p != null) modelo.addRow(new Object[]{p.getId(), p.getNombre(), p.getPrioridad()});
        }
    }

    public void generarProcesosLote(int cantidad) {
        try {
            Nucleo.mutex.acquire();
            for (int i = 0; i < cantidad; i++) {
                inyectarUnProceso("Tsk_");
            }
            log("SISTEMA: Inyectado lote de " + cantidad + " misiones.");
        } catch (Exception e) {}
        finally { Nucleo.mutex.release(); refrescarTodo(); }
    }

    private void inyectarTareaUrgente() {
        try {
            Nucleo.mutex.acquire();
            PCB urgente = new PCB((int)(Math.random()*100)+900, "URGENTE", 1, 8, 20, 0);
            Nucleo.colaNuevos.encolarAlInicio(urgente);
            log("CRÍTICO: Tarea de emergencia inyectada.");
        } catch (Exception e) {} 
        finally { Nucleo.mutex.release(); refrescarTodo(); }
    }

    private void inyectarUnProceso(String prefijo) {
        int id = (int)(Math.random() * 900) + 100;
        int inst = 10 + (int)(Math.random() * 10);
        int prio = 1 + (int)(Math.random() * 5);
        int dead = 150 + (int)(Math.random() * 200); 
        PCB p = new PCB(id, prefijo + id, prio, inst, dead, 0);
        Nucleo.colaNuevos.encolar(p);
    }

    private void activarEmergencia() { manejadorHardware.activarInterrupcion(); }

    public void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            areaLog.append("> " + msg + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }

    public JComboBox<String> getComboAlgoritmo() { return comboAlgoritmo; }
    public JSlider getSliderVelocidad() { return sliderVelocidad; }
}