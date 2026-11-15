package co.edu.uniquindio.sga.model;

import java.time.LocalDate;

public class Recepcionista extends Empleado {
    public Recepcionista(String nombre, String documento, LocalDate fechaNacimiento,
                         String telefono, String correo, String password) {
        super(nombre, documento, fechaNacimiento, telefono, correo, password);
    }

    @Override
    public String getRol() { return "RECEPCIONISTA"; }
}
