
package unimet.proyectoso1.modelo;
public class PCB {
    private int id;
    private String nombre;
    private EstadoProceso estado;

    private int programCounter; 
    private int memoryAddressRegister; 

    private int prioridad;
    private int instruccionesTotales; 
    private int deadline; 
    private int cicloExcepcion; 


    public PCB(int id, String nombre, int prioridad, int instruccionesTotales, int deadline, int cicloExcepcion) {
        this.id = id;
        this.nombre = nombre;
        this.prioridad = prioridad;
        this.instruccionesTotales = instruccionesTotales;
        this.deadline = deadline;
        this.cicloExcepcion = cicloExcepcion;
        
        // Inicializaciones por defecto requeridas
        this.estado = EstadoProceso.NUEVO;
        this.programCounter = 0;
        this.memoryAddressRegister = 0;
    }


    public int getId() { return id; }
    
    public String getNombre() { return nombre; }
    
    public EstadoProceso getEstado() { return estado; }
    public void setEstado(EstadoProceso estado) { this.estado = estado; }

    public int getProgramCounter() { return programCounter; }
    public void setProgramCounter(int programCounter) { this.programCounter = programCounter; }
    
    public void incrementarPC() { this.programCounter++; }

    public int getMemoryAddressRegister() { return memoryAddressRegister; }
    public void setMemoryAddressRegister(int mar) { this.memoryAddressRegister = mar; }

    public int getPrioridad() { return prioridad; }
    public void setPrioridad(int prioridad) { this.prioridad = prioridad; }

    public int getInstruccionesTotales() { return instruccionesTotales; }

    public int getDeadline() { return deadline; }

    public int getCicloExcepcion() { return cicloExcepcion; }

    @Override
    public String toString() {
        return "PCB{" + "id=" + id + ", nombre=" + nombre + ", estado=" + estado + 
               ", PC=" + programCounter + ", Prio=" + prioridad + '}';
    }
}