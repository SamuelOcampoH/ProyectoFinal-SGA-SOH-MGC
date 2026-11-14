package co.edu.uniquindio.sga.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ConflictoCanal(
        String codigoExterno,
        String codigoApartamento,
        LocalDate entrada,
        LocalDate salida,
        LocalDateTime fechaIntento,
        String motivo
) {}