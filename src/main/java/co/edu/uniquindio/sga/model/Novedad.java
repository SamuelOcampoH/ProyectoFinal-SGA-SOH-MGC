package co.edu.uniquindio.sga.model;

import java.time.LocalDateTime;

public record Novedad(
        String codigoApartamento,
        LocalDateTime fechaHora,
        String descripcion,
        String reportadoPor
) {}