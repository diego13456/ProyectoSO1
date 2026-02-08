package unimet.proyectoso1.util;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.BufferedReader;
import java.io.FileReader;
import unimet.proyectoso1.estructuras.ListaEnlazada;
import unimet.proyectoso1.modelo.PCB;
import unimet.proyectoso1.gui.VentanaPrincipal;

public class LectorArchivos {
    private Gson gson = new Gson();
    private VentanaPrincipal gui; // Referencia a la GUI para el log

    public LectorArchivos(VentanaPrincipal gui) {
        this.gui = gui;
    }

    public void cargarDesdeJSON(String ruta, ListaEnlazada listaDestino) {
        try (JsonReader reader = new JsonReader(new FileReader(ruta))) {
            reader.beginArray();
            while (reader.hasNext()) {
                PCB proceso = gson.fromJson(reader, PCB.class);
                validarYAgregar(proceso, listaDestino);
            }
            reader.endArray();
        } catch (Exception e) {
            gui.log("Error JSON: Archivo corrupto o formato inválido.");
        }
    }

    public void cargarDesdeCSV(String ruta, ListaEnlazada listaDestino) {
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            int contadorId = 1; // Generador de ID simple para el constructor
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                
                // Ajustado al constructor: int id, String nombre, int prioridad, int totalInst, int deadline, int cicloExc
                PCB proceso = new PCB(
                    contadorId++, 
                    datos[0].trim(),                          // Nombre
                    Integer.parseInt(datos[4].trim()),        // Prioridad
                    Integer.parseInt(datos[1].trim()),        // Instrucciones Totales
                    Integer.parseInt(datos[3].trim()),        // Deadline
                    Integer.parseInt(datos[2].trim())         // Ciclo Excepción (antes era boolean, ahora int)
                );
                
                validarYAgregar(proceso, listaDestino);
            }
        } catch (Exception e) {
            gui.log("Error CSV: Datos incompatibles con el PCB del satélite.");
        }
    }

    private void validarYAgregar(PCB p, ListaEnlazada lista) throws Exception {
        // Corregido: Usando getInstruccionesTotales() que es el nombre real en tu PCB
        if (p.getInstruccionesTotales() <= 0) {
            throw new Exception("Instrucciones inválidas");
        }
        lista.insertar(p); 
    }
}