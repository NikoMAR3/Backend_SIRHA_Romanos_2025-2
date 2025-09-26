package edu.dosw.sirha.model;

public enum PetitionType {
    ADD("Agregar materia"),
    REMOVE("Retirar materia"),
    CHANGE("Cambio de grupo");

    private final String description;

    PetitionType(String description) {
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
