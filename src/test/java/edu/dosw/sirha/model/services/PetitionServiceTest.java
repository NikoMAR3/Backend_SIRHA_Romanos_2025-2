package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.components.util.PetitionHandler;
import edu.dosw.sirha.model.entities.*;
import edu.dosw.sirha.model.persistence.repository.PetitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PetitionServiceTest {

    @Mock
    private PetitionRepository petitionRepository;
    @Mock
    private PetitionHandler petitionHandler;
    @Mock
    private StudentService studentService;

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
        petition.setModificationDate(LocalDateTime.now().minusDays(1));
        petition.setAssociateDeanery("Engineering");
        petition.setSubjectShortName("MATH101");
        petition.setIsExceptionalCase(true);
    }

    // --- CREATE ---
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
    void createPetition_missingType_throwsException() {
        petition.setType(null);
        assertThrows(IllegalArgumentException.class, () -> petitionService.createPetition(petition));
    }

    @Test
    void createPetition_missingPriority_throwsException() {
        petition.setPriority(null);
        assertThrows(IllegalArgumentException.class, () -> petitionService.createPetition(petition));
    }

    @Test
    void createPetition_missingJustification_throwsException() {
        petition.setJustification("");
        assertThrows(IllegalArgumentException.class, () -> petitionService.createPetition(petition));
    }

    // --- MODIFY ---
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
    void modifyPetition_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> petitionService.modifyPetition(null));
    }

    @Test
    void modifyPetition_noId_throws() {
        petition.setPetitionId("");
        assertThrows(IllegalArgumentException.class, () -> petitionService.modifyPetition(petition));
    }

    @Test
    void modifyPetition_notExist_throws() {
        when(petitionRepository.existsById("123")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> petitionService.modifyPetition(petition));
    }

    @Test
    void modifyPetition_nullType_throws() {
        when(petitionRepository.existsById("123")).thenReturn(true);
        petition.setType(null);
        assertThrows(IllegalArgumentException.class, () -> petitionService.modifyPetition(petition));
    }

    @Test
    void modifyPetition_nullState_throws() {
        when(petitionRepository.existsById("123")).thenReturn(true);
        petition.setState(null);
        assertThrows(IllegalArgumentException.class, () -> petitionService.modifyPetition(petition));
    }

    @Test
    void modifyPetition_nullPriority_throws() {
        when(petitionRepository.existsById("123")).thenReturn(true);
        petition.setPriority(null);
        assertThrows(IllegalArgumentException.class, () -> petitionService.modifyPetition(petition));
    }

    // --- DELETE ---
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
    void deletePetition_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> petitionService.deletePetition(""));
        assertThrows(IllegalArgumentException.class, () -> petitionService.deletePetition(null));
    }

    // --- SEARCH BY ID ---
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
    void searchPetitionsById_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> petitionService.searchPetitionsById(""));
        assertThrows(IllegalArgumentException.class, () -> petitionService.searchPetitionsById(null));
    }

    // --- SEARCH ALL ---
    @Test
    void searchAllPetitions_returnsList() {
        when(petitionRepository.findAllByOrderByCreationDateDesc())
                .thenReturn(List.of(petition));
        List<Petition> result = petitionService.searchAllPetitions();
        assertEquals(1, result.size());
    }

    // --- SEARCH BY TYPE ---
    @Test
    void searchPetitionByType_returnsList() {
        when(petitionRepository.findByType(PetitionType.ADD_SUBJECT))
                .thenReturn(List.of(petition));
        List<Petition> result = petitionService.searchPetitionByType(PetitionType.ADD_SUBJECT);
        assertEquals(1, result.size());
    }

    @Test
    void searchPetitionByType_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> petitionService.searchPetitionByType(null));
    }

    // --- CHANGE STATE/PRIORITY ---
    @Test
    void changePetitionState_success() {
        when(petitionRepository.findById("123")).thenReturn(Optional.of(petition));
        when(petitionRepository.save(any(Petition.class))).thenReturn(petition);

        Petition result = petitionService.changePetitionState("123", PetitionState.APPROVED);
        assertEquals(PetitionState.APPROVED, result.getState());
    }

    @Test
    void changePetitionState_invalidParams_throws() {
        assertThrows(IllegalArgumentException.class, () -> petitionService.changePetitionState("", PetitionState.APPROVED));
        assertThrows(IllegalArgumentException.class, () -> petitionService.changePetitionState("123", null));
    }

    @Test
    void changePetitionPriority_success() {
        when(petitionRepository.findById("123")).thenReturn(Optional.of(petition));
        when(petitionRepository.save(any(Petition.class))).thenReturn(petition);

        Petition result = petitionService.changePetitionPriority("123", PetitionPriority.LOW);
        assertEquals(PetitionPriority.LOW, result.getPriority());
    }

    @Test
    void changePetitionPriority_invalidParams_throws() {
        assertThrows(IllegalArgumentException.class, () -> petitionService.changePetitionPriority("", PetitionPriority.LOW));
        assertThrows(IllegalArgumentException.class, () -> petitionService.changePetitionPriority("123", null));
    }

    // --- PRIORITY ---
    @Test
    void searchPetitionsByPriority_returnsList() {
        when(petitionRepository.findByPriority(PetitionPriority.HIGH))
                .thenReturn(List.of(petition));
        List<Petition> result = petitionService.searchPetitionsByPriority(PetitionPriority.HIGH);
        assertEquals(1, result.size());
    }

    @Test
    void searchPetitionsByPriority_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> petitionService.searchPetitionsByPriority(null));
    }

    // --- CREATION DATE ---
    @Test
    void searchPetitionsByCreationDate_returnsList() {
        LocalDateTime dt = LocalDateTime.now().minusDays(2);
        when(petitionRepository.findByCreationDateAfter(dt)).thenReturn(List.of(petition));
        List<Petition> result = petitionService.searchPetitionsByCreationDate(dt);
        assertEquals(1, result.size());
    }

    @Test
    void searchPetitionsByCreationDate_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> petitionService.searchPetitionsByCreationDate(null));
    }

    // --- STATE ---
    @Test
    void searchPetitionsByState_returnsList() {
        when(petitionRepository.findByState(PetitionState.PENDING))
                .thenReturn(List.of(petition));
        List<Petition> result = petitionService.searchPetitionsByState(PetitionState.PENDING);
        assertEquals(1, result.size());
    }

    @Test
    void searchPetitionsByState_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> petitionService.searchPetitionsByState(null));
    }

    // --- DEANERY ---
    @Test
    void searchPetitionsByDeanery_returnsList() {
        when(petitionRepository.findByAssociateDeanery("Engineering"))
                .thenReturn(List.of(petition));
        List<Petition> result = petitionService.searchPetitionsByDeanery("Engineering");
        assertEquals(1, result.size());
    }

    @Test
    void searchPetitionsByDeanery_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> petitionService.searchPetitionsByDeanery(null));
        assertThrows(IllegalArgumentException.class, () -> petitionService.searchPetitionsByDeanery(""));
    }

    // --- STUDENT ID ---
    @Test
    void searchPetitionsByStudentId_returnsList() {
        when(petitionRepository.findByStudentId("stu01"))
                .thenReturn(List.of(petition));
        List<Petition> result = petitionService.searchPetitionsByStudentId("stu01");
        assertEquals(1, result.size());
    }

    @Test
    void searchPetitionsByStudentId_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> petitionService.searchPetitionsByStudentId(null));
        assertThrows(IllegalArgumentException.class, () -> petitionService.searchPetitionsByStudentId(""));
    }

    // --- SUBJECT ID ---
    @Test
    void searchPetitionsBySubjectId_returnsList() {
        when(petitionRepository.findBySubjectId("subj01"))
                .thenReturn(List.of(petition));
        List<Petition> result = petitionService.searchPetitionsBySubjectId("subj01");
        assertEquals(1, result.size());
    }

    @Test
    void searchPetitionsBySubjectId_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> petitionService.searchPetitionsBySubjectId(null));
        assertThrows(IllegalArgumentException.class, () -> petitionService.searchPetitionsBySubjectId(""));
    }

    // --- countByState ---
    @Test
    void countByState_success() {
        when(petitionRepository.countByState(PetitionState.PENDING)).thenReturn(10L);
        assertEquals(10L, petitionService.countByState(PetitionState.PENDING));
    }

    @Test
    void countByState_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> petitionService.countByState(null));
    }

    // --- countByType ---
    @Test
    void countByType_success() {
        when(petitionRepository.countByType(PetitionType.ADD_SUBJECT)).thenReturn(5L);
        assertEquals(5L, petitionService.countByType(PetitionType.ADD_SUBJECT));
    }

    @Test
    void countByType_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> petitionService.countByType(null));
    }

    // --- getPetitionStatsByDeanery ---
    @Test
    void getPetitionStatsByDeanery_success() {
        Petition p2 = new Petition();
        p2.setState(PetitionState.APPROVED);
        Petition p3 = new Petition();
        p3.setState(PetitionState.REPROVED);
        when(petitionRepository.findByAssociateDeanery("Engineering")).thenReturn(List.of(petition, p2, p3));
        Map<String, Long> stats = petitionService.getPetitionStatsByDeanery("Engineering");
        assertEquals(3L, stats.get("total"));
        assertEquals(1L, stats.get("pending"));
        assertEquals(1L, stats.get("approved"));
        assertEquals(1L, stats.get("rejected"));
    }

    @Test
    void getPetitionStatsByDeanery_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> petitionService.getPetitionStatsByDeanery(null));
        assertThrows(IllegalArgumentException.class, () -> petitionService.getPetitionStatsByDeanery(""));
    }

    // --- getMostRequestedSubjects ---
    @Test
    void getMostRequestedSubjects_success() {
        Petition p2 = new Petition();
        p2.setSubjectShortName("PHY101");
        when(petitionRepository.findByType(PetitionType.CHANGE_GROUP)).thenReturn(List.of(petition, p2, petition));
        List<Map<String, Object>> result = petitionService.getMostRequestedSubjects(2);
        assertEquals(2, result.size());
        assertEquals("MATH101", result.get(0).get("subject"));
        assertEquals(2L, result.get(0).get("count"));
    }

    // --- searchExceptionalCases ---
    @Test
    void searchExceptionalCases_success() {
        Petition p2 = new Petition();
        p2.setCreationDate(LocalDateTime.now());
        when(petitionRepository.findByIsExceptionalCase(true)).thenReturn(List.of(petition, p2));
        List<Petition> result = petitionService.searchExceptionalCases();
        assertEquals(2, result.size());
    }

    @Test
    void searchExceptionalCases_repositoryError_throws() {
        when(petitionRepository.findByIsExceptionalCase(true)).thenThrow(new RuntimeException("DB fail"));
        assertThrows(IllegalArgumentException.class, () -> petitionService.searchExceptionalCases());
    }

    // --- getStudentPetitions ---
    @Test
    void getStudentPetitions_success() {
        Petition p2 = new Petition();
        p2.setStudentId("stu02");
        when(petitionRepository.findAllByOrderByCreationDateDesc()).thenReturn(List.of(petition, p2));
        when(studentService.searchStudentById(anyString())).thenReturn(mock(Student.class));
        List<Petition> result = petitionService.getStudentPetitions("stu01");
        assertEquals(2, result.size());
    }

    @Test
    void getStudentPetitions_someInvalid() {
        Petition p2 = new Petition();
        p2.setStudentId("stu02");
        when(petitionRepository.findAllByOrderByCreationDateDesc()).thenReturn(List.of(petition, p2));
        when(studentService.searchStudentById(petition.getStudentId())).thenReturn(mock(Student.class));
        when(studentService.searchStudentById(p2.getStudentId())).thenThrow(new RuntimeException("Not found"));

        List<Petition> result = petitionService.getStudentPetitions("stu01");
        assertEquals(1, result.size()); // Solo petition es válido
    }
}