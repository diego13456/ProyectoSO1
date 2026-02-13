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
                // Sincronización de velocidad con el Slider de la GUI
                long delay = gui.getSliderVelocidad().getValue();
                Thread.sleep(delay);

                // Sección Crítica: Bloqueamos el Mutex para que nadie toque las colas mientras planificamos
                Nucleo.mutex.acquire(); 
                try {
                    Nucleo.relojDelSistema++;

                    if (Nucleo.bajoInterrupcion) {
                        gestionarCicloISR();
                    } else {
                        // 1. Verificar si algún proceso en cualquier cola expiró (Tiempo Real)
                        Planificador.verificarDeadlines();
                        
                        // 2. Gestionar E/S: Mover de CPU a Bloqueado o despertar bloqueados
                        Planificador.gestionarBloqueados(); 
                        
                        // 3. Planificador de Largo Plazo: Admitir procesos nuevos al sistema (RAM o Disco)
                        Planificador.planificarLargoPlazo(); 
                        
                        // 4. Planificador de Mediano Plazo: Swapping (Mover entre RAM y Disco por espacio/prioridad)
                        Planificador.planificarMedianoPlazo(); 
                        
                        // 5. Planificador de Corto Plazo: Elegir quién de la RAM usa la CPU
                        String algoritmoSeleccionado = (String) gui.getComboAlgoritmo().getSelectedItem();
                        Planificador.planificarCortoPlazo(algoritmoSeleccionado);

                        // 6. Ejecutar la instrucción del proceso que quedó en CPU
                        if (Nucleo.procesoEnEjecucion != null) {
                            ejecutarCicloCPU(Nucleo.procesoEnEjecucion);
                        }
                    }

                } finally {
                    // Liberamos el Mutex para que la GUI pueda leer los datos
                    Nucleo.mutex.release();
                }

                // Actualizar la interfaz gráfica con los nuevos estados de las colas
                gui.refrescarTodo();

            } catch (InterruptedException e) {
                enEjecucion = false;
                Thread.currentThread().interrupt();
            }
        }
    }

    private void gestionarCicloISR() {
        Nucleo.ciclosRestantesISR--;
        gui.log("SISTEMA OCUPADO: Atendiendo interrupción de hardware...");
        
        if (Nucleo.ciclosRestantesISR <= 0) {
            Nucleo.bajoInterrupcion = false;
            gui.log("ISR Finalizada: Retornando control al planificador.");
        }
    }

    private void ejecutarCicloCPU(PCB p) {
        // Ejecución de la instrucción actual
        p.incrementarPC(); 
        
        // Aumentar el contador de uso de CPU (Quantum para Round Robin)
        Nucleo.contadorQuantum++;
        
        // Verificar si el proceso terminó su tarea
        if (p.getProgramCounter() >= p.getInstruccionesTotales()) {
            p.setEstado(EstadoProceso.TERMINADO);
            Nucleo.colaTerminados.encolar(p);
            
            Nucleo.misionesExitosas++; 
            
            gui.log("Misión Completada: " + p.getNombre() + " en ciclo " + Nucleo.relojDelSistema);
            
            // Liberamos la CPU y reseteamos el contador de tiempo para el siguiente proceso
            Nucleo.procesoEnEjecucion = null;
            Nucleo.contadorQuantum = 0;
        }
    }
}