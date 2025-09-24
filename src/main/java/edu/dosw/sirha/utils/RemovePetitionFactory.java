package edu.dosw.sirha.utils;

import edu.dosw.sirha.model.ClassPetition;
import edu.dosw.sirha.model.RemovePetition;
import edu.dosw.sirha.model.Student;

public class RemovePetitionFactory extends ClassPetitionFactory {

    @Override
    public ClassPetition createPetition(String subjectCode, String observations,
                                        Student student, String... groupIds) {
        if (groupIds.length < 1) {
            throw new IllegalArgumentException("RemovePetition requiere currentGroupId");
        }
        return new RemovePetition(subjectCode, observations, student, groupIds[0]);
    }
}
