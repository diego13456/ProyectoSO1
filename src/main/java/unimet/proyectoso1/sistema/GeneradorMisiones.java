package unimet.proyectoso1.sistema;

import unimet.proyectoso1.gui.VentanaPrincipal;
import unimet.proyectoso1.modelo.PCB;

public class GeneradorMisiones extends Thread {
    private VentanaPrincipal gui;

    public GeneradorMisiones(VentanaPrincipal gui) {
        this.gui = gui;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Simula que llega una misión externa cada 5 a 10 segundos
                Thread.sleep((long) (5000 + Math.random() * 5000));

                // Acceso seguro a las colas usando el Semáforo
                Nucleo.mutex.acquire();
                try {
                    int id = (int)(Math.random() * 900) + 1000; // IDs de misiones externas (rango 1000+)
                    int inst = 10 + (int)(Math.random() * 10);
                    int prio = 1 + (int)(Math.random() * 3); // Suelen ser prioridad alta
                    int dead = 200 + (int)(Math.random() * 200);

                    PCB p = new PCB(id, "EXTERNO_" + id, prio, inst, dead, 0);
                    Nucleo.colaNuevos.encolar(p);

                    gui.log("--- EVENTO EXTERNO: Sensor detectó misión crítica [" + id + "] ---");
                } finally {
                    Nucleo.mutex.release();
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}