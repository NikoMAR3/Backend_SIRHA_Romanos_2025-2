package edu.dosw.sirha.model;

import edu.dosw.sirha.core.PetitionCommand;
import edu.dosw.sirha.services.PetitionManager;
import edu.dosw.sirha.core.RemoveStudentPetitionCommand;

public class RemovePetition extends ClassPetition {
    private String currentGroupId;

    public RemovePetition() {}

    public RemovePetition(String subjectCode, String observations, Student student, String currentGroupId) {
        super("REMOVE", subjectCode, observations, student);
        this.currentGroupId = currentGroupId;
    }

    public String getCurrentGroupId() { return currentGroupId; }
    public void setCurrentGroupId(String currentGroupId) { this.currentGroupId = currentGroupId; }

    @Override
    public void ifAcceptedProcedure() {
        System.out.println("Procesando solicitud de remoción del grupo: " + currentGroupId);
    }

    @Override
    public PetitionCommand toCommand(PetitionManager manager) {
        return new RemoveStudentPetitionCommand(manager, this);
    }
}
