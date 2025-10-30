package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.Deanery;
import edu.dosw.sirha.model.entities.Dean;
import edu.dosw.sirha.model.entities.Professor;
import edu.dosw.sirha.model.entities.AcademicProgram;
import edu.dosw.sirha.model.persistence.repository.DeaneryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DeaneryService.
 * Validates all business logic for deanery management including creation,
 * modification, deletion, and search operations.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DeaneryService Tests")
class DeaneryServiceTest {

    @Mock
    private DeaneryRepository deaneryRepository;

    @InjectMocks
    private DeaneryService deaneryService;

    private Deanery testDeanery;
    private Dean testDean;
    private List<Professor> testProfessors;
    private List<AcademicProgram> testPrograms;

    /**
     * Sets up test data before each test execution.
     * Creates sample deaneries with related entities.
     */
    @BeforeEach
    void setUp() {
    
        testDean = new Dean("dean123", "Dr. María González", "maria.gonzalez@escuelaing.edu.co", "12345678");
        testDean.setActive(true);

   
        Professor professor1 = new Professor("prof123", "Dr. Juan Pérez", "juan.perez@escuelaing.edu.co", "11111111");
        Professor professor2 = new Professor("prof456", "Dr. Ana Rodríguez", "ana.rodriguez@escuelaing.edu.co", "22222222");
        testProfessors = new ArrayList<>(Arrays.asList(professor1, professor2));

        AcademicProgram program1 = new AcademicProgram();
        program1.setId("program123");
        program1.setName("Ingeniería de Sistemas");

        AcademicProgram program2 = new AcademicProgram();
        program2.setId("program456");
        program2.setName("Ingeniería Civil");

        testPrograms = new ArrayList<>(Arrays.asList(program1, program2));

    
        testDeanery = new Deanery();
        testDeanery.setId("deanery123");
        testDeanery.setDeaneryName("Facultad de Ingeniería");
        testDeanery.setDean(testDean);
        testDeanery.setProfessors(testProfessors);
        testDeanery.setAcademicPrograms(testPrograms);
    }

    @Nested
    @DisplayName("createDeanery() Tests")
    class CreateDeaneryTests {

        /**
         * Tests successful creation of a deanery with valid data.
         */
        @Test
        @DisplayName("Should create deanery successfully when valid deanery is provided")
        void createDeanery_ValidDeanery_ShouldReturnCreatedDeanery() {
            when(deaneryRepository.findByDeaneryName("Facultad de Ingeniería")).thenReturn(Optional.empty());
            when(deaneryRepository.save(any(Deanery.class))).thenReturn(testDeanery);

            Deanery result = deaneryService.createDeanery(testDeanery);

            assertNotNull(result);
            assertEquals(testDeanery.getId(), result.getId());
            assertEquals(testDeanery.getDeaneryName(), result.getDeaneryName());
            verify(deaneryRepository).findByDeaneryName("Facultad de Ingeniería");
            verify(deaneryRepository).save(testDeanery);
        }

        /**
         * Tests exception handling when deanery is null.
         */
        @Test
        @DisplayName("Should throw exception when deanery is null")
        void createDeanery_NullDeanery_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.createDeanery(null)
            );

            assertEquals("Deanery cannot be null", exception.getMessage());
            verify(deaneryRepository, never()).save(any());
        }

        /**
         * Tests validation of required fields.
         */
        @Test
        @DisplayName("Should throw exception when deanery name is null")
        void createDeanery_NullDeaneryName_ShouldThrowException() {
            testDeanery.setDeaneryName(null);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.createDeanery(testDeanery)
            );

            assertEquals("Deanery name cannot be null or empty", exception.getMessage());
            verify(deaneryRepository, never()).save(any());
        }

        /**
         * Tests validation when deanery name is empty.
         */
        @Test
        @DisplayName("Should throw exception when deanery name is empty")
        void createDeanery_EmptyDeaneryName_ShouldThrowException() {
            testDeanery.setDeaneryName("   ");

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.createDeanery(testDeanery)
            );

            assertEquals("Deanery name cannot be null or empty", exception.getMessage());
            verify(deaneryRepository, never()).save(any());
        }

        /**
         * Tests validation of unique deanery name.
         */
        @Test
        @DisplayName("Should throw exception when deanery name already exists")
        void createDeanery_DuplicateName_ShouldThrowException() {
            when(deaneryRepository.findByDeaneryName("Facultad de Ingeniería")).thenReturn(Optional.of(testDeanery));

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.createDeanery(testDeanery)
            );

            assertEquals("Deanery with name 'Facultad de Ingeniería' already exists", exception.getMessage());
            verify(deaneryRepository).findByDeaneryName("Facultad de Ingeniería");
            verify(deaneryRepository, never()).save(any());
        }

        /**
         * Tests creation with minimal required data.
         */
        @Test
        @DisplayName("Should create deanery with only name when minimal data is provided")
        void createDeanery_MinimalData_ShouldCreateSuccessfully() {
            Deanery minimalDeanery = new Deanery();
            minimalDeanery.setDeaneryName("Nueva Facultad");

            when(deaneryRepository.findByDeaneryName("Nueva Facultad")).thenReturn(Optional.empty());
            when(deaneryRepository.save(any(Deanery.class))).thenReturn(minimalDeanery);

            Deanery result = deaneryService.createDeanery(minimalDeanery);

            assertNotNull(result);
            assertEquals("Nueva Facultad", result.getDeaneryName());
            verify(deaneryRepository).save(minimalDeanery);
        }
    }

    @Nested
    @DisplayName("modifyDeanery() Tests")
    class ModifyDeaneryTests {

        /**
         * Tests successful modification of a deanery.
         */
        @Test
        @DisplayName("Should modify deanery successfully when valid deanery is provided")
        void modifyDeanery_ValidDeanery_ShouldReturnModifiedDeanery() {
            when(deaneryRepository.existsById("deanery123")).thenReturn(true);
            when(deaneryRepository.save(any(Deanery.class))).thenReturn(testDeanery);

            Deanery result = deaneryService.modifyDeanery(testDeanery);

            assertNotNull(result);
            assertEquals(testDeanery.getId(), result.getId());
            assertEquals(testDeanery.getDeaneryName(), result.getDeaneryName());
            verify(deaneryRepository).existsById("deanery123");
            verify(deaneryRepository).save(testDeanery);
        }

        /**
         * Tests exception when deanery is null.
         */
        @Test
        @DisplayName("Should throw exception when deanery is null")
        void modifyDeanery_NullDeanery_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.modifyDeanery(null)
            );

            assertEquals("Deanery cannot be null", exception.getMessage());
            verify(deaneryRepository, never()).save(any());
        }

        /**
         * Tests exception when deanery ID is null.
         */
        @Test
        @DisplayName("Should throw exception when deanery ID is null")
        void modifyDeanery_NullId_ShouldThrowException() {
            testDeanery.setId(null);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.modifyDeanery(testDeanery)
            );

            assertEquals("Deanery ID cannot be null or empty for modification", exception.getMessage());
            verify(deaneryRepository, never()).save(any());
        }

        /**
         * Tests exception when deanery ID is empty.
         */
        @Test
        @DisplayName("Should throw exception when deanery ID is empty")
        void modifyDeanery_EmptyId_ShouldThrowException() {
            testDeanery.setId("   ");

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.modifyDeanery(testDeanery)
            );

            assertEquals("Deanery ID cannot be null or empty for modification", exception.getMessage());
            verify(deaneryRepository, never()).save(any());
        }

        /**
         * Tests exception when deanery name is null.
         */
        @Test
        @DisplayName("Should throw exception when deanery name is null")
        void modifyDeanery_NullName_ShouldThrowException() {
            testDeanery.setDeaneryName(null);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.modifyDeanery(testDeanery)
            );

            assertEquals("Deanery name cannot be null or empty", exception.getMessage());
            verify(deaneryRepository, never()).save(any());
        }

        /**
         * Tests exception when deanery doesn't exist.
         */
        @Test
        @DisplayName("Should throw exception when deanery doesn't exist")
        void modifyDeanery_NonExistentDeanery_ShouldThrowException() {
            when(deaneryRepository.existsById("deanery123")).thenReturn(false);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.modifyDeanery(testDeanery)
            );

            assertEquals("Deanery with ID 'deanery123' does not exist", exception.getMessage());
            verify(deaneryRepository).existsById("deanery123");
            verify(deaneryRepository, never()).save(any());
        }

        /**
         * Tests modification with updated data.
         */
        @Test
        @DisplayName("Should modify deanery with updated data successfully")
        void modifyDeanery_UpdatedData_ShouldModifySuccessfully() {
            testDeanery.setDeaneryName("Facultad de Ingeniería Actualizada");

            when(deaneryRepository.existsById("deanery123")).thenReturn(true);
            when(deaneryRepository.save(any(Deanery.class))).thenReturn(testDeanery);

            Deanery result = deaneryService.modifyDeanery(testDeanery);

            assertNotNull(result);
            assertEquals("Facultad de Ingeniería Actualizada", result.getDeaneryName());
            verify(deaneryRepository).save(testDeanery);
        }
    }

    @Nested
    @DisplayName("deleteDeanery() Tests")
    class DeleteDeaneryTests {

        /**
         * Tests successful deletion of a deanery.
         */
        @Test
        @DisplayName("Should delete deanery successfully when valid ID is provided")
        void deleteDeanery_ValidId_ShouldDeleteSuccessfully() {
            when(deaneryRepository.existsById("deanery123")).thenReturn(true);

            assertDoesNotThrow(() -> deaneryService.deleteDeanery("deanery123"));

            verify(deaneryRepository).existsById("deanery123");
            verify(deaneryRepository).deleteById("deanery123");
        }

        /**
         * Tests exception when ID is null.
         */
        @Test
        @DisplayName("Should throw exception when ID is null")
        void deleteDeanery_NullId_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.deleteDeanery(null)
            );

            assertEquals("Deanery ID cannot be null or empty", exception.getMessage());
            verify(deaneryRepository, never()).deleteById(anyString());
        }

        /**
         * Tests exception when ID is empty.
         */
        @Test
        @DisplayName("Should throw exception when ID is empty")
        void deleteDeanery_EmptyId_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.deleteDeanery("   ")
            );

            assertEquals("Deanery ID cannot be null or empty", exception.getMessage());
            verify(deaneryRepository, never()).deleteById(anyString());
        }

        /**
         * Tests exception when deanery doesn't exist.
         */
        @Test
        @DisplayName("Should throw exception when deanery doesn't exist")
        void deleteDeanery_NonExistentDeanery_ShouldThrowException() {
            when(deaneryRepository.existsById("nonexistent123")).thenReturn(false);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.deleteDeanery("nonexistent123")
            );

            assertEquals("Deanery with ID 'nonexistent123' does not exist", exception.getMessage());
            verify(deaneryRepository).existsById("nonexistent123");
            verify(deaneryRepository, never()).deleteById(anyString());
        }
    }

    @Nested
    @DisplayName("searchDeaneryById() Tests")
    class SearchDeaneryByIdTests {

        /**
         * Tests successful retrieval of deanery by ID.
         */
        @Test
        @DisplayName("Should return deanery when valid ID is provided")
        void searchDeaneryById_ValidId_ShouldReturnDeanery() {
            when(deaneryRepository.findDeaneryById("deanery123")).thenReturn(Optional.of(testDeanery));

            Deanery result = deaneryService.searchDeaneryById("deanery123");

            assertNotNull(result);
            assertEquals(testDeanery.getId(), result.getId());
            assertEquals(testDeanery.getDeaneryName(), result.getDeaneryName());
            verify(deaneryRepository).findDeaneryById("deanery123");
        }

        /**
         * Tests exception when ID is null.
         */
        @Test
        @DisplayName("Should throw exception when ID is null")
        void searchDeaneryById_NullId_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.searchDeaneryById(null)
            );

            assertEquals("Deanery ID cannot be null or empty", exception.getMessage());
            verify(deaneryRepository, never()).findDeaneryById(anyString());
        }

        /**
         * Tests exception when ID is empty.
         */
        @Test
        @DisplayName("Should throw exception when ID is empty")
        void searchDeaneryById_EmptyId_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.searchDeaneryById("   ")
            );

            assertEquals("Deanery ID cannot be null or empty", exception.getMessage());
            verify(deaneryRepository, never()).findDeaneryById(anyString());
        }

        /**
         * Tests exception when deanery is not found.
         */
        @Test
        @DisplayName("Should throw exception when deanery is not found")
        void searchDeaneryById_NotFound_ShouldThrowException() {
            when(deaneryRepository.findDeaneryById("nonexistent123")).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.searchDeaneryById("nonexistent123")
            );

            assertEquals("Deanery with ID 'nonexistent123' not found", exception.getMessage());
            verify(deaneryRepository).findDeaneryById("nonexistent123");
        }
    }

    @Nested
    @DisplayName("searchDeaneryByName() Tests")
    class SearchDeaneryByNameTests {

        /**
         * Tests successful retrieval of deanery by name.
         */
        @Test
        @DisplayName("Should return deanery when valid name is provided")
        void searchDeaneryByName_ValidName_ShouldReturnDeanery() {
            when(deaneryRepository.findByDeaneryName("Facultad de Ingeniería")).thenReturn(Optional.of(testDeanery));

            Deanery result = deaneryService.searchDeaneryByName("Facultad de Ingeniería");

            assertNotNull(result);
            assertEquals(testDeanery.getDeaneryName(), result.getDeaneryName());
            assertEquals(testDeanery.getId(), result.getId());
            verify(deaneryRepository).findByDeaneryName("Facultad de Ingeniería");
        }

        /**
         * Tests exception when name is null.
         */
        @Test
        @DisplayName("Should throw exception when name is null")
        void searchDeaneryByName_NullName_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.searchDeaneryByName(null)
            );

            assertEquals("Deanery name cannot be null or empty", exception.getMessage());
            verify(deaneryRepository, never()).findByDeaneryName(anyString());
        }

        /**
         * Tests exception when name is empty.
         */
        @Test
        @DisplayName("Should throw exception when name is empty")
        void searchDeaneryByName_EmptyName_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.searchDeaneryByName("   ")
            );

            assertEquals("Deanery name cannot be null or empty", exception.getMessage());
            verify(deaneryRepository, never()).findByDeaneryName(anyString());
        }

        /**
         * Tests exception when deanery is not found.
         */
        @Test
        @DisplayName("Should throw exception when deanery is not found")
        void searchDeaneryByName_NotFound_ShouldThrowException() {
            when(deaneryRepository.findByDeaneryName("Facultad No Existente")).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.searchDeaneryByName("Facultad No Existente")
            );

            assertEquals("Deanery with name 'Facultad No Existente' not found", exception.getMessage());
            verify(deaneryRepository).findByDeaneryName("Facultad No Existente");
        }

        /**
         * Tests search with trimmed name.
         */
        @Test
        @DisplayName("Should search deanery by name with trimmed spaces")
        void searchDeaneryByName_WithSpaces_ShouldTrimAndSearch() {
            when(deaneryRepository.findByDeaneryName("Facultad de Ingeniería")).thenReturn(Optional.of(testDeanery));

            Deanery result = deaneryService.searchDeaneryByName("Facultad de Ingeniería");

            assertNotNull(result);
            assertEquals(testDeanery.getDeaneryName(), result.getDeaneryName());
            verify(deaneryRepository).findByDeaneryName("Facultad de Ingeniería");
        }
    }

    @Nested
    @DisplayName("searchAllDeaneries() Tests")
    class SearchAllDeaneriesTests {

        /**
         * Tests retrieval of all deaneries.
         */
        @Test
        @DisplayName("Should return all deaneries")
        void searchAllDeaneries_ShouldReturnAllDeaneries() {
            Deanery anotherDeanery = new Deanery();
            anotherDeanery.setId("deanery456");
            anotherDeanery.setDeaneryName("Facultad de Ciencias");

            List<Deanery> expectedDeaneries = Arrays.asList(testDeanery, anotherDeanery);
            when(deaneryRepository.findAll()).thenReturn(expectedDeaneries);

            List<Deanery> result = deaneryService.searchAllDeaneries();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains(testDeanery));
            assertTrue(result.contains(anotherDeanery));
            verify(deaneryRepository).findAll();
        }

        /**
         * Tests retrieval when no deaneries exist.
         */
        @Test
        @DisplayName("Should return empty list when no deaneries exist")
        void searchAllDeaneries_NoDeaneries_ShouldReturnEmptyList() {
            when(deaneryRepository.findAll()).thenReturn(new ArrayList<>());

            List<Deanery> result = deaneryService.searchAllDeaneries();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(deaneryRepository).findAll();
        }

        /**
         * Tests retrieval with single deanery.
         */
        @Test
        @DisplayName("Should return single deanery when only one exists")
        void searchAllDeaneries_SingleDeanery_ShouldReturnSingleItem() {
            List<Deanery> expectedDeaneries = Arrays.asList(testDeanery);
            when(deaneryRepository.findAll()).thenReturn(expectedDeaneries);

            List<Deanery> result = deaneryService.searchAllDeaneries();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testDeanery, result.get(0));
            verify(deaneryRepository).findAll();
        }
    }

    @Nested
    @DisplayName("existsById() Tests")
    class ExistsByIdTests {

        /**
         * Tests existence check when deanery exists.
         */
        @Test
        @DisplayName("Should return true when deanery exists")
        void existsById_ExistingDeanery_ShouldReturnTrue() {
            when(deaneryRepository.existsById("deanery123")).thenReturn(true);

            boolean result = deaneryService.existsById("deanery123");

            assertTrue(result);
            verify(deaneryRepository).existsById("deanery123");
        }

        /**
         * Tests existence check when deanery doesn't exist.
         */
        @Test
        @DisplayName("Should return false when deanery doesn't exist")
        void existsById_NonExistentDeanery_ShouldReturnFalse() {
            when(deaneryRepository.existsById("nonexistent123")).thenReturn(false);

            boolean result = deaneryService.existsById("nonexistent123");

            assertFalse(result);
            verify(deaneryRepository).existsById("nonexistent123");
        }

        /**
         * Tests exception when ID is null.
         */
        @Test
        @DisplayName("Should throw exception when ID is null")
        void existsById_NullId_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.existsById(null)
            );

            assertEquals("Deanery ID cannot be null or empty", exception.getMessage());
            verify(deaneryRepository, never()).existsById(anyString());
        }

        /**
         * Tests exception when ID is empty.
         */
        @Test
        @DisplayName("Should throw exception when ID is empty")
        void existsById_EmptyId_ShouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.existsById("   ")
            );

            assertEquals("Deanery ID cannot be null or empty", exception.getMessage());
            verify(deaneryRepository, never()).existsById(anyString());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCasesTests {

        /**
         * Tests behavior with special characters in name.
         */
        @Test
        @DisplayName("Should handle special characters in deanery name")
        void createDeanery_SpecialCharacters_ShouldHandleCorrectly() {
            testDeanery.setDeaneryName("Facultad de Ing. & Ciencias");

            when(deaneryRepository.findByDeaneryName("Facultad de Ing. & Ciencias")).thenReturn(Optional.empty());
            when(deaneryRepository.save(any(Deanery.class))).thenReturn(testDeanery);

            Deanery result = deaneryService.createDeanery(testDeanery);

            assertNotNull(result);
            assertEquals("Facultad de Ing. & Ciencias", result.getDeaneryName());
        }

        /**
         * Tests behavior with very long names.
         */
        @Test
        @DisplayName("Should handle long deanery names")
        void createDeanery_LongName_ShouldHandleCorrectly() {
            String longName = "Facultad de Ingeniería de Sistemas y Computación con Énfasis en Desarrollo de Software";
            testDeanery.setDeaneryName(longName);

            when(deaneryRepository.findByDeaneryName(longName)).thenReturn(Optional.empty());
            when(deaneryRepository.save(any(Deanery.class))).thenReturn(testDeanery);

            Deanery result = deaneryService.createDeanery(testDeanery);

            assertNotNull(result);
            assertEquals(longName, result.getDeaneryName());
        }

        /**
         * Tests case sensitivity in name search.
         */
        @Test
        @DisplayName("Should handle case sensitivity in name search")
        void searchDeaneryByName_CaseSensitive_ShouldSearchExactCase() {
            when(deaneryRepository.findByDeaneryName("facultad de ingeniería")).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deaneryService.searchDeaneryByName("facultad de ingeniería")
            );

            assertEquals("Deanery with name 'facultad de ingeniería' not found", exception.getMessage());
            verify(deaneryRepository).findByDeaneryName("facultad de ingeniería");
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
            DeaneryRepository repository = mock(DeaneryRepository.class);

            DeaneryService service = new DeaneryService(repository);

            assertNotNull(service);
        }
    }
}