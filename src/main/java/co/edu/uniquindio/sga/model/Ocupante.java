package co.edu.uniquindio.sga.model;

import java.time.LocalDate;
import java.time.Period;

public record Ocupante(String nombre, LocalDate fechaNacimiento) {
    public int calcularEdadEn(LocalDate fechaReferencia) {
        return Period.between(fechaNacimiento, fechaReferencia).getYears();
    }

    public boolean pagaTarifa(LocalDate fechaEntrada) {
        return calcularEdadEn(fechaEntrada) >= 6;
    }
}