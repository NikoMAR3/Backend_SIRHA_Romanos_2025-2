package edu.dosw.sirha.utils;

public class RemovePetition extends ClassPetition {
    private String currentGroupId;

    public RemovePetition() {}

    public RemovePetition(String subjectCode, String observations, String studentId, String currentGroupId) {
        super("REMOVE", subjectCode, observations, studentId);
        this.currentGroupId = currentGroupId;
    }

    public String getCurrentGroupId() { return currentGroupId; }
    public void setCurrentGroupId(String currentGroupId) { this.currentGroupId = currentGroupId; }

    @Override
    public void ifAcceptedProcedure() {
        System.out.println("Procesando solicitud de remoción del grupo: " + currentGroupId);
    }

    @Override
    public Command toCommand(PetitionManager manager) {
        return new RemoveStudentCommand(manager, this);
    }
}
