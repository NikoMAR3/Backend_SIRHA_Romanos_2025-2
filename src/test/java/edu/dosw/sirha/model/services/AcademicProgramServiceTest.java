package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.AcademicProgram;
import edu.dosw.sirha.model.entities.AcademicPlan;
import edu.dosw.sirha.model.entities.Deanery;
import edu.dosw.sirha.model.persistence.repository.AcademicProgramRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AcademicProgramService.
 * Validates all business logic for academic program operations including creation,
 * modification, deletion, and retrieval of university careers.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AcademicProgramService Tests")
class AcademicProgramServiceTest {

    @Mock
    private AcademicProgramRepository academicProgramRepository;

    @InjectMocks
    private AcademicProgramService academicProgramService;

    private AcademicProgram testProgram;
    private Deanery testDeanery;
    private List<AcademicPlan> testPlans;

    /**
     * Sets up test data before each test execution.
     * Creates sample academic program with associated deanery and plans.
     */
    @BeforeEach
    void setUp() {
        testDeanery = new Deanery();
        testDeanery.setId("deanery123");
        testDeanery.setDeaneryName("Decanatura de Ingeniería");

        AcademicPlan plan1 = new AcademicPlan();
        plan1.setId("plan1");
        plan1.setName("Plan 14");

        AcademicPlan plan2 = new AcademicPlan();
        plan2.setId("plan2");
        plan2.setName("Plan 15");

        testPlans = Arrays.asList(plan1, plan2);

        testProgram = new AcademicProgram();
        testProgram.setId("program123");
        testProgram.setName("Ingeniería de Sistemas");
        testProgram.setDeanery(testDeanery);
        testProgram.setPlans(testPlans);
    }

    @Nested
    @DisplayName("createProgram() Tests")
    class CreateProgramTests {

        /**
         * Tests successful creation of a new academic program with valid data.
         */
        @Test
        @DisplayName("Should create program successfully when valid program is provided")
        void createProgram_ValidProgram_ShouldReturnCreatedProgram() {
            AcademicProgram newProgram = new AcademicProgram();
            newProgram.setName("Ingeniería Civil");
            newProgram.setDeanery(testDeanery);

            when(academicProgramRepository.findByName("Ingeniería Civil")).thenReturn(Optional.empty());
            when(academicProgramRepository.save(any(AcademicProgram.class))).thenReturn(testProgram);

            AcademicProgram result = academicProgramService.createProgram(newProgram);

            assertNotNull(result);
            assertEquals(testProgram.getId(), result.getId());
            verify(academicProgramRepository).findByName("Ingeniería Civil");
            verify(academicProgramRepository).save(newProgram);
        }

        /**
         * Tests creation of a program when name is null, which should be allowed.
         */
        @Test
        @DisplayName("Should create program successfully when program has no name")
        void createProgram_ProgramWithNoName_ShouldReturnCreatedProgram() {
            AcademicProgram newProgram = new AcademicProgram();
            newProgram.setDeanery(testDeanery);

            when(academicProgramRepository.save(any(AcademicProgram.class))).thenReturn(testProgram);

            AcademicProgram result = academicProgramService.createProgram(newProgram);

            assertNotNull(result);
            assertEquals(testProgram.getId(), result.getId());
            verify(academicProgramRepository, never()).findByName(anyString());
            verify(academicProgramRepository).save(newProgram);
        }

        /**
         * Tests exception handling when attempting to create a null program.
         */
        @Test
        @DisplayName("Should throw exception when program is null")
        void createProgram_NullProgram_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicProgramService.createProgram(null)
            );

            assertEquals("El programa académico no puede ser nulo", exception.getMessage());
            verify(academicProgramRepository, never()).save(any());
        }

        /**
         * Tests exception handling when attempting to create a program with duplicate name.
         */
        @Test
        @DisplayName("Should throw exception when program with same name already exists")
        void createProgram_DuplicateName_ShouldThrowException() {
            AcademicProgram newProgram = new AcademicProgram();
            newProgram.setName("Ingeniería de Sistemas");

            when(academicProgramRepository.findByName("Ingeniería de Sistemas")).thenReturn(Optional.of(testProgram));

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicProgramService.createProgram(newProgram)
            );

            assertEquals("El programa académico con nombre 'Ingeniería de Sistemas' ya existe", exception.getMessage());
            verify(academicProgramRepository).findByName("Ingeniería de Sistemas");
            verify(academicProgramRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("modifyProgram() Tests")
    class ModifyProgramTests {

        /**
         * Tests successful modification of an existing academic program.
         */
        @Test
        @DisplayName("Should modify program successfully when valid program is provided")
        void modifyProgram_ValidProgram_ShouldReturnUpdatedProgram() {
            when(academicProgramRepository.existsById("program123")).thenReturn(true);
            when(academicProgramRepository.save(any(AcademicProgram.class))).thenReturn(testProgram);

            AcademicProgram result = academicProgramService.modifyProgram(testProgram);

            assertNotNull(result);
            assertEquals(testProgram.getId(), result.getId());
            verify(academicProgramRepository).existsById("program123");
            verify(academicProgramRepository).save(testProgram);
        }

        /**
         * Tests exception handling when attempting to modify a null program.
         */
        @Test
        @DisplayName("Should throw exception when program is null")
        void modifyProgram_NullProgram_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicProgramService.modifyProgram(null)
            );

            assertEquals("El programa académico no puede ser nulo", exception.getMessage());
            verify(academicProgramRepository, never()).existsById(anyString());
            verify(academicProgramRepository, never()).save(any());
        }

        /**
         * Tests exception handling when attempting to modify a program with null ID.
         */
        @Test
        @DisplayName("Should throw exception when program ID is null")
        void modifyProgram_NullId_ShouldThrowException() {
            AcademicProgram programWithoutId = new AcademicProgram();
            programWithoutId.setName("Ingeniería de Sistemas");

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicProgramService.modifyProgram(programWithoutId)
            );

            assertEquals("El programa académico con ID 'null' no existe", exception.getMessage());
            verify(academicProgramRepository, never()).existsById(anyString());
            verify(academicProgramRepository, never()).save(any());
        }

        /**
         * Tests exception handling when attempting to modify a non-existent program.
         */
        @Test
        @DisplayName("Should throw exception when program doesn't exist")
        void modifyProgram_NonExistentProgram_ShouldThrowException() {
            when(academicProgramRepository.existsById("program123")).thenReturn(false);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicProgramService.modifyProgram(testProgram)
            );

            assertEquals("El programa académico con ID 'program123' no existe", exception.getMessage());
            verify(academicProgramRepository).existsById("program123");
            verify(academicProgramRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteProgram() Tests")
    class DeleteProgramTests {

        /**
         * Tests successful deletion of an academic program without associated plans.
         */
        @Test
        @DisplayName("Should delete program successfully when program has no associated plans")
        void deleteProgram_ProgramWithoutPlans_ShouldDeleteSuccessfully() {
            AcademicProgram programWithoutPlans = new AcademicProgram();
            programWithoutPlans.setId("program123");
            programWithoutPlans.setName("Ingeniería de Sistemas");
            programWithoutPlans.setPlans(null);

            when(academicProgramRepository.findById("program123")).thenReturn(Optional.of(programWithoutPlans));

            academicProgramService.deleteProgram("program123");

            verify(academicProgramRepository).findById("program123");
            verify(academicProgramRepository).deleteById("program123");
        }

        /**
         * Tests successful deletion of an academic program with empty plans list.
         */
        @Test
        @DisplayName("Should delete program successfully when program has empty plans list")
        void deleteProgram_ProgramWithEmptyPlans_ShouldDeleteSuccessfully() {
            AcademicProgram programWithEmptyPlans = new AcademicProgram();
            programWithEmptyPlans.setId("program123");
            programWithEmptyPlans.setName("Ingeniería de Sistemas");
            programWithEmptyPlans.setPlans(Arrays.asList());

            when(academicProgramRepository.findById("program123")).thenReturn(Optional.of(programWithEmptyPlans));

            academicProgramService.deleteProgram("program123");

            verify(academicProgramRepository).findById("program123");
            verify(academicProgramRepository).deleteById("program123");
        }

        /**
         * Tests exception handling when attempting to delete a program with null ID.
         */
        @Test
        @DisplayName("Should throw exception when ID is null")
        void deleteProgram_NullId_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicProgramService.deleteProgram(null)
            );

            assertEquals("El ID del programa no puede ser nulo o vacío", exception.getMessage());
            verify(academicProgramRepository, never()).findById(anyString());
            verify(academicProgramRepository, never()).deleteById(anyString());
        }

        /**
         * Tests exception handling when attempting to delete a program with empty ID.
         */
        @Test
        @DisplayName("Should throw exception when ID is empty")
        void deleteProgram_EmptyId_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicProgramService.deleteProgram("   ")
            );

            assertEquals("El ID del programa no puede ser nulo o vacío", exception.getMessage());
            verify(academicProgramRepository, never()).findById(anyString());
            verify(academicProgramRepository, never()).deleteById(anyString());
        }

        /**
         * Tests exception handling when attempting to delete a non-existent program.
         */
        @Test
        @DisplayName("Should throw exception when program doesn't exist")
        void deleteProgram_NonExistentProgram_ShouldThrowException() {
            when(academicProgramRepository.findById("program123")).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicProgramService.deleteProgram("program123")
            );

            assertEquals("El programa académico con ID 'program123' no existe", exception.getMessage());
            verify(academicProgramRepository).findById("program123");
            verify(academicProgramRepository, never()).deleteById(anyString());
        }

        /**
         * Tests exception handling when attempting to delete a program with associated plans.
         */
        @Test
        @DisplayName("Should throw exception when program has associated plans")
        void deleteProgram_ProgramWithAssociatedPlans_ShouldThrowException() {
            when(academicProgramRepository.findById("program123")).thenReturn(Optional.of(testProgram));

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicProgramService.deleteProgram("program123")
            );

            assertEquals("No se puede eliminar el programa 'Ingeniería de Sistemas' porque tiene 2 planes académicos asociados", 
                        exception.getMessage());
            verify(academicProgramRepository).findById("program123");
            verify(academicProgramRepository, never()).deleteById(anyString());
        }
    }

    @Nested
    @DisplayName("searchProgramById() Tests")
    class SearchProgramByIdTests {

        /**
         * Tests successful retrieval of an academic program by valid ID.
         */
        @Test
        @DisplayName("Should return program when valid ID is provided")
        void searchProgramById_ValidId_ShouldReturnProgram() {
            when(academicProgramRepository.findById("program123")).thenReturn(Optional.of(testProgram));

            AcademicProgram result = academicProgramService.searchProgramById("program123");

            assertNotNull(result);
            assertEquals(testProgram.getId(), result.getId());
            assertEquals(testProgram.getName(), result.getName());
            verify(academicProgramRepository).findById("program123");
        }

        /**
         * Tests exception handling when searching with null ID.
         */
        @Test
        @DisplayName("Should throw exception when ID is null")
        void searchProgramById_NullId_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicProgramService.searchProgramById(null)
            );

            assertEquals("El ID del programa no puede ser nulo o vacío", exception.getMessage());
            verify(academicProgramRepository, never()).findById(anyString());
        }

        /**
         * Tests exception handling when searching with empty ID.
         */
        @Test
        @DisplayName("Should throw exception when ID is empty")
        void searchProgramById_EmptyId_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicProgramService.searchProgramById("  ")
            );

            assertEquals("El ID del programa no puede ser nulo o vacío", exception.getMessage());
            verify(academicProgramRepository, never()).findById(anyString());
        }

        /**
         * Tests exception handling when program is not found by ID.
         */
        @Test
        @DisplayName("Should throw exception when program is not found")
        void searchProgramById_NotFound_ShouldThrowException() {
            when(academicProgramRepository.findById("program123")).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicProgramService.searchProgramById("program123")
            );

            assertEquals("El programa académico con ID 'program123' no fue encontrado", exception.getMessage());
            verify(academicProgramRepository).findById("program123");
        }
    }

    @Nested
    @DisplayName("searchAllPrograms() Tests")
    class SearchAllProgramsTests {

        /**
         * Tests successful retrieval of all academic programs.
         */
        @Test
        @DisplayName("Should return all programs when programs exist")
        void searchAllPrograms_ProgramsExist_ShouldReturnAllPrograms() {
            AcademicProgram program2 = new AcademicProgram();
            program2.setId("program456");
            program2.setName("Ingeniería Civil");

            List<AcademicProgram> expectedPrograms = Arrays.asList(testProgram, program2);
            when(academicProgramRepository.findAll()).thenReturn(expectedPrograms);

            List<AcademicProgram> result = academicProgramService.searchAllPrograms();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains(testProgram));
            assertTrue(result.contains(program2));
            verify(academicProgramRepository).findAll();
        }

        /**
         * Tests retrieval when no programs exist in the database.
         */
        @Test
        @DisplayName("Should return empty list when no programs exist")
        void searchAllPrograms_NoProgramsExist_ShouldReturnEmptyList() {
            when(academicProgramRepository.findAll()).thenReturn(Arrays.asList());

            List<AcademicProgram> result = academicProgramService.searchAllPrograms();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(academicProgramRepository).findAll();
        }
    }

    @Nested
    @DisplayName("searchProgramByName() Tests")
    class SearchProgramByNameTests {

        /**
         * Tests successful retrieval of an academic program by valid name.
         */
        @Test
        @DisplayName("Should return program when valid name is provided")
        void searchProgramByName_ValidName_ShouldReturnProgram() {
            when(academicProgramRepository.findByName("Ingeniería de Sistemas")).thenReturn(Optional.of(testProgram));

            AcademicProgram result = academicProgramService.searchProgramByName("Ingeniería de Sistemas");

            assertNotNull(result);
            assertEquals(testProgram.getName(), result.getName());
            assertEquals(testProgram.getId(), result.getId());
            verify(academicProgramRepository).findByName("Ingeniería de Sistemas");
        }

        /**
         * Tests exception handling when searching with null name.
         */
        @Test
        @DisplayName("Should throw exception when name is null")
        void searchProgramByName_NullName_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicProgramService.searchProgramByName(null)
            );

            assertEquals("El nombre del programa no puede ser nulo o vacío", exception.getMessage());
            verify(academicProgramRepository, never()).findByName(anyString());
        }

        /**
         * Tests exception handling when searching with empty name.
         */
        @Test
        @DisplayName("Should throw exception when name is empty")
        void searchProgramByName_EmptyName_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicProgramService.searchProgramByName("  ")
            );

            assertEquals("El nombre del programa no puede ser nulo o vacío", exception.getMessage());
            verify(academicProgramRepository, never()).findByName(anyString());
        }

        /**
         * Tests exception handling when program is not found by name.
         */
        @Test
        @DisplayName("Should throw exception when program is not found")
        void searchProgramByName_NotFound_ShouldThrowException() {
            when(academicProgramRepository.findByName("Ingeniería de Sistemas")).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicProgramService.searchProgramByName("Ingeniería de Sistemas")
            );

            assertEquals("El programa académico con nombre 'Ingeniería de Sistemas' no fue encontrado", exception.getMessage());
            verify(academicProgramRepository).findByName("Ingeniería de Sistemas");
        }
    }

    @Nested
    @DisplayName("countPlansInProgram() Tests")
    class CountPlansInProgramTests {

        /**
         * Tests counting plans in a program that has associated plans.
         */
        @Test
        @DisplayName("Should return correct count when program has plans")
        void countPlansInProgram_ProgramWithPlans_ShouldReturnCorrectCount() {
            when(academicProgramRepository.findById("program123")).thenReturn(Optional.of(testProgram));

            int result = academicProgramService.countPlansInProgram("program123");

            assertEquals(2, result);
            verify(academicProgramRepository).findById("program123");
        }

        /**
         * Tests counting plans in a program that has no plans (null).
         */
        @Test
        @DisplayName("Should return zero when program has null plans")
        void countPlansInProgram_ProgramWithNullPlans_ShouldReturnZero() {
            AcademicProgram programWithoutPlans = new AcademicProgram();
            programWithoutPlans.setId("program123");
            programWithoutPlans.setPlans(null);

            when(academicProgramRepository.findById("program123")).thenReturn(Optional.of(programWithoutPlans));

            int result = academicProgramService.countPlansInProgram("program123");

            assertEquals(0, result);
            verify(academicProgramRepository).findById("program123");
        }

        /**
         * Tests counting plans in a program that has empty plans list.
         */
        @Test
        @DisplayName("Should return zero when program has empty plans list")
        void countPlansInProgram_ProgramWithEmptyPlans_ShouldReturnZero() {
            AcademicProgram programWithEmptyPlans = new AcademicProgram();
            programWithEmptyPlans.setId("program123");
            programWithEmptyPlans.setPlans(Arrays.asList());

            when(academicProgramRepository.findById("program123")).thenReturn(Optional.of(programWithEmptyPlans));

            int result = academicProgramService.countPlansInProgram("program123");

            assertEquals(0, result);
            verify(academicProgramRepository).findById("program123");
        }

        /**
         * Tests exception handling when program ID is invalid for count operation.
         */
        @Test
        @DisplayName("Should throw exception when program is not found")
        void countPlansInProgram_NonExistentProgram_ShouldThrowException() {
            when(academicProgramRepository.findById("program123")).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicProgramService.countPlansInProgram("program123")
            );

            assertEquals("El programa académico con ID 'program123' no fue encontrado", exception.getMessage());
            verify(academicProgramRepository).findById("program123");
        }
    }

    @Nested
    @DisplayName("getCurrentPlanForProgram() Tests")
    class GetCurrentPlanForProgramTests {

        /**
         * Tests retrieval of current plan for a program that has plans.
         */
        @Test
        @DisplayName("Should return current plan when program has plans")
        void getCurrentPlanForProgram_ProgramWithPlans_ShouldReturnCurrentPlan() {
            when(academicProgramRepository.findById("program123")).thenReturn(Optional.of(testProgram));

            Optional<AcademicPlan> result = academicProgramService.getCurrentPlanForProgram("program123");

            assertTrue(result.isPresent());
            assertEquals("plan2", result.get().getId());
            assertEquals("Plan 15", result.get().getName());
            verify(academicProgramRepository).findById("program123");
        }

        /**
         * Tests retrieval of current plan for a program that has null plans.
         */
        @Test
        @DisplayName("Should return empty optional when program has null plans")
        void getCurrentPlanForProgram_ProgramWithNullPlans_ShouldReturnEmpty() {
            AcademicProgram programWithoutPlans = new AcademicProgram();
            programWithoutPlans.setId("program123");
            programWithoutPlans.setPlans(null);

            when(academicProgramRepository.findById("program123")).thenReturn(Optional.of(programWithoutPlans));

            Optional<AcademicPlan> result = academicProgramService.getCurrentPlanForProgram("program123");

            assertFalse(result.isPresent());
            verify(academicProgramRepository).findById("program123");
        }

        /**
         * Tests retrieval of current plan for a program that has empty plans list.
         */
        @Test
        @DisplayName("Should return empty optional when program has empty plans list")
        void getCurrentPlanForProgram_ProgramWithEmptyPlans_ShouldReturnEmpty() {
            AcademicProgram programWithEmptyPlans = new AcademicProgram();
            programWithEmptyPlans.setId("program123");
            programWithEmptyPlans.setPlans(Arrays.asList());

            when(academicProgramRepository.findById("program123")).thenReturn(Optional.of(programWithEmptyPlans));

            Optional<AcademicPlan> result = academicProgramService.getCurrentPlanForProgram("program123");

            assertFalse(result.isPresent());
            verify(academicProgramRepository).findById("program123");
        }

        /**
         * Tests exception handling when program is not found for current plan retrieval.
         */
        @Test
        @DisplayName("Should throw exception when program is not found")
        void getCurrentPlanForProgram_NonExistentProgram_ShouldThrowException() {
            when(academicProgramRepository.findById("program123")).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicProgramService.getCurrentPlanForProgram("program123")
            );

            assertEquals("El programa académico con ID 'program123' no fue encontrado", exception.getMessage());
            verify(academicProgramRepository).findById("program123");
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        /**
         * Tests that the service can be instantiated with repository dependency.
         */
        @Test
        @DisplayName("Should create service with repository dependency")
        void constructor_ShouldCreateServiceWithRepository() {
            AcademicProgramRepository repository = mock(AcademicProgramRepository.class);

            AcademicProgramService service = new AcademicProgramService(repository);

            assertNotNull(service);
        }
    }
}