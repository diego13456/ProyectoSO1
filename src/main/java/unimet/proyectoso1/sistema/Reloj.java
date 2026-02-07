package unimet.proyectoso1.sistema;

import javax.swing.SwingUtilities;
import unimet.proyectoso1.gui.VentanaPrincipal;

public class Reloj extends Thread {
    private int tiempoPulsoMs; 
    private final VentanaPrincipal ventana; 

    public Reloj(int tiempoPulsoMs, VentanaPrincipal ventana) {
        this.tiempoPulsoMs = tiempoPulsoMs;
        this.ventana = ventana;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(tiempoPulsoMs);

                Nucleo.mutex.acquire(); 

                Nucleo.relojDelSistema++;

                Planificador.planificarLargoPlazo(); 
                Planificador.planificarCortoPlazo(); 

                CPU.ejecutarCiclo();

                SwingUtilities.invokeLater(() -> {
                    ventana.refrescarTodo();
                });

                Nucleo.mutex.release(); 

            } catch (InterruptedException e) {
                System.err.println("Reloj interrumpido: " + e.getMessage());
                break; 
            }
        }
    }

    public void setVelocidad(int nuevaVelocidadMs) {
        this.tiempoPulsoMs = nuevaVelocidadMs;
    }
}