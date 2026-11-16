package co.edu.uniquindio.sga.model;

import java.time.LocalDate;

public abstract class Empleado extends Persona {
    private boolean activo;

    protected Empleado(String nombre, String documento, LocalDate fechaNacimiento,
                       String telefono, String correo, String password) {
        super(nombre, documento, fechaNacimiento, telefono, correo, password);
        this.activo = true;
    }

    @Override
    public boolean validarCredenciales(String usuario, String pass) {
        return activo && super.validarCredenciales(usuario, pass);
    }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}