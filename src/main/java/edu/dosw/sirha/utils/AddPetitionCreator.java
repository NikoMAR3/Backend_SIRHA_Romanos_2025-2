package edu.dosw.sirha.utils;

public class AddPetitionCreator extends ClassPetitionCreator {

    @Override
    public ClassPetition createPetition(String subjectCode, String observations,
                                        String studentId, String... groupIds) {
        if (groupIds.length < 1) {
            throw new IllegalArgumentException("AddPetition requiere targetGroupId");
        }
        return new AddPetition(subjectCode, observations, studentId, groupIds[0]);
    }
}
