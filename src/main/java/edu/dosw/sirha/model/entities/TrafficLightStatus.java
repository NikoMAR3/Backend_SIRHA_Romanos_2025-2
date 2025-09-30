package edu.dosw.sirha.model.entities;

import lombok.Getter;

/**
 * Enum that represents the status of a traffic light.
 */
@Getter
public enum TrafficLightStatus {
    RED("Reprobado"),
    BLUE("Matriculado"),
    WHITE("Pendiente"),
    GREEN("Aprobado");

    private final String description;

    /** Constructor for TrafficLightStatus enum.
     * @param description The description of the traffic light status.
     */
    TrafficLightStatus(String description) {
        this.description = description;
    }

    /**
     * Returns the description of the traffic light status.
     * @return The description of the traffic light status.
     */
    @Override
    public String toString() {
        return description;
    }
}

