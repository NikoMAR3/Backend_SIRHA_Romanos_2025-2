package edu.dosw.sirha.model.services;

import edu.dosw.sirha.controller.dtos.UserDTO;
import edu.dosw.sirha.model.entities.Dean;
import edu.dosw.sirha.model.entities.Deanery;
import edu.dosw.sirha.model.entities.UserType;
import edu.dosw.sirha.model.persistence.repository.DeanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
 * Unit tests for DeanService.
 * Validates all business logic for dean management including creation,
 * modification, deletion, and search operations.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DeanService Tests")
class DeanServiceTest {

    @Mock
    private DeanRepository deanRepository;

    @InjectMocks
    private DeanService deanService;

    private Dean testDean;
    private UserDTO testUserDTO;
    private Deanery testDeanery;

    /**
     * Sets up test data before each test execution.
     * Creates sample deans, DTOs, and related entities.
     */
    @BeforeEach
    void setUp() {
        
        testUserDTO = new UserDTO();
        testUserDTO.setName("Dr. María González");
        testUserDTO.setMail("maria.gonzalez@escuelaing.edu.co");
        testUserDTO.setDocument("12345678");

      
        testDeanery = new Deanery();
        testDeanery.setId("deanery123");
        testDeanery.setDeaneryName("Facultad de Ingeniería");

    
        testDean = new Dean("dean123", "Dr. María González", "maria.gonzalez@escuelaing.edu.co", "12345678");
        testDean.setActive(true);
        testDean.setDeanery(testDeanery);
    }

    @Nested
    @DisplayName("createDean() Tests")
    class CreateDeanTests {

        /**
         * Tests successful creation of a dean with valid UserDTO.
         */
        @Test
        @DisplayName("Should create dean successfully when valid UserDTO is provided")
        void createDean_ValidUserDTO_ShouldReturnCreatedDean() {
            when(deanRepository.save(any(Dean.class))).thenReturn(testDean);

            Dean result = deanService.createDean(testUserDTO);

            assertNotNull(result);
            assertEquals(testUserDTO.getName(), result.getName());
            assertEquals(testUserDTO.getMail(), result.getMail());
            assertEquals(testUserDTO.getDocument(), result.getDocument());
            assertEquals(UserType.DEAN, result.getType());


            ArgumentCaptor<Dean> deanCaptor = ArgumentCaptor.forClass(Dean.class);
            verify(deanRepository).save(deanCaptor.capture());
            
            Dean savedDean = deanCaptor.getValue();
            assertNotNull(savedDean.getId());
            assertEquals(testUserDTO.getName(), savedDean.getName());
            assertEquals(testUserDTO.getMail(), savedDean.getMail());
            assertEquals(testUserDTO.getDocument(), savedDean.getDocument());
            assertEquals(UserType.DEAN, savedDean.getType());
        }

        /**
         * Tests that UUID is generated automatically.
         */
        @Test
        @DisplayName("Should generate UUID automatically when creating dean")
        void createDean_ShouldGenerateUUID() {
            when(deanRepository.save(any(Dean.class))).thenReturn(testDean);

            Dean result = deanService.createDean(testUserDTO);

            ArgumentCaptor<Dean> deanCaptor = ArgumentCaptor.forClass(Dean.class);
            verify(deanRepository).save(deanCaptor.capture());
            
            Dean savedDean = deanCaptor.getValue();
            assertNotNull(savedDean.getId());
            assertFalse(savedDean.getId().isEmpty());
           
            assertTrue(savedDean.getId().matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        }

        /**
         * Tests exception handling when UserDTO is null.
         */
        @Test
        @DisplayName("Should throw exception when UserDTO is null")
        void createDean_NullUserDTO_ShouldThrowException() {
            assertThrows(
                NullPointerException.class,
                () -> deanService.createDean(null)
            );

            verify(deanRepository, never()).save(any());
        }

        /**
         * Tests creation with minimal valid data.
         */
        @Test
        @DisplayName("Should create dean with minimal required data")
        void createDean_MinimalData_ShouldCreateSuccessfully() {
            UserDTO minimalDTO = new UserDTO();
            minimalDTO.setName("Dr. Juan Pérez");
            minimalDTO.setMail("juan.perez@escuelaing.edu.co");
            minimalDTO.setDocument("87654321");

            Dean expectedDean = new Dean("generated-id", "Dr. Juan Pérez", "juan.perez@escuelaing.edu.co", "87654321");
            when(deanRepository.save(any(Dean.class))).thenReturn(expectedDean);

            Dean result = deanService.createDean(minimalDTO);

            assertNotNull(result);
            assertEquals("Dr. Juan Pérez", result.getName());
            assertEquals("juan.perez@escuelaing.edu.co", result.getMail());
            assertEquals("87654321", result.getDocument());
            verify(deanRepository).save(any(Dean.class));
        }

        /**
         * Tests creation with special characters in data.
         */
        @Test
        @DisplayName("Should handle special characters in dean data")
        void createDean_SpecialCharacters_ShouldHandleCorrectly() {
            UserDTO specialDTO = new UserDTO();
            specialDTO.setName("Dr. José María Ñuñez-Valdés");
            specialDTO.setMail("jose.maria@escuelaing.edu.co");
            specialDTO.setDocument("12.345.678-9");

            Dean expectedDean = new Dean("generated-id", specialDTO.getName(), specialDTO.getMail(), specialDTO.getDocument());
            when(deanRepository.save(any(Dean.class))).thenReturn(expectedDean);

            Dean result = deanService.createDean(specialDTO);

            assertNotNull(result);
            assertEquals("Dr. José María Ñuñez-Valdés", result.getName());
            assertEquals("jose.maria@escuelaing.edu.co", result.getMail());
            assertEquals("12.345.678-9", result.getDocument());
        }
    }

    @Nested
    @DisplayName("modifyDean() Tests")
    class ModifyDeanTests {

        /**
         * Tests successful modification of an existing dean.
         */
        @Test
        @DisplayName("Should modify dean successfully when dean exists")
        void modifyDean_ExistingDean_ShouldReturnUpdatedDean() {
            UserDTO updatedDTO = new UserDTO();
            updatedDTO.setName("Dr. María González Actualizada");
            updatedDTO.setMail("maria.gonzalez.nueva@escuelaing.edu.co");
            updatedDTO.setDocument("87654321");

            Dean updatedDean = new Dean("dean123", updatedDTO.getName(), updatedDTO.getMail(), updatedDTO.getDocument());

            when(deanRepository.findById("dean123")).thenReturn(Optional.of(testDean));
            when(deanRepository.save(any(Dean.class))).thenReturn(updatedDean);

            Optional<Dean> result = deanService.modifyDean("dean123", updatedDTO);

            assertTrue(result.isPresent());
            assertEquals(updatedDTO.getName(), result.get().getName());
            assertEquals(updatedDTO.getMail(), result.get().getMail());
            assertEquals(updatedDTO.getDocument(), result.get().getDocument());
            assertEquals("dean123", result.get().getId());

            verify(deanRepository).findById("dean123");
            verify(deanRepository).save(any(Dean.class));
        }

        /**
         * Tests modification when dean doesn't exist.
         */
        @Test
        @DisplayName("Should return empty Optional when dean doesn't exist")
        void modifyDean_NonExistentDean_ShouldReturnEmptyOptional() {
            when(deanRepository.findById("nonexistent123")).thenReturn(Optional.empty());

            Optional<Dean> result = deanService.modifyDean("nonexistent123", testUserDTO);

            assertFalse(result.isPresent());
            verify(deanRepository).findById("nonexistent123");
            verify(deanRepository, never()).save(any());
        }

        /**
         * Tests modification with null ID.
         */
        @Test
        @DisplayName("Should return empty Optional when ID is null")
        void modifyDean_NullId_ShouldReturnEmptyOptional() {
            when(deanRepository.findById(null)).thenReturn(Optional.empty());

            Optional<Dean> result = deanService.modifyDean(null, testUserDTO);

            assertFalse(result.isPresent());
            verify(deanRepository, never()).save(any());
        }

        /**
         * Tests modification with null UserDTO.
         */
        @Test
        @DisplayName("Should throw exception when UserDTO is null")
        void modifyDean_NullUserDTO_ShouldThrowException() {
            when(deanRepository.findById("dean123")).thenReturn(Optional.of(testDean));

            assertThrows(
                NullPointerException.class,
                () -> deanService.modifyDean("dean123", null)
            );
        }

        /**
         * Tests that modification preserves the original ID.
         */
        @Test
        @DisplayName("Should preserve original ID during modification")
        void modifyDean_ShouldPreserveOriginalId() {
            when(deanRepository.findById("dean123")).thenReturn(Optional.of(testDean));
            when(deanRepository.save(any(Dean.class))).thenReturn(testDean);

            Optional<Dean> result = deanService.modifyDean("dean123", testUserDTO);

            assertTrue(result.isPresent());
            assertEquals("dean123", result.get().getId());

            ArgumentCaptor<Dean> deanCaptor = ArgumentCaptor.forClass(Dean.class);
            verify(deanRepository).save(deanCaptor.capture());
            
            Dean savedDean = deanCaptor.getValue();
            assertEquals("dean123", savedDean.getId());
        }

        /**
         * Tests partial modification of dean data.
         */
        @Test
        @DisplayName("Should modify only provided fields")
        void modifyDean_PartialModification_ShouldUpdateOnlyProvidedFields() {
            UserDTO partialDTO = new UserDTO();
            partialDTO.setName("Nuevo Nombre");
            partialDTO.setMail(testDean.getMail());
            partialDTO.setDocument(testDean.getDocument()); 

            when(deanRepository.findById("dean123")).thenReturn(Optional.of(testDean));
            when(deanRepository.save(any(Dean.class))).thenReturn(testDean);

            Optional<Dean> result = deanService.modifyDean("dean123", partialDTO);

            assertTrue(result.isPresent());
            
            ArgumentCaptor<Dean> deanCaptor = ArgumentCaptor.forClass(Dean.class);
            verify(deanRepository).save(deanCaptor.capture());
            
            Dean savedDean = deanCaptor.getValue();
            assertEquals("Nuevo Nombre", savedDean.getName());
            assertEquals(testDean.getMail(), savedDean.getMail());
            assertEquals(testDean.getDocument(), savedDean.getDocument());
        }
    }

    @Nested
    @DisplayName("deleteDean() Tests")
    class DeleteDeanTests {

        /**
         * Tests successful deletion of an existing dean.
         */
        @Test
        @DisplayName("Should delete dean successfully when dean exists")
        void deleteDean_ExistingDean_ShouldReturnTrue() {
            when(deanRepository.existsById("dean123")).thenReturn(true);

            boolean result = deanService.deleteDean("dean123");

            assertTrue(result);
            verify(deanRepository).existsById("dean123");
            verify(deanRepository).deleteById("dean123");
        }

        /**
         * Tests deletion when dean doesn't exist.
         */
        @Test
        @DisplayName("Should return false when dean doesn't exist")
        void deleteDean_NonExistentDean_ShouldReturnFalse() {
            when(deanRepository.existsById("nonexistent123")).thenReturn(false);

            boolean result = deanService.deleteDean("nonexistent123");

            assertFalse(result);
            verify(deanRepository).existsById("nonexistent123");
            verify(deanRepository, never()).deleteById(anyString());
        }

        /**
         * Tests deletion with null ID.
         */
        @Test
        @DisplayName("Should return false when ID is null")
        void deleteDean_NullId_ShouldReturnFalse() {
            when(deanRepository.existsById(null)).thenReturn(false);

            boolean result = deanService.deleteDean(null);

            assertFalse(result);
            verify(deanRepository, never()).deleteById(anyString());
        }

        /**
         * Tests deletion with empty ID.
         */
        @Test
        @DisplayName("Should return false when ID is empty")
        void deleteDean_EmptyId_ShouldReturnFalse() {
            when(deanRepository.existsById("")).thenReturn(false);

            boolean result = deanService.deleteDean("");

            assertFalse(result);
            verify(deanRepository).existsById("");
            verify(deanRepository, never()).deleteById(anyString());
        }

        /**
         * Tests deletion operation order.
         */
        @Test
        @DisplayName("Should check existence before attempting deletion")
        void deleteDean_ShouldCheckExistenceFirst() {
            when(deanRepository.existsById("dean123")).thenReturn(true);

            deanService.deleteDean("dean123");

            // Verificar orden de operaciones
            verify(deanRepository).existsById("dean123");
            verify(deanRepository).deleteById("dean123");
        }
    }

    @Nested
    @DisplayName("searchDeanById() Tests")
    class SearchDeanByIdTests {

        /**
         * Tests successful retrieval of dean by ID.
         */
        @Test
        @DisplayName("Should return dean when valid ID is provided")
        void searchDeanById_ValidId_ShouldReturnDean() {
            when(deanRepository.findById("dean123")).thenReturn(Optional.of(testDean));

            Dean result = deanService.searchDeanByCode("dean123");

            assertNotNull(result);
            assertEquals(testDean.getId(), result.getId());
            assertEquals(testDean.getName(), result.getName());
            assertEquals(testDean.getMail(), result.getMail());
            assertEquals(testDean.getDocument(), result.getDocument());
            verify(deanRepository).findById("dean123");
        }

        /**
         * Tests exception when dean is not found.
         */
        @Test
        @DisplayName("Should throw exception when dean is not found")
        void searchDeanById_NotFound_ShouldThrowException() {
            when(deanRepository.findById("nonexistent123")).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deanService.searchDeanByCode("nonexistent123")
            );

            assertEquals("Dean no encontrado con id: nonexistent123", exception.getMessage());
            verify(deanRepository).findById("nonexistent123");
        }

        /**
         * Tests exception with null ID.
         */
        @Test
        @DisplayName("Should throw exception when ID is null")
        void searchDeanById_NullId_ShouldThrowException() {
            when(deanRepository.findById(null)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deanService.searchDeanByCode(null)
            );

            assertEquals("Dean no encontrado con id: null", exception.getMessage());
        }

        /**
         * Tests exception with empty ID.
         */
        @Test
        @DisplayName("Should throw exception when ID is empty")
        void searchDeanById_EmptyId_ShouldThrowException() {
            when(deanRepository.findById("")).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deanService.searchDeanByCode("")
            );

            assertEquals("Dean no encontrado con id: ", exception.getMessage());
        }

        /**
         * Tests retrieval of dean with complete data.
         */
        @Test
        @DisplayName("Should return dean with all associated data")
        void searchDeanById_ShouldReturnCompleteData() {
            testDean.setDeanery(testDeanery); // Asegurar que tiene decanatura

            when(deanRepository.findById("dean123")).thenReturn(Optional.of(testDean));

            Dean result = deanService.searchDeanByCode("dean123");

            assertNotNull(result);
            assertNotNull(result.getDeanery());
            assertEquals(testDeanery.getId(), result.getDeanery().getId());
            assertEquals(testDeanery.getDeaneryName(), result.getDeanery().getDeaneryName());
        }
    }

    @Nested
    @DisplayName("searchAllDeans() Tests")
    class SearchAllDeansTests {

        /**
         * Tests retrieval of all deans.
         */
        @Test
        @DisplayName("Should return all deans")
        void searchAllDeans_ShouldReturnAllDeans() {
            Dean anotherDean = new Dean("dean456", "Dr. Carlos López", "carlos.lopez@escuelaing.edu.co", "87654321");
            List<Dean> expectedDeans = Arrays.asList(testDean, anotherDean);

            when(deanRepository.findAll()).thenReturn(expectedDeans);

            List<Dean> result = deanService.searchAllDeans();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains(testDean));
            assertTrue(result.contains(anotherDean));
            verify(deanRepository).findAll();
        }

        /**
         * Tests retrieval when no deans exist.
         */
        @Test
        @DisplayName("Should return empty list when no deans exist")
        void searchAllDeans_NoDeans_ShouldReturnEmptyList() {
            when(deanRepository.findAll()).thenReturn(new ArrayList<>());

            List<Dean> result = deanService.searchAllDeans();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(deanRepository).findAll();
        }

        /**
         * Tests retrieval with single dean.
         */
        @Test
        @DisplayName("Should return single dean when only one exists")
        void searchAllDeans_SingleDean_ShouldReturnSingleItem() {
            List<Dean> expectedDeans = Arrays.asList(testDean);
            when(deanRepository.findAll()).thenReturn(expectedDeans);

            List<Dean> result = deanService.searchAllDeans();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testDean, result.get(0));
            verify(deanRepository).findAll();
        }

    }
    
    @Nested
    @DisplayName("searchDeanByDeanery() Tests")
    class SearchDeanByDeaneryTests {

        /**
         * Tests successful retrieval of deans by deanery.
         */
        @Test
        @DisplayName("Should return deans for valid deanery ID")
        void searchDeanByDeanery_ValidDeaneryId_ShouldReturnDeans() {
            Dean anotherDean = new Dean("dean456", "Dr. Ana Martínez", "ana.martinez@escuelaing.edu.co", "11111111");
            List<Dean> expectedDeans = Arrays.asList(testDean, anotherDean);

            when(deanRepository.findByDeaneryId("deanery123")).thenReturn(expectedDeans);

            List<Dean> result = deanService.searchDeanByDeanery("deanery123");

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains(testDean));
            assertTrue(result.contains(anotherDean));
            verify(deanRepository).findByDeaneryId("deanery123");
        }

        /**
         * Tests retrieval when no deans are associated with deanery.
         */
        @Test
        @DisplayName("Should return empty list when no deans are associated with deanery")
        void searchDeanByDeanery_NoDeans_ShouldReturnEmptyList() {
            when(deanRepository.findByDeaneryId("deanery456")).thenReturn(new ArrayList<>());

            List<Dean> result = deanService.searchDeanByDeanery("deanery456");

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(deanRepository).findByDeaneryId("deanery456");
        }

        /**
         * Tests with null deanery ID.
         */
        @Test
        @DisplayName("Should handle null deanery ID gracefully")
        void searchDeanByDeanery_NullDeaneryId_ShouldReturnEmptyList() {
            when(deanRepository.findByDeaneryId(null)).thenReturn(new ArrayList<>());

            List<Dean> result = deanService.searchDeanByDeanery(null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(deanRepository).findByDeaneryId(null);
        }

        /**
         * Tests with empty deanery ID.
         */
        @Test
        @DisplayName("Should handle empty deanery ID gracefully")
        void searchDeanByDeanery_EmptyDeaneryId_ShouldReturnEmptyList() {
            when(deanRepository.findByDeaneryId("")).thenReturn(new ArrayList<>());

            List<Dean> result = deanService.searchDeanByDeanery("");

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(deanRepository).findByDeaneryId("");
        }

        /**
         * Tests retrieval of single dean for deanery.
         */
        @Test
        @DisplayName("Should return single dean when only one is associated with deanery")
        void searchDeanByDeanery_SingleDean_ShouldReturnSingleItem() {
            List<Dean> expectedDeans = Arrays.asList(testDean);
            when(deanRepository.findByDeaneryId("deanery123")).thenReturn(expectedDeans);

            List<Dean> result = deanService.searchDeanByDeanery("deanery123");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testDean, result.get(0));
            verify(deanRepository).findByDeaneryId("deanery123");
        }

        /**
         * Tests with non-existent deanery ID.
         */
        @Test
        @DisplayName("Should return empty list for non-existent deanery")
        void searchDeanByDeanery_NonExistentDeanery_ShouldReturnEmptyList() {
            when(deanRepository.findByDeaneryId("nonexistent999")).thenReturn(new ArrayList<>());

            List<Dean> result = deanService.searchDeanByDeanery("nonexistent999");

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(deanRepository).findByDeaneryId("nonexistent999");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCasesTests {

        /**
         * Tests behavior with special characters in IDs.
         */
        @Test
        @DisplayName("Should handle special characters in dean ID")
        void searchDeanById_SpecialCharacters_ShouldHandleCorrectly() {
            String specialId = "dean-123_test@domain";
            Dean specialDean = new Dean(specialId, "Dr. Test", "test@test.com", "12345");

            when(deanRepository.findById(specialId)).thenReturn(Optional.of(specialDean));

            Dean result = deanService.searchDeanByCode(specialId);

            assertNotNull(result);
            assertEquals(specialId, result.getId());
        }

        /**
         * Tests creation and immediate retrieval.
         */
        @Test
        @DisplayName("Should create and immediately find dean")
        void createAndSearch_ShouldWorkTogether() {
            when(deanRepository.save(any(Dean.class))).thenReturn(testDean);
            when(deanRepository.findById(testDean.getId())).thenReturn(Optional.of(testDean));

            Dean created = deanService.createDean(testUserDTO);
            Dean found = deanService.searchDeanByCode(created.getId());

            assertNotNull(created);
            assertNotNull(found);
            assertEquals(created.getId(), found.getId());
            assertEquals(created.getName(), found.getName());
        }

        /**
         * Tests modification and retrieval consistency.
         */
        @Test
        @DisplayName("Should modify and reflect changes when retrieved")
        void modifyAndSearch_ShouldBeConsistent() {
            UserDTO updatedDTO = new UserDTO();
            updatedDTO.setName("Nombre Actualizado");
            updatedDTO.setMail("nuevo@mail.com");
            updatedDTO.setDocument("99999999");

            Dean updatedDean = new Dean(testDean.getId(), updatedDTO.getName(), updatedDTO.getMail(), updatedDTO.getDocument());

            when(deanRepository.findById("dean123")).thenReturn(Optional.of(testDean));
            when(deanRepository.save(any(Dean.class))).thenReturn(updatedDean);

            Optional<Dean> modified = deanService.modifyDean("dean123", updatedDTO);
            assertTrue(modified.isPresent());
            assertEquals("Nombre Actualizado", modified.get().getName());
            assertEquals("nuevo@mail.com", modified.get().getMail());
            assertEquals("99999999", modified.get().getDocument());
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
            DeanRepository repository = mock(DeanRepository.class);

            DeanService service = new DeanService(repository);

            assertNotNull(service);
        }
    }
}