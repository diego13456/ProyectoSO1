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
                long delay = gui.getSliderVelocidad().getValue();
                Thread.sleep(delay);

                Nucleo.mutex.acquire(); 
                try {
                    Nucleo.relojDelSistema++;

                    if (Nucleo.bajoInterrupcion) {
                        gestionarCicloISR();
                    } else {
                        Planificador.verificarDeadlines();
                        
                        Planificador.contabilizarEspera();

                        Planificador.gestionarBloqueados(); 
                        Planificador.planificarLargoPlazo(); 
                        Planificador.planificarMedianoPlazo(); 
                        
                        String algoritmoSeleccionado = (String) gui.getComboAlgoritmo().getSelectedItem();
                        Planificador.planificarCortoPlazo(algoritmoSeleccionado);

                        if (Nucleo.procesoEnEjecucion != null) {
                            Nucleo.ciclosCPUOcupada++;
                            ejecutarCicloCPU(Nucleo.procesoEnEjecucion);
                        }
                    }

                } finally {
                    Nucleo.mutex.release();
                }

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
        p.incrementarPC(); 
        Nucleo.contadorQuantum++;
        
        if (p.getProgramCounter() >= p.getInstruccionesTotales()) {
            p.setEstado(EstadoProceso.TERMINADO);
            Nucleo.colaTerminados.encolar(p);
            
            Nucleo.misionesExitosas++; 
            Nucleo.totalProcesosFinalizados++;
            
            Nucleo.sumatoriaTiempoEspera += p.getTiempoEspera();
            
            gui.log("Misión Completada: " + p.getNombre() + " | Espera total: " + p.getTiempoEspera());
            
            Nucleo.procesoEnEjecucion = null;
            Nucleo.contadorQuantum = 0;
        }
    }
}