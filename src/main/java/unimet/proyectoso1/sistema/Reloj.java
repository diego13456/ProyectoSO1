package unimet.proyectoso1.sistema;

import unimet.proyectoso1.gui.VentanaPrincipal;
import unimet.proyectoso1.modelo.EstadoProceso;
import unimet.proyectoso1.modelo.PCB;

public class Reloj extends Thread {

    private VentanaPrincipal gui;
    private boolean enEjecucion = true;

    public Reloj(VentanaPrincipal gui) {
        this.gui = gui;
    }

    @Override
    public void run() {
        while (enEjecucion) {
            try {
                // 1. Gestionar Velocidad desde el Slider de la GUI
                long delay = gui.getSliderVelocidad().getValue();
                Thread.sleep(delay);

                // 2. Control de Interrupción (ISR)
                // Si hay emergencia, el reloj sigue contando pero el RTOS se detiene
                if (Nucleo.bajoInterrupcion) {
                    continue; 
                }

                Nucleo.mutex.acquire();
                try {
                    // 3. Incrementar el Reloj de Misión
                    Nucleo.relojDelSistema++;

                    // 4. Planificación de Largo Plazo (Cargar procesos a RAM)
                    Planificador.planificarLargoPlazo();

                    // 5. Planificación de Corto Plazo (Dispatcher)
                    // Obtenemos el algoritmo seleccionado en el ComboBox en tiempo real
                    String algoritmoSeleccionado = (String) gui.getComboAlgoritmo().getSelectedItem();
                    Planificador.planificarCortoPlazo(algoritmoSeleccionado);

                    // 6. Ejecución de la instrucción actual en CPU
                    if (Nucleo.procesoEnEjecucion != null) {
                        ejecutarCicloCPU(Nucleo.procesoEnEjecucion);
                    }

                } finally {
                    Nucleo.mutex.release();
                }

                // 7. Actualizar la Interfaz Gráfica
                gui.refrescarTodo();

            } catch (InterruptedException e) {
                enEjecucion = false;
                Thread.currentThread().interrupt();
            }
        }
    }

    private void ejecutarCicloCPU(PCB p) {
        // Incrementar el PC (Program Counter)
        p.incrementarPC();
        
        // Simular ejecución: Si llega al total de instrucciones, termina
        if (p.getProgramCounter() >= p.getInstruccionesTotales()) {
            p.setEstado(EstadoProceso.TERMINADO);
            gui.log("Task Finished: " + p.getNombre() + " [ID: " + p.getId() + "]");
            Nucleo.procesoEnEjecucion = null;
        }
    }
}