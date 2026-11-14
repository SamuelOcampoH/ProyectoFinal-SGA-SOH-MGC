package co.edu.uniquindio.sga.model;

import java.time.LocalDate;

public record PeriodoTemporada(
        Temporada temporada,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {
    public boolean aplicaA(LocalDate fecha) {
        return !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
    }
}