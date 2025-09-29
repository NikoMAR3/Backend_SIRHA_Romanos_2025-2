package edu.dosw.sirha.model.entities;

public enum PetitionType {
    ADD_SUBJECT("Agregar materia"),
    REMOVE_SUBJECT("Retirar materia"),
    CHANGE_GROUP("Cambio de grupo");

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
