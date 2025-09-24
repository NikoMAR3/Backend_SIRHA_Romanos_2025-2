package edu.dosw.sirha.model;

import edu.dosw.sirha.services.PetitionsLoader;
import edu.dosw.sirha.core.PetitionCommand;
import edu.dosw.sirha.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test class for testing the functionality of the Dean class.
 * This test class verifies the proper integration between Dean, PetitionAssistant,
 * and various service managers in the petition workflow system. Tests include
 * petition creation, approval/rejection processes, command execution,
 * and dependency injection validation using JUnit 5 and Mockito framework.
 */
class DeanTest {

    private PetitionAssistant assistant;
    private PetitionManager petitionManager;
    private ScheduleManager scheduleManager;
    private TrafficLightManager trafficLightManager;
    private ClassManager classManager;
    private PetitionsLoader loader;
    private Dean dean;
    private Student student;

    /**
     * Sets up the test environment before each test method execution.
     * Initializes all mock dependencies, creates a PetitionAssistant instance,
     * configures the Dean with necessary services, and sets up a test Student
     * with proper observer relationships for the petition workflow.
     *
     * @throws Exception if any initialization error occurs
     */
    @BeforeEach
    void setUp() throws Exception {
        scheduleManager = mock(ScheduleManager.class);
        petitionManager = mock(PetitionManager.class);
        trafficLightManager = mock(TrafficLightManager.class);
        classManager = mock(ClassManager.class);

        assistant = new PetitionAssistant();

        loader = new PetitionsLoader();
        loader.setAssistant(assistant);
        loader.setPetitionManager(petitionManager);

        dean = new Dean(scheduleManager, petitionManager, trafficLightManager,
                classManager, assistant);

        dean.configure("DEAN001", "Computer Science");

        student = new Student("Juan Perez", "stu-001");
        student.addObserver(loader);
    }

    /**
     * Tests the petition creation and loading workflow.
     * Verifies that when a student creates a petition, it is properly
     * loaded into the PetitionAssistant through the observer pattern
     * and that the corresponding command is correctly generated.
     */
    @Test
    void studentCreatesPetition_LoadedIntoAssistant() {
        ChangePetition petition = new ChangePetition(
                "CS101", "Cambio de grupo", student,
                "CS101-01", "CS101-02"
        );

        student.addPetition(petition);

        PetitionCommand command = assistant.getCommandByPetition(petition);

        assertNotNull(command, "El command debe estar cargado en el assistant");
        assertEquals(petition, command.getPetition(), "El command debe envolver la misma petición");
    }

    /**
     * Tests the petition approval process.
     * Verifies that when the Dean approves a petition, the corresponding
     * command is executed and the petition status is updated to "APPROVED".
     * Uses Mockito spy to verify that the execute() method is called.
     */
    @Test
    void deanApprovesPetition_CommandExecutedAndApproved() {
        ChangePetition petition = new ChangePetition(
                "CS101", "Cambio de grupo", student,
                "CS101-01", "CS101-02"
        );
        student.addPetition(petition);

        PetitionCommand originalCommand = assistant.getCommandByPetition(petition);
        PetitionCommand spyCommand = spy(originalCommand);

        assistant.addCommand(petition.getType(), spyCommand);

        dean.answerPetition(petition, true);

        verify(spyCommand).execute();
        assertEquals("APPROVED", petition.getStatus());
    }

    /**
     * Tests the petition rejection process.
     * Verifies that when the Dean rejects a petition, the corresponding
     * command is undone and the petition status is updated to "DENIED".
     * Uses Mockito spy to verify that the undo() method is called.
     */
    @Test
    void deanRejectsPetition_CommandUndoneAndRejected() {
        ChangePetition petition = new ChangePetition(
                "CS101", "Cambio de grupo", student,
                "CS101-01", "CS101-02"
        );
        student.addPetition(petition);

        PetitionCommand originalCommand = assistant.getCommandByPetition(petition);
        PetitionCommand spyCommand = spy(originalCommand);

        assistant.addCommand(petition.getType(), spyCommand);

        dean.answerPetition(petition, false);

        verify(spyCommand).undo();
        assertEquals("DENIED", petition.getStatus());
    }

    /**
     * Tests the Dean's dependency injection and configuration.
     * Verifies that the Dean instance is properly initialized with all
     * required dependencies, that configuration values are correctly set,
     * and that core methods can be called without throwing exceptions.
     */
    @Test
    void deanHasAllDependenciesInjected() {
        assertNotNull(dean, "Dean debe estar inicializado");
        assertEquals("DEAN001", dean.getDean_id());
        assertEquals("Computer Science", dean.getMajor());

        assertDoesNotThrow(() -> {
            dean.checkPetitions("CHANGE");
            dean.checkTrafficLight();
        });
    }
}
