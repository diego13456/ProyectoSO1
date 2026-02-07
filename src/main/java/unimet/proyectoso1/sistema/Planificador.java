/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso1.sistema;


import unimet.proyectoso1.modelo.EstadoProceso;
import unimet.proyectoso1.modelo.PCB;

public class Planificador {

    public static void planificarLargoPlazo() {
        while (!Nucleo.colaNuevos.estaVacia() && Nucleo.colaListos.getTamano() < Nucleo.LIMITE_MEMORIA_RAM) {
            PCB proceso = Nucleo.colaNuevos.desencolar();
            proceso.setEstado(EstadoProceso.LISTO);
            Nucleo.colaListos.encolar(proceso);
            System.out.println("[PLANIFICADOR] Movido a RAM (Listo): " + proceso.getNombre());
        }
    }

    public static void planificarCortoPlazo() {
        if (Nucleo.procesoEnEjecucion == null && !Nucleo.colaListos.estaVacia()) {
            PCB siguiente = Nucleo.colaListos.desencolar();
            siguiente.setEstado(EstadoProceso.EJECUCION);
            Nucleo.procesoEnEjecucion = siguiente;
            System.out.println("[DISPATCHER] Cambio de Contexto -> CPU asignada a: " + siguiente.getNombre());
        }
    }
}