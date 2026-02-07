
package unimet.proyectoso1.sistema;


import unimet.proyectoso1.modelo.EstadoProceso;

public class CPU {

    public static void ejecutarCiclo() {
        if (Nucleo.procesoEnEjecucion != null) {
            Nucleo.procesoEnEjecucion.incrementarPC(); 
            
            int marActual = Nucleo.procesoEnEjecucion.getMemoryAddressRegister();
            Nucleo.procesoEnEjecucion.setMemoryAddressRegister(marActual + 1);
            
            System.out.println("[CPU] Ejecutando: " + Nucleo.procesoEnEjecucion.getNombre() + 
                               " | PC: " + Nucleo.procesoEnEjecucion.getProgramCounter() + 
                               " | Reloj: " + Nucleo.relojDelSistema);

            if (Nucleo.procesoEnEjecucion.getProgramCounter() >= Nucleo.procesoEnEjecucion.getInstruccionesTotales()) {
                Nucleo.procesoEnEjecucion.setEstado(EstadoProceso.TERMINADO);
                Nucleo.colaTerminados.encolar(Nucleo.procesoEnEjecucion);
                System.out.println("   >>> PROCESO TERMINADO: " + Nucleo.procesoEnEjecucion.getNombre());
                Nucleo.procesoEnEjecucion = null; 
            }
        } else {
            System.out.println("[CPU] IDLE (Esperando procesos...) | Reloj: " + Nucleo.relojDelSistema);
        }
    }
}