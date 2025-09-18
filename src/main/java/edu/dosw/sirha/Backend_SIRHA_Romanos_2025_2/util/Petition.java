package edu.dosw.sirha.Backend_SIRHA_Romanos_2025_2.util;

public abstract class Petition {
    private DateType dateOfCreation;
    private String priority;

    public String getPriority() {
        return priority;
    }

    public String getPetitionState(){
        return "en proceso";
    }

    public String getPetitionCreationDate(){
        return "";
    }

    public void ifAcceptedProcedure(){

    }

    public Command toCommand(PetitionManager manager){
        return null;
    }

}