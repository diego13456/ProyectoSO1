package unimet.proyectoso1.sistema;

import unimet.proyectoso1.gui.VentanaPrincipal;

public class ManejadorHardware {
    private VentanaPrincipal gui;

    public ManejadorHardware(VentanaPrincipal gui) {
        this.gui = gui;
    }

    public void activarInterrupcion() {
    Nucleo.ciclosRestantesISR = 5; 
    Nucleo.bajoInterrupcion = true;
    gui.log("!!! ALERTA: MICRO-METEORITO !!! Atendiendo ISR...");
    }
}
