package co.edu.uniquindio.sga.model;

public interface Autenticable {
    String getUsuario();
    boolean validarCredenciales(String usuario, String password);
    String getRol();
}