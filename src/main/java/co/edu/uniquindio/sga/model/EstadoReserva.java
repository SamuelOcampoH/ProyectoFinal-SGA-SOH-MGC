package co.edu.uniquindio.sga.model;

public enum EstadoReserva {
    PENDIENTE,
    CONFIRMADA,
    EN_CURSO,
    FINALIZADA,
    CANCELADA,
    NO_SHOW;

    public boolean esActiva() {
        return this == PENDIENTE || this == CONFIRMADA || this == EN_CURSO;
    }

    public boolean puedeTransicionarA(EstadoReserva destino) {
        return switch (this) {
            case PENDIENTE -> destino == CONFIRMADA || destino == CANCELADA;
            case CONFIRMADA -> destino == EN_CURSO || destino == CANCELADA || destino == NO_SHOW;
            case EN_CURSO -> destino == FINALIZADA;
            case FINALIZADA, CANCELADA, NO_SHOW -> false;
        };
    }
}