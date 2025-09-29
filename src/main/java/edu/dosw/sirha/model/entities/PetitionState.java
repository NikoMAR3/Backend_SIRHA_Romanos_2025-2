package edu.dosw.sirha.model.entities;

public enum PetitionState {
    PENDING("Pendiente"),
    IN_PROCESS("En revisión"),
    APPROVED("Aprobada"),
    REPROVED("Rechazada");

    private final String description;

    PetitionState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
