package edu.dosw.sirha.model.entities;

/**
 * Enum representing the types of petitions that can be made.
 */
public enum PetitionType {
    ADD_SUBJECT("Agregar materia"),
    REMOVE_SUBJECT("Retirar materia"),
    CHANGE_GROUP("Cambio de grupo");

    private final String description;

    /**
     * Constructor for PetitionType enum.
     * @param description The description of the petition type.
     */
    PetitionType(String description) {
        this.description = description;
    }

    /**
     * Gets the description of the petition type.
     * @return The description of the petition type.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the string representation of the petition type.
     * @return The description of the petition type.
     */
    @Override
    public String toString() {
        return description;
    }
}
