/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso1.sistema;

public class Reloj extends Thread {
    private final int tiempoPulsoMs; 

    public Reloj(int tiempoPulsoMs) {
        this.tiempoPulsoMs = tiempoPulsoMs;
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

                Nucleo.mutex.release(); 

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}