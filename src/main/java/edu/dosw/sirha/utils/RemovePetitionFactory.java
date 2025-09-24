package edu.dosw.sirha.utils;

import edu.dosw.sirha.model.ClassPetition;
import edu.dosw.sirha.model.RemovePetition;
import edu.dosw.sirha.model.Student;

/**
 * Factory for creating RemovePetition instances.
 */
public class RemovePetitionFactory extends ClassPetitionFactory {

    /**
     * Creates a RemovePetition instance.
     * @param subjectCode
     * @param observations
     * @param student
     * @param groupIds
     * @return a new RemovePetition instance
     * @throws IllegalArgumentException if groupIds is empty
     */
    @Override
    public ClassPetition createPetition(String subjectCode, String observations,
                                        Student student, String... groupIds) {
        if (groupIds.length < 1) {
            throw new IllegalArgumentException("RemovePetition requiere currentGroupId");
        }
        return new RemovePetition(subjectCode, observations, student, groupIds[0]);
    }
}
