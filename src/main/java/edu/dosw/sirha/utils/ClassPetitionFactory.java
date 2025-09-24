package edu.dosw.sirha.utils;

import edu.dosw.sirha.model.ClassPetition;
import edu.dosw.sirha.model.Student;

/**
 * Factory class to create different types of ClassPetition objects.
 */
public abstract class ClassPetitionFactory {

    /**
     * Creates a ClassPetition object based on the provided parameters.
     * @param subjectCode
     * @param observations
     * @param student
     * @param groupIds
     * @return ClassPetition object
     */
    public abstract ClassPetition createPetition(String subjectCode, String observations,
                                                 Student student, String... groupIds);

    /**
     * Static method to get the appropriate factory based on the petition type.
     * @param type Type of petition ("ADD", "REMOVE", "CHANGE")
     * @return ClassPetitionFactory
     * @throws IllegalArgumentException if the type is not recognized
     */
    public static ClassPetitionFactory getCreator(String type) {
        switch (type.toUpperCase()) {
            case "ADD":
                return new AddPetitionFactory();
            case "REMOVE":
                return new RemovePetitionFactory();
            case "CHANGE":
                return new ChangePetitionFactory();
            default:
                throw new IllegalArgumentException("Tipo de petición no válido: " + type);
        }
    }
}
