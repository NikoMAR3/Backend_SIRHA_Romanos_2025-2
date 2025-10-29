package edu.dosw.sirha.model.components.util;

import edu.dosw.sirha.model.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Petition Handlers Tests")
class PetitionHandlersTest {

    private Petition createBasePetition() {
        Petition petition = new Petition();
        petition.setPetitionId("PET001");
        petition.setStudentId("student123");
        petition.setType(PetitionType.ADD_SUBJECT);
        petition.setSubjectId("MATH101");
        petition.setState(PetitionState.PENDING);
        petition.setCreationDate(LocalDateTime.now());
        return petition;
    }



    @Test
    @DisplayName("ProfessorHandler - Should approve petition with LOW priority")
    void professorHandler_ShouldApproveLowPriorityPetition() {

        ProfessorHandler professorHandler = new ProfessorHandler();
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.LOW);


        boolean result = professorHandler.answerPetition(petition);


        assertTrue(result);
    }

    @Test
    @DisplayName("ProfessorHandler - Should not approve petition with MEDIUM priority")
    void professorHandler_ShouldNotApproveMediumPriorityPetition() {

        ProfessorHandler professorHandler = new ProfessorHandler();
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.MEDIUM);

        boolean result = professorHandler.answerPetition(petition);


        assertFalse(result);
    }

    @Test
    @DisplayName("ProfessorHandler - Should not approve petition with HIGH priority")
    void professorHandler_ShouldNotApproveHighPriorityPetition() {

        ProfessorHandler professorHandler = new ProfessorHandler();
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.HIGH);


        boolean result = professorHandler.answerPetition(petition);


        assertFalse(result);
    }

    @Test
    @DisplayName("ProfessorHandler - Should not approve petition with URGENT priority")
    void professorHandler_ShouldNotApproveUrgentPriorityPetition() {

        ProfessorHandler professorHandler = new ProfessorHandler();
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.URGENT);


        boolean result = professorHandler.answerPetition(petition);


        assertFalse(result);
    }

    @Test
    @DisplayName("ProfessorHandler - Should delegate to next handler when cannot answer")
    void professorHandler_ShouldDelegateToNextHandler() {

        ProfessorHandler professorHandler = new ProfessorHandler();
        AcademicVicePresidentHandler nextHandler = new AcademicVicePresidentHandler();
        professorHandler.setNextHandler(nextHandler);
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.MEDIUM);


        boolean result = professorHandler.answerPetition(petition);


        assertTrue(result);
    }



    @Test
    @DisplayName("DeanHandler - Should approve petition with URGENT priority")
    void deanHandler_ShouldApproveUrgentPriorityPetition() {

        DeanHandler deanHandler = new DeanHandler();
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.URGENT);

        boolean result = deanHandler.answerPetition(petition);

        assertTrue(result);
    }

    @Test
    @DisplayName("DeanHandler - Should not approve petition with LOW priority")
    void deanHandler_ShouldNotApproveLowPriorityPetition() {

        DeanHandler deanHandler = new DeanHandler();
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.LOW);

        boolean result = deanHandler.answerPetition(petition);

        assertFalse(result);
    }

    @Test
    @DisplayName("DeanHandler - Should not approve petition with MEDIUM priority")
    void deanHandler_ShouldNotApproveMediumPriorityPetition() {

        DeanHandler deanHandler = new DeanHandler();
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.MEDIUM);


        boolean result = deanHandler.answerPetition(petition);


        assertFalse(result);
    }

    @Test
    @DisplayName("DeanHandler - Should not approve petition with HIGH priority")
    void deanHandler_ShouldNotApproveHighPriorityPetition() {

        DeanHandler deanHandler = new DeanHandler();
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.HIGH);


        boolean result = deanHandler.answerPetition(petition);


        assertFalse(result);
    }

    @Test
    @DisplayName("DeanHandler - Should delegate to next handler when cannot answer")
    void deanHandler_ShouldDelegateToNextHandler() {
        // Arrange
        DeanHandler deanHandler = new DeanHandler();
        AcademicVicePresidentHandler nextHandler = new AcademicVicePresidentHandler();
        deanHandler.setNextHandler(nextHandler);
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.HIGH);

        // Act
        boolean result = deanHandler.answerPetition(petition);

        // Assert
        assertTrue(result);
    }

    // ==================== ACADEMIC VICE PRESIDENT HANDLER TESTS ====================

    @Test
    @DisplayName("AcademicVicePresidentHandler - Should approve petition with HIGH priority")
    void vpHandler_ShouldApproveHighPriorityPetition() {
        // Arrange
        AcademicVicePresidentHandler vpHandler = new AcademicVicePresidentHandler();
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.HIGH);

        // Act
        boolean result = vpHandler.answerPetition(petition);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("AcademicVicePresidentHandler - Should approve petition with MEDIUM priority")
    void vpHandler_ShouldApproveMediumPriorityPetition() {
        // Arrange
        AcademicVicePresidentHandler vpHandler = new AcademicVicePresidentHandler();
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.MEDIUM);

        // Act
        boolean result = vpHandler.answerPetition(petition);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("AcademicVicePresidentHandler - Should not approve petition with LOW priority")
    void vpHandler_ShouldNotApproveLowPriorityPetition() {
        // Arrange
        AcademicVicePresidentHandler vpHandler = new AcademicVicePresidentHandler();
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.LOW);

        // Act
        boolean result = vpHandler.answerPetition(petition);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("AcademicVicePresidentHandler - Should not approve petition with URGENT priority")
    void vpHandler_ShouldNotApproveUrgentPriorityPetition() {
        // Arrange
        AcademicVicePresidentHandler vpHandler = new AcademicVicePresidentHandler();
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.URGENT);

        // Act
        boolean result = vpHandler.answerPetition(petition);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("AcademicVicePresidentHandler - Should delegate to next handler when cannot answer")
    void vpHandler_ShouldDelegateToNextHandler() {
        // Arrange
        AcademicVicePresidentHandler vpHandler = new AcademicVicePresidentHandler();
        ProfessorHandler nextHandler = new ProfessorHandler();
        vpHandler.setNextHandler(nextHandler);
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.LOW);

        // Act
        boolean result = vpHandler.answerPetition(petition);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("AcademicVicePresidentHandler - Should return false when cannot answer and no next handler")
    void vpHandler_ShouldReturnFalseWhenNoNextHandler() {
        // Arrange
        AcademicVicePresidentHandler vpHandler = new AcademicVicePresidentHandler();
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.LOW);

        // Act
        boolean result = vpHandler.answerPetition(petition);

        // Assert
        assertFalse(result);
    }

    // ==================== CHAIN OF RESPONSIBILITY TESTS ====================

    @Test
    @DisplayName("Chain - Should process LOW priority at professor level")
    void chain_ShouldProcessLowPriorityAtProfessorLevel() {
        // Arrange
        ProfessorHandler professorHandler = new ProfessorHandler();
        DeanHandler deanHandler = new DeanHandler();
        AcademicVicePresidentHandler vpHandler = new AcademicVicePresidentHandler();
        professorHandler.setNextHandler(vpHandler);
        vpHandler.setNextHandler(deanHandler);
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.LOW);

        // Act
        boolean result = professorHandler.answerPetition(petition);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Chain - Should delegate MEDIUM priority to VP")
    void chain_ShouldDelegateMediumPriorityToVP() {
        // Arrange
        ProfessorHandler professorHandler = new ProfessorHandler();
        DeanHandler deanHandler = new DeanHandler();
        AcademicVicePresidentHandler vpHandler = new AcademicVicePresidentHandler();
        professorHandler.setNextHandler(vpHandler);
        vpHandler.setNextHandler(deanHandler);
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.MEDIUM);

        // Act
        boolean result = professorHandler.answerPetition(petition);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Chain - Should delegate HIGH priority to VP")
    void chain_ShouldDelegateHighPriorityToVP() {
        // Arrange
        ProfessorHandler professorHandler = new ProfessorHandler();
        DeanHandler deanHandler = new DeanHandler();
        AcademicVicePresidentHandler vpHandler = new AcademicVicePresidentHandler();
        professorHandler.setNextHandler(vpHandler);
        vpHandler.setNextHandler(deanHandler);
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.HIGH);

        // Act
        boolean result = professorHandler.answerPetition(petition);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Chain - Should delegate URGENT priority to Dean")
    void chain_ShouldDelegateUrgentPriorityToDean() {
        // Arrange
        ProfessorHandler professorHandler = new ProfessorHandler();
        DeanHandler deanHandler = new DeanHandler();
        AcademicVicePresidentHandler vpHandler = new AcademicVicePresidentHandler();
        professorHandler.setNextHandler(vpHandler);
        vpHandler.setNextHandler(deanHandler);
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.URGENT);

        // Act
        boolean result = professorHandler.answerPetition(petition);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Chain - Should handle complete chain correctly")
    void chain_ShouldHandleCompleteChainCorrectly() {
        // Arrange
        ProfessorHandler professorHandler = new ProfessorHandler();
        DeanHandler deanHandler = new DeanHandler();
        AcademicVicePresidentHandler vpHandler = new AcademicVicePresidentHandler();
        professorHandler.setNextHandler(vpHandler);
        vpHandler.setNextHandler(deanHandler);
        Petition petition = createBasePetition();

        // Test all priorities through the chain
        petition.setPriority(PetitionPriority.LOW);
        assertTrue(professorHandler.answerPetition(petition));

        petition.setPriority(PetitionPriority.MEDIUM);
        assertTrue(professorHandler.answerPetition(petition));

        petition.setPriority(PetitionPriority.HIGH);
        assertTrue(professorHandler.answerPetition(petition));

        petition.setPriority(PetitionPriority.URGENT);
        assertTrue(professorHandler.answerPetition(petition));
    }

    @Test
    @DisplayName("Chain - Should work with different chain configurations")
    void chain_ShouldWorkWithDifferentChainConfigurations() {
        // Arrange: Setup different chain: Dean -> VP
        DeanHandler deanFirst = new DeanHandler();
        AcademicVicePresidentHandler vpSecond = new AcademicVicePresidentHandler();
        deanFirst.setNextHandler(vpSecond);
        Petition petition = createBasePetition();

        // Act & Assert
        petition.setPriority(PetitionPriority.URGENT);
        assertTrue(deanFirst.answerPetition(petition));

        petition.setPriority(PetitionPriority.HIGH);
        assertTrue(deanFirst.answerPetition(petition));

        petition.setPriority(PetitionPriority.LOW);
        assertFalse(deanFirst.answerPetition(petition));
    }

    // ==================== HANDLER SETTER TESTS ====================

    @Test
    @DisplayName("Setter - Should set next handler correctly")
    void setter_ShouldSetNextHandlerCorrectly() {
        // Arrange
        ProfessorHandler handler1 = new ProfessorHandler();
        DeanHandler handler2 = new DeanHandler();
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.URGENT);

        // Act
        handler1.setNextHandler(handler2);

        // Assert
        assertTrue(handler1.answerPetition(petition));
    }

    @Test
    @DisplayName("Setter - Should allow null next handler")
    void setter_ShouldAllowNullNextHandler() {
        // Arrange
        ProfessorHandler handler = new ProfessorHandler();
        Petition petition = createBasePetition();
        petition.setPriority(PetitionPriority.MEDIUM);

        // Act
        handler.setNextHandler(null);

        // Assert
        assertFalse(handler.answerPetition(petition));
    }

    @Test
    @DisplayName("Setter - Should allow chain reconfiguration")
    void setter_ShouldAllowChainReconfiguration() {
        // Arrange
        ProfessorHandler handler = new ProfessorHandler();
        DeanHandler deanHandler = new DeanHandler();
        AcademicVicePresidentHandler vpHandler = new AcademicVicePresidentHandler();
        Petition petition = createBasePetition();

        // Act - First configuration
        handler.setNextHandler(deanHandler);
        petition.setPriority(PetitionPriority.URGENT);
        assertTrue(handler.answerPetition(petition));

        // Act - Reconfigure
        handler.setNextHandler(vpHandler);
        petition.setPriority(PetitionPriority.MEDIUM);
        assertTrue(handler.answerPetition(petition));
    }
}