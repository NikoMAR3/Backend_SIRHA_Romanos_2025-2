package edu.dosw.sirha.model.entities;

public enum UserType {
    STUDENT("Estudiante"),
    DEAN("Decano"),
    PROFESSOR("Profesor"),
    ACADEMIC_VICEPRESIDENT("Vicepresidente Académico");

    private final String description;

    UserType(String description) {
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
