package co.edu.uniquindio.sga.model;

import java.time.LocalDate;

public record BloqueoApartamento(
        String codigoApartamento,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String motivo
) {
    public boolean cubreFecha(LocalDate fecha) {
        return !fecha.isBefore(fechaInicio) && fecha.isBefore(fechaFin);
    }
}