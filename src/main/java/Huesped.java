package co.edu.uniquindio.sga.model;

import java.time.LocalDate;

public class Huesped extends Persona {
    private String rutaFotografia;

    public Huesped(String nombre, String documento, LocalDate fechaNacimiento,
                   String telefono, String correo, String password, String rutaFotografia) {
        super(nombre, documento, fechaNacimiento, telefono, correo, password);
        this.rutaFotografia = rutaFotografia;
    }

    @Override
    public String getRol() { return "HUESPED"; }

    public String getRutaFotografia() { return rutaFotografia; }
    public void setRutaFotografia(String rutaFotografia) { this.rutaFotografia = rutaFotografia; }
}