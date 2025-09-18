package edu.dosw.sirha.utils;

import edu.dosw.sirha.model.ClassPetition;
import edu.dosw.sirha.model.RemovePetition;

public class RemovePetitionCreator extends ClassPetitionCreator {

    @Override
    public ClassPetition createPetition(String subjectCode, String observations,
                                        String studentId, String... groupIds) {
        if (groupIds.length < 1) {
            throw new IllegalArgumentException("RemovePetition requiere currentGroupId");
        }
        return new RemovePetition(subjectCode, observations, studentId, groupIds[0]);
    }
}
