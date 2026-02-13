
package unimet.proyectoso1.modelo;

public enum EstadoProceso {
    NUEVO,
    LISTO,
    EJECUCION,
    BLOQUEADO,
    TERMINADO,
    LISTO_SUSPENDIDO,   // En disco, listo para ejecutar
    BLOQUEADO_SUSPENDIDO // En disco, esperando I/O
}
