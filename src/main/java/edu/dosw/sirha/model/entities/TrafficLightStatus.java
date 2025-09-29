package edu.dosw.sirha.model.entities;

import lombok.Getter;

/**
 * Enumeration representing the academic traffic light status of a student.
 */
@Getter
public enum TrafficLightStatus {
    RED("Reprobado"),
    BLUE("Matriculado"),
    WHITE("Pendiente"),
    GREEN("Aprobado");

    private final String description;

    /**
     * Basic constructor for the class
     */
    TrafficLightStatus(String description) {
        this.description = description;
    }

    /**
     * Returns the textual description of the status.
     *
     * @return the description associated with this status.
     */
    @Override
    public String toString() {
        return description;
    }
}

