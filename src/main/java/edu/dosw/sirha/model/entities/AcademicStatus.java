package edu.dosw.sirha.model.entities;

public enum AcademicStatus {
    ACTIVE("Activo"),
    INACTIVE("Inactivo"),
    SUSPENDED("Suspendido"),
    GRADUATED("Graduado");

    private final String description;

    AcademicStatus(String description) {
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
