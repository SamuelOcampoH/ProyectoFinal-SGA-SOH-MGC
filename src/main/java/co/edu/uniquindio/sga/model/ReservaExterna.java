package co.edu.uniquindio.sga.model;

import java.time.LocalDate;
import java.util.List;

public class ReservaExterna extends Reserva {
    private final String codigoExterno;
    private final long comisionPlataforma;

    public ReservaExterna(String codigo, String codigoExterno, Huesped titular, Apartamento apartamento,
                          LocalDate fechaEntrada, LocalDate fechaSalida, List<Ocupante> ocupantes,
                          long valorEstancia, PoliticaCancelacion politica) {
        super(codigo, titular, apartamento, fechaEntrada, fechaSalida, ocupantes, valorEstancia, politica);
        if (codigoExterno == null || codigoExterno.isBlank()) {
            throw new IllegalArgumentException("Código externo obligatorio.");
        }
        this.codigoExterno = codigoExterno;
        this.comisionPlataforma = Math.round(valorEstancia * 0.15);
    }

    @Override
    public String getCanal() { return "EXTERNA"; }

    @Override
    public boolean cumpleRequisitoConfirmacion() { return true; }

    public String getCodigoExterno() { return codigoExterno; }
    public long getComisionPlataforma() { return comisionPlataforma; }
}