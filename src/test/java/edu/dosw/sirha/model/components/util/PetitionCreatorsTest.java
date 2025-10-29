package edu.dosw.sirha.model.components.util;

import edu.dosw.sirha.controller.dtos.PetitionRequestDTO;
import edu.dosw.sirha.model.entities.*;
import edu.dosw.sirha.model.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Petition Creators Tests")
class PetitionCreatorsTest {

    @Nested
    @DisplayName("AddSubjectCreator Tests")
    @ExtendWith(MockitoExtension.class)
    class AddSubjectCreatorTest {

        @Mock
        private StudentService studentService;

        @Mock
        private DeanService deanService;

        @Mock
        private ProfessorService professorService;

        @Mock
        private AcademicVicePresidentService academicVicePresidentService;

        @InjectMocks
        private AddSubjectCreator addSubjectCreator;

        private PetitionRequestDTO requestDTO;

        @BeforeEach
        void setUp() {
            requestDTO = new PetitionRequestDTO();
            requestDTO.setUserID("student123");
            requestDTO.setType(PetitionType.ADD_SUBJECT);
            requestDTO.setDescription("Need to add this subject for my curriculum");

            Map<String, Object> details = new HashMap<>();
            details.put("subjectId", "MATH101");
            requestDTO.setDetails(details);
        }

        @Test
        @DisplayName("Should create ADD_SUBJECT petition with correct fields")
        void shouldCreateAddSubjectPetition() {
            // Arrange
            Student student = new Student();
            student.setId("student123");
            when(studentService.searchStudentById("student123")).thenReturn(student);

            // Act
            Petition petition = addSubjectCreator.createPetition(requestDTO);

            // Assert
            assertNotNull(petition);
            assertNotNull(petition.getPetitionId());
            assertEquals("student123", petition.getStudentId());
            assertEquals(PetitionType.ADD_SUBJECT, petition.getType());
            assertEquals("MATH101", petition.getSubjectId());
            assertEquals("Need to add this subject for my curriculum", petition.getJustification());
            assertEquals(PetitionState.PENDING, petition.getState());
            assertNotNull(petition.getCreationDate());
            assertEquals(PetitionPriority.LOW, petition.getPriority());
        }

        @Test
        @DisplayName("Should support ADD_SUBJECT type")
        void shouldSupportAddSubjectType() {
            assertTrue(addSubjectCreator.supports(PetitionType.ADD_SUBJECT));
        }

        @Test
        @DisplayName("Should not support other types")
        void shouldNotSupportOtherTypes() {
            assertFalse(addSubjectCreator.supports(PetitionType.REMOVE_SUBJECT));
            assertFalse(addSubjectCreator.supports(PetitionType.CHANGE_GROUP));
        }

        @Test
        @DisplayName("Should assign LOW priority for student")
        void shouldAssignLowPriorityForStudent() {

            Student student = new Student();
            when(studentService.searchStudentById("student123")).thenReturn(student);


            Petition petition = addSubjectCreator.createPetition(requestDTO);


            assertEquals(PetitionPriority.LOW, petition.getPriority());
        }

        @Test
        @DisplayName("Should assign URGENT priority for dean")
        void shouldAssignUrgentPriorityForDean() {
            // Arrange
            requestDTO.setUserID("dean123");
            Dean dean = new Dean();
            when(studentService.searchStudentById("dean123")).thenThrow(new RuntimeException());
            when(deanService.searchDeanByCode("dean123")).thenReturn(dean);

            // Act
            Petition petition = addSubjectCreator.createPetition(requestDTO);

            // Assert
            assertEquals(PetitionPriority.URGENT, petition.getPriority());
        }

        @Test
        @DisplayName("Should assign MEDIUM priority for professor")
        void shouldAssignMediumPriorityForProfessor() {
            // Arrange
            requestDTO.setUserID("prof123");
            Professor professor = new Professor();
            when(studentService.searchStudentById("prof123")).thenThrow(new RuntimeException());
            when(deanService.searchDeanByCode("prof123")).thenThrow(new RuntimeException());
            when(professorService.searchProfessorById("prof123")).thenReturn(professor);

            // Act
            Petition petition = addSubjectCreator.createPetition(requestDTO);

            // Assert
            assertEquals(PetitionPriority.MEDIUM, petition.getPriority());
        }
    }

    @Nested
    @DisplayName("RemoveSubjectCreator Tests")
    @ExtendWith(MockitoExtension.class)
    class RemoveSubjectCreatorTest {

        @Mock
        private StudentService studentService;

        @Mock
        private DeanService deanService;

        @Mock
        private ProfessorService professorService;

        @Mock
        private AcademicVicePresidentService academicVicePresidentService;

        @InjectMocks
        private RemoveSubjectCreator removeSubjectCreator;

        private PetitionRequestDTO requestDTO;

        @BeforeEach
        void setUp() {
            requestDTO = new PetitionRequestDTO();
            requestDTO.setUserID("student123");
            requestDTO.setType(PetitionType.REMOVE_SUBJECT);
            requestDTO.setDescription("Need to remove this subject");

            Map<String, Object> details = new HashMap<>();
            details.put("subjectId", "PHYS201");
            requestDTO.setDetails(details);
        }

        @Test
        @DisplayName("Should create REMOVE_SUBJECT petition with correct fields")
        void shouldCreateRemoveSubjectPetition() {
            // Arrange
            Student student = new Student();
            when(studentService.searchStudentById("student123")).thenReturn(student);

            // Act
            Petition petition = removeSubjectCreator.createPetition(requestDTO);

            // Assert
            assertNotNull(petition);
            assertNotNull(petition.getPetitionId());
            assertEquals("student123", petition.getStudentId());
            assertEquals(PetitionType.REMOVE_SUBJECT, petition.getType());
            assertEquals("PHYS201", petition.getSubjectId());
            assertEquals("Need to remove this subject", petition.getJustification());
            assertEquals(PetitionState.PENDING, petition.getState());
            assertNotNull(petition.getCreationDate());
        }

        @Test
        @DisplayName("Should support REMOVE_SUBJECT type")
        void shouldSupportRemoveSubjectType() {
            assertTrue(removeSubjectCreator.supports(PetitionType.REMOVE_SUBJECT));
        }

        @Test
        @DisplayName("Should not support other types")
        void shouldNotSupportOtherTypes() {
            assertFalse(removeSubjectCreator.supports(PetitionType.ADD_SUBJECT));
            assertFalse(removeSubjectCreator.supports(PetitionType.CHANGE_GROUP));
        }

        @Test
        @DisplayName("Should generate unique petition IDs")
        void shouldGenerateUniquePetitionIds() {
            // Arrange
            Student student = new Student();
            when(studentService.searchStudentById("student123")).thenReturn(student);

            // Act
            Petition petition1 = removeSubjectCreator.createPetition(requestDTO);
            Petition petition2 = removeSubjectCreator.createPetition(requestDTO);

            // Assert
            assertNotEquals(petition1.getPetitionId(), petition2.getPetitionId());
        }
    }

    @Nested
    @DisplayName("ChangeGroupCreator Tests")
    @ExtendWith(MockitoExtension.class)
    class ChangeGroupCreatorTest {

        @Mock
        private StudentService studentService;

        @Mock
        private DeanService deanService;

        @Mock
        private ProfessorService professorService;

        @Mock
        private AcademicVicePresidentService academicVicePresidentService;

        @InjectMocks
        private ChangeGroupCreator changeGroupCreator;

        private PetitionRequestDTO requestDTO;

        @BeforeEach
        void setUp() {
            requestDTO = new PetitionRequestDTO();
            requestDTO.setUserID("student123");
            requestDTO.setType(PetitionType.CHANGE_GROUP);
            requestDTO.setDescription("Need to change group for schedule conflict");

            Map<String, Object> details = new HashMap<>();
            details.put("subjectId", "CHEM301");
            requestDTO.setDetails(details);
        }

        @Test
        @DisplayName("Should create CHANGE_GROUP petition with correct fields")
        void shouldCreateChangeGroupPetition() {
            // Arrange
            Student student = new Student();
            when(studentService.searchStudentById("student123")).thenReturn(student);

            // Act
            Petition petition = changeGroupCreator.createPetition(requestDTO);

            // Assert
            assertNotNull(petition);
            assertNotNull(petition.getPetitionId());
            assertEquals("student123", petition.getStudentId());
            assertEquals(PetitionType.CHANGE_GROUP, petition.getType());
            assertEquals("CHEM301", petition.getSubjectId());
            assertEquals("Need to change group for schedule conflict", petition.getJustification());
            assertEquals(PetitionState.PENDING, petition.getState());
            assertNotNull(petition.getCreationDate());
        }

        @Test
        @DisplayName("Should support CHANGE_GROUP type")
        void shouldSupportChangeGroupType() {
            assertTrue(changeGroupCreator.supports(PetitionType.CHANGE_GROUP));
        }

        @Test
        @DisplayName("Should not support other types")
        void shouldNotSupportOtherTypes() {
            assertFalse(changeGroupCreator.supports(PetitionType.ADD_SUBJECT));
            assertFalse(changeGroupCreator.supports(PetitionType.REMOVE_SUBJECT));
        }

        @Test
        @DisplayName("Should assign URGENT priority for academic vice president")
        void shouldAssignUrgentPriorityForVP() {
            // Arrange
            requestDTO.setUserID("vp123");
            AcademicVicePresident vp = new AcademicVicePresident();
            when(studentService.searchStudentById("vp123")).thenThrow(new RuntimeException());
            when(deanService.searchDeanByCode("vp123")).thenThrow(new RuntimeException());
            when(professorService.searchProfessorById("vp123")).thenThrow(new RuntimeException());
            when(academicVicePresidentService.searchAcademicVicePresidentById("vp123")).thenReturn(vp);

            // Act
            Petition petition = changeGroupCreator.createPetition(requestDTO);

            // Assert
            assertEquals(PetitionPriority.URGENT, petition.getPriority());
        }

        @Test
        @DisplayName("Should handle missing details gracefully")
        void shouldHandleMissingDetails() {
            // Arrange
            requestDTO.setDetails(new HashMap<>());
            Student student = new Student();
            when(studentService.searchStudentById("student123")).thenReturn(student);

            // Act
            Petition petition = changeGroupCreator.createPetition(requestDTO);

            // Assert
            assertNotNull(petition);
            assertNull(petition.getSubjectId());
        }
    }

    @Nested
    @DisplayName("PetitionCreator Priority Calculation Tests")
    @ExtendWith(MockitoExtension.class)
    class PriorityCalculationTest {

        @Mock
        private StudentService studentService;

        @Mock
        private DeanService deanService;

        @Mock
        private ProfessorService professorService;

        @Mock
        private AcademicVicePresidentService academicVicePresidentService;

        @InjectMocks
        private AddSubjectCreator creator;

        private PetitionRequestDTO requestDTO;

        @BeforeEach
        void setUp() {
            requestDTO = new PetitionRequestDTO();
            Map<String, Object> details = new HashMap<>();
            details.put("subjectId", "TEST101");
            requestDTO.setDetails(details);
        }

        @Test
        @DisplayName("Should return LOW priority when all services throw exceptions")
        void shouldReturnLowPriorityWhenAllServicesFail() {
            // Arrange
            requestDTO.setUserID("unknown123");
            when(studentService.searchStudentById(anyString())).thenThrow(new RuntimeException());
            when(deanService.searchDeanByCode(anyString())).thenThrow(new RuntimeException());
            when(professorService.searchProfessorById(anyString())).thenThrow(new RuntimeException());
            when(academicVicePresidentService.searchAcademicVicePresidentById(anyString()))
                    .thenThrow(new RuntimeException());

            // Act
            Petition petition = creator.createPetition(requestDTO);

            // Assert
            assertEquals(PetitionPriority.LOW, petition.getPriority());
        }

        @Test
        @DisplayName("Should check services in correct order")
        void shouldCheckServicesInOrder() {
            // Arrange
            requestDTO.setUserID("test123");
            Student student = new Student();
            when(studentService.searchStudentById("test123")).thenReturn(student);

            // Act
            Petition petition = creator.createPetition(requestDTO);

            // Assert
            verify(studentService).searchStudentById("test123");
            verify(deanService, never()).searchDeanByCode(anyString());
            verify(professorService, never()).searchProfessorById(anyString());
            verify(academicVicePresidentService, never()).searchAcademicVicePresidentById(anyString());
        }
    }
}