package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.components.util.PetitionHandler;
import edu.dosw.sirha.model.entities.Petition;
import edu.dosw.sirha.model.entities.PetitionPriority;
import edu.dosw.sirha.model.entities.PetitionState;
import edu.dosw.sirha.model.entities.PetitionType;
import edu.dosw.sirha.model.persistence.repository.PetitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PetitionServiceTest {
    @Mock
    private PetitionRepository petitionRepository;
    @Mock
    private PetitionHandler petitionHandler;
    @InjectMocks
    private PetitionService petitionService;
    private Petition petition;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        petition = new Petition();
        petition.setPetitionId("123");
        petition.setStudentId("stu01");
        petition.setType(PetitionType.ADD_SUBJECT);
        petition.setPriority(PetitionPriority.HIGH);
        petition.setJustification("Valid justification");
        petition.setState(PetitionState.PENDING);
        petition.setCreationDate(LocalDateTime.now().minusDays(1));
    }

    @Test
    void createPetition_success() {
        when(petitionRepository.save(any(Petition.class))).thenReturn(petition);
        when(petitionHandler.answerPetition(any(Petition.class))).thenReturn(true);

        Petition result = petitionService.createPetition(petition);

        assertNotNull(result);
        assertEquals("123", result.getPetitionId());
        assertEquals(PetitionState.PENDING, result.getState());
        verify(petitionRepository, times(1)).save(any(Petition.class));
        verify(petitionHandler, times(1)).answerPetition(result);
    }

    @Test
    void createPetition_nullPetition_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> petitionService.createPetition(null));
    }

    @Test
    void createPetition_missingStudentId_throwsException() {
        petition.setStudentId(null);
        assertThrows(IllegalArgumentException.class, () -> petitionService.createPetition(petition));
    }

    @Test
    void modifyPetition_success() {
        when(petitionRepository.existsById("123")).thenReturn(true);
        when(petitionRepository.save(any(Petition.class))).thenReturn(petition);

        Petition result = petitionService.modifyPetition(petition);

        assertNotNull(result);
        assertEquals("123", result.getPetitionId());
        verify(petitionRepository, times(1)).save(petition);
    }

    @Test
    void modifyPetition_nonExistingId_throwsException() {
        when(petitionRepository.existsById("123")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> petitionService.modifyPetition(petition));
    }

    @Test
    void deletePetition_success() {
        when(petitionRepository.existsById("123")).thenReturn(true);
        boolean deleted = petitionService.deletePetition("123");
        assertTrue(deleted);
        verify(petitionRepository).deleteById("123");
    }

    @Test
    void deletePetition_notFound_returnsFalse() {
        when(petitionRepository.existsById("123")).thenReturn(false);
        boolean deleted = petitionService.deletePetition("123");
        assertFalse(deleted);
    }

    @Test
    void searchPetitionsById_found() {
        when(petitionRepository.findById("123")).thenReturn(Optional.of(petition));
        Petition result = petitionService.searchPetitionsById("123");
        assertEquals("123", result.getPetitionId());
    }

    @Test
    void searchPetitionsById_notFound_throwsException() {
        when(petitionRepository.findById("123")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> petitionService.searchPetitionsById("123"));
    }

    @Test
    void searchAllPetitions_returnsList() {
        when(petitionRepository.findAllByOrderByCreationDateDesc())
                .thenReturn(Arrays.asList(petition));
        List<Petition> result = petitionService.searchAllPetitions();
        assertEquals(1, result.size());
    }

    @Test
    void changePetitionState_success() {
        when(petitionRepository.findById("123")).thenReturn(Optional.of(petition));
        when(petitionRepository.save(any(Petition.class))).thenReturn(petition);

        Petition result = petitionService.changePetitionState("123", PetitionState.APPROVED);
        assertEquals(PetitionState.APPROVED, result.getState());
    }

    @Test
    void changePetitionPriority_success() {
        when(petitionRepository.findById("123")).thenReturn(Optional.of(petition));
        when(petitionRepository.save(any(Petition.class))).thenReturn(petition);

        Petition result = petitionService.changePetitionPriority("123", PetitionPriority.LOW);
        assertEquals(PetitionPriority.LOW, result.getPriority());
    }

    @Test
    void searchPetitionByType_returnsList() {
        when(petitionRepository.findByType(PetitionType.ADD_SUBJECT))
                .thenReturn(Arrays.asList(petition));
        List<Petition> result = petitionService.searchPetitionByType(PetitionType.ADD_SUBJECT);
        assertEquals(1, result.size());
    }

    @Test
    void searchPetitionsByPriority_returnsList() {
        when(petitionRepository.findByPriority(PetitionPriority.HIGH))
                .thenReturn(Arrays.asList(petition));
        List<Petition> result = petitionService.searchPetitionsByPriority(PetitionPriority.HIGH);
        assertEquals(1, result.size());
    }

    @Test
    void searchPetitionsByState_returnsList() {
        when(petitionRepository.findByState(PetitionState.PENDING))
                .thenReturn(Arrays.asList(petition));
        List<Petition> result = petitionService.searchPetitionsByState(PetitionState.PENDING);
        assertEquals(1, result.size());
    }

    @Test
    void searchPetitionsByDeanery_returnsList() {
        when(petitionRepository.findByDeanery("Engineering"))
                .thenReturn(Arrays.asList(petition));
        List<Petition> result = petitionService.searchPetitionsByDeanery("Engineering");
        assertEquals(1, result.size());
    }

    @Test
    void searchPetitionsByStudentId_returnsList() {
        when(petitionRepository.findByStudentId("stu01"))
                .thenReturn(Arrays.asList(petition));
        List<Petition> result = petitionService.searchPetitionsByStudentId("stu01");
        assertEquals(1, result.size());
    }

    @Test
    void searchPetitionsBySubjectId_returnsList() {
        when(petitionRepository.findBySubjectId("subj01"))
                .thenReturn(Arrays.asList(petition));
        List<Petition> result = petitionService.searchPetitionsBySubjectId("subj01");
        assertEquals(1, result.size());
    }
}
