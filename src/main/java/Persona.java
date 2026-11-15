package co.edu.uniquindio.sga.model;

import java.time.LocalDate;
import java.time.Period;

public abstract class Persona implements Autenticable {
    private String nombre;
    private final String documento;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String correo;
    private String password;

    protected Persona(String nombre, String documento, LocalDate fechaNacimiento,
                      String telefono, String correo, String password) {
        if (nombre == null || nombre.isBlank() || documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("Nombre y documento son obligatorios.");
        }
        this.nombre = nombre;
        this.documento = documento;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.correo = correo;
        this.password = password;
    }

    public int calcularEdadActual() {
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    @Override
    public String getUsuario() { return documento; }

    @Override
    public boolean validarCredenciales(String usuario, String pass) {
        return this.documento.equals(usuario) && this.password.equals(pass);
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDocumento() { return documento; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return nombre + " (" + documento + ")";
    }
}
