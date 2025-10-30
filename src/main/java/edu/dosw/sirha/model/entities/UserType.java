package edu.dosw.sirha.model.entities;

/**
 * Enum representing the types of users in the system.
 */
public enum UserType {
    STUDENT("Estudiante"),
    DEAN("Decano"),
    PROFESSOR("Profesor"),
    ACADEMIC_VICEPRESIDENT("Vicepresidente Académico");

    private final String description;

    /**
     * Constructor for UserType enum.
     * @param description The description of the user type.
     */
    UserType(String description) {
        this.description = description;
    }

    /**
     * Gets the description of the user type.
     * @return The description of the user type.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the string representation of the user type.
     * @return The description of the user type.
     */
    @Override
    public String toString() {
        return description;
    }
}
