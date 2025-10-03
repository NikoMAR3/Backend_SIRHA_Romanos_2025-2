package edu.dosw.sirha.model.services;

import edu.dosw.sirha.controller.dtos.UserDTO;
import edu.dosw.sirha.model.entities.AcademicVicePresident;
import edu.dosw.sirha.model.entities.UserType;
import edu.dosw.sirha.model.persistence.repository.AcademicVicePresidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AcademicVicePresidentService.
 * Validates all business logic for Academic Vice President operations including creation,
 * modification, deletion, and retrieval of vice president records.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AcademicVicePresidentService Tests")
class AcademicVicePresidentServiceTest {

    @Mock
    private AcademicVicePresidentRepository academicVicePresidentRepository;

    @InjectMocks
    private AcademicVicePresidentService academicVicePresidentService;

    private UserDTO testUserDTO;
    private AcademicVicePresident testAcademicVicePresident;

    /**
     * Sets up test data before each test execution.
     * Creates sample UserDTO and AcademicVicePresident entities for testing.
     */
    @BeforeEach
    void setUp() {
        testUserDTO = new UserDTO();
        testUserDTO.setName("María García");
        testUserDTO.setMail("maria.garcia@escuelaing.edu.co");
        testUserDTO.setDocument("12345678");

        testAcademicVicePresident = new AcademicVicePresident(
                "avp123",
                "María García",
                "maria.garcia@escuelaing.edu.co",
                "12345678"
        );
    }

    @Nested
    @DisplayName("createAcademicVicePresident() Tests")
    class CreateAcademicVicePresidentTests {

        /**
         * Tests successful creation of a new Academic Vice President with valid UserDTO.
         */
        @Test
        @DisplayName("Should create Academic Vice President successfully when valid DTO is provided")
        void createAcademicVicePresident_ValidDTO_ShouldReturnCreatedEntity() {
            when(academicVicePresidentRepository.save(any(AcademicVicePresident.class))).thenReturn(testAcademicVicePresident);

            AcademicVicePresident result = academicVicePresidentService.createAcademicVicePresident(testUserDTO);

            assertNotNull(result);
            assertEquals(testAcademicVicePresident.getId(), result.getId());
            assertEquals(testAcademicVicePresident.getName(), result.getName());
            assertEquals(testAcademicVicePresident.getMail(), result.getMail());
            assertEquals(testAcademicVicePresident.getDocument(), result.getDocument());
            assertEquals(UserType.ACADEMIC_VICEPRESIDENT, result.getType());
            
            verify(academicVicePresidentRepository).save(any(AcademicVicePresident.class));
        }

        /**
         * Tests creation with DTO containing minimal required data.
         */
        @Test
        @DisplayName("Should create Academic Vice President with minimal data")
        void createAcademicVicePresident_MinimalData_ShouldReturnCreatedEntity() {
            UserDTO minimalDTO = new UserDTO();
            minimalDTO.setName("Juan Pérez");
            minimalDTO.setMail("juan.perez@escuelaing.edu.co");
            minimalDTO.setDocument("87654321");

            AcademicVicePresident expectedResult = new AcademicVicePresident(
                    "avp456",
                    "Juan Pérez",
                    "juan.perez@escuelaing.edu.co",
                    "87654321"
            );

            when(academicVicePresidentRepository.save(any(AcademicVicePresident.class))).thenReturn(expectedResult);

            AcademicVicePresident result = academicVicePresidentService.createAcademicVicePresident(minimalDTO);

            assertNotNull(result);
            assertEquals("Juan Pérez", result.getName());
            assertEquals("juan.perez@escuelaing.edu.co", result.getMail());
            assertEquals("87654321", result.getDocument());
            assertEquals(UserType.ACADEMIC_VICEPRESIDENT, result.getType());
            
            verify(academicVicePresidentRepository).save(any(AcademicVicePresident.class));
        }

        /**
         * Tests that UUID is properly generated for new entities.
         */
        @Test
        @DisplayName("Should generate UUID for new Academic Vice President")
        void createAcademicVicePresident_ShouldGenerateUUID() {
            when(academicVicePresidentRepository.save(any(AcademicVicePresident.class))).thenAnswer(invocation -> {
                AcademicVicePresident avp = invocation.getArgument(0);
                assertNotNull(avp.getId());
                assertFalse(avp.getId().isEmpty());
                return avp;
            });

            AcademicVicePresident result = academicVicePresidentService.createAcademicVicePresident(testUserDTO);

            verify(academicVicePresidentRepository).save(any(AcademicVicePresident.class));
        }

        /**
         * Tests creation with DTO containing null values.
         */
        @Test
        @DisplayName("Should handle DTO with null values")
        void createAcademicVicePresident_DTOWithNullValues_ShouldCreateEntity() {
            UserDTO dtoWithNulls = new UserDTO();
            dtoWithNulls.setName(null);
            dtoWithNulls.setMail(null);
            dtoWithNulls.setDocument(null);

            AcademicVicePresident expectedResult = new AcademicVicePresident("avp789", null, null, null);
            when(academicVicePresidentRepository.save(any(AcademicVicePresident.class))).thenReturn(expectedResult);

            AcademicVicePresident result = academicVicePresidentService.createAcademicVicePresident(dtoWithNulls);

            assertNotNull(result);
            assertNull(result.getName());
            assertNull(result.getMail());
            assertNull(result.getDocument());
            assertEquals(UserType.ACADEMIC_VICEPRESIDENT, result.getType());
            
            verify(academicVicePresidentRepository).save(any(AcademicVicePresident.class));
        }
    }

    @Nested
    @DisplayName("modifyAcademicVicePresident() Tests")
    class ModifyAcademicVicePresidentTests {

        /**
         * Tests successful modification of an existing Academic Vice President.
         */
        @Test
        @DisplayName("Should modify Academic Vice President successfully when valid ID and DTO are provided")
        void modifyAcademicVicePresident_ValidIdAndDTO_ShouldReturnUpdatedEntity() {
            UserDTO updatedDTO = new UserDTO();
            updatedDTO.setName("María García Actualizada");
            updatedDTO.setMail("maria.garcia.updated@escuelaing.edu.co");
            updatedDTO.setDocument("12345679");

            AcademicVicePresident updatedEntity = new AcademicVicePresident(
                    "avp123",
                    "María García Actualizada",
                    "maria.garcia.updated@escuelaing.edu.co",
                    "12345679"
            );

            when(academicVicePresidentRepository.findById("avp123")).thenReturn(Optional.of(testAcademicVicePresident));
            when(academicVicePresidentRepository.save(any(AcademicVicePresident.class))).thenReturn(updatedEntity);

            AcademicVicePresident result = academicVicePresidentService.modifyAcademicVicePresident("avp123", updatedDTO);

            assertNotNull(result);
            assertEquals("avp123", result.getId());
            assertEquals("María García Actualizada", result.getName());
            assertEquals("maria.garcia.updated@escuelaing.edu.co", result.getMail());
            assertEquals("12345679", result.getDocument());
            
            verify(academicVicePresidentRepository).findById("avp123");
            verify(academicVicePresidentRepository).save(any(AcademicVicePresident.class));
        }

        /**
         * Tests modification with partial data update.
         */
        @Test
        @DisplayName("Should modify Academic Vice President with partial data")
        void modifyAcademicVicePresident_PartialUpdate_ShouldReturnUpdatedEntity() {
            UserDTO partialDTO = new UserDTO();
            partialDTO.setName("Nuevo Nombre");
            partialDTO.setMail("maria.garcia@escuelaing.edu.co");
            partialDTO.setDocument("12345678");

            when(academicVicePresidentRepository.findById("avp123")).thenReturn(Optional.of(testAcademicVicePresident));
            when(academicVicePresidentRepository.save(any(AcademicVicePresident.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AcademicVicePresident result = academicVicePresidentService.modifyAcademicVicePresident("avp123", partialDTO);

            assertNotNull(result);
            assertEquals("Nuevo Nombre", result.getName());
            assertEquals("maria.garcia@escuelaing.edu.co", result.getMail());
            assertEquals("12345678", result.getDocument());
            
            verify(academicVicePresidentRepository).findById("avp123");
            verify(academicVicePresidentRepository).save(testAcademicVicePresident);
        }

        /**
         * Tests modification when entity is not found.
         */
        @Test
        @DisplayName("Should return null when Academic Vice President is not found")
        void modifyAcademicVicePresident_EntityNotFound_ShouldReturnNull() {
            when(academicVicePresidentRepository.findById("nonexistent")).thenReturn(Optional.empty());

            AcademicVicePresident result = academicVicePresidentService.modifyAcademicVicePresident("nonexistent", testUserDTO);

            assertNull(result);
            verify(academicVicePresidentRepository).findById("nonexistent");
            verify(academicVicePresidentRepository, never()).save(any());
        }

        /**
         * Tests modification with null ID.
         */
        @Test
        @DisplayName("Should handle null ID gracefully")
        void modifyAcademicVicePresident_NullId_ShouldReturnNull() {
            when(academicVicePresidentRepository.findById(null)).thenReturn(Optional.empty());

            AcademicVicePresident result = academicVicePresidentService.modifyAcademicVicePresident(null, testUserDTO);

            assertNull(result);
            verify(academicVicePresidentRepository).findById(null);
            verify(academicVicePresidentRepository, never()).save(any());
        }

        /**
         * Tests modification with empty ID.
         */
        @Test
        @DisplayName("Should handle empty ID gracefully")
        void modifyAcademicVicePresident_EmptyId_ShouldReturnNull() {
            when(academicVicePresidentRepository.findById("")).thenReturn(Optional.empty());

            AcademicVicePresident result = academicVicePresidentService.modifyAcademicVicePresident("", testUserDTO);

            assertNull(result);
            verify(academicVicePresidentRepository).findById("");
            verify(academicVicePresidentRepository, never()).save(any());
        }

        /**
         * Tests modification with DTO containing null values.
         */
        @Test
        @DisplayName("Should handle DTO with null values in modification")
        void modifyAcademicVicePresident_DTOWithNulls_ShouldUpdateWithNulls() {
            UserDTO dtoWithNulls = new UserDTO();
            dtoWithNulls.setName(null);
            dtoWithNulls.setMail(null);
            dtoWithNulls.setDocument(null);

            when(academicVicePresidentRepository.findById("avp123")).thenReturn(Optional.of(testAcademicVicePresident));
            when(academicVicePresidentRepository.save(any(AcademicVicePresident.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AcademicVicePresident result = academicVicePresidentService.modifyAcademicVicePresident("avp123", dtoWithNulls);

            assertNotNull(result);
            assertNull(result.getName());
            assertNull(result.getMail());
            assertNull(result.getDocument());
            
            verify(academicVicePresidentRepository).findById("avp123");
            verify(academicVicePresidentRepository).save(testAcademicVicePresident);
        }
    }

    @Nested
    @DisplayName("deleteAcademicVicePresident() Tests")
    class DeleteAcademicVicePresidentTests {

        /**
         * Tests successful deletion of an existing Academic Vice President.
         */
        @Test
        @DisplayName("Should delete Academic Vice President successfully when valid ID is provided")
        void deleteAcademicVicePresident_ValidId_ShouldReturnTrue() {
            when(academicVicePresidentRepository.existsById("avp123")).thenReturn(true);

            boolean result = academicVicePresidentService.deleteAcademicVicePresident("avp123");

            assertTrue(result);
            verify(academicVicePresidentRepository).existsById("avp123");
            verify(academicVicePresidentRepository).deleteById("avp123");
        }

        /**
         * Tests deletion when entity does not exist.
         */
        @Test
        @DisplayName("Should return false when Academic Vice President is not found")
        void deleteAcademicVicePresident_EntityNotFound_ShouldReturnFalse() {
            when(academicVicePresidentRepository.existsById("nonexistent")).thenReturn(false);

            boolean result = academicVicePresidentService.deleteAcademicVicePresident("nonexistent");

            assertFalse(result);
            verify(academicVicePresidentRepository).existsById("nonexistent");
            verify(academicVicePresidentRepository, never()).deleteById(anyString());
        }

        /**
         * Tests deletion with null ID.
         */
        @Test
        @DisplayName("Should handle null ID gracefully")
        void deleteAcademicVicePresident_NullId_ShouldReturnFalse() {
            when(academicVicePresidentRepository.existsById(null)).thenReturn(false);

            boolean result = academicVicePresidentService.deleteAcademicVicePresident(null);

            assertFalse(result);
            verify(academicVicePresidentRepository).existsById(null);
            verify(academicVicePresidentRepository, never()).deleteById(anyString());
        }

        /**
         * Tests deletion with empty ID.
         */
        @Test
        @DisplayName("Should handle empty ID gracefully")
        void deleteAcademicVicePresident_EmptyId_ShouldReturnFalse() {
            when(academicVicePresidentRepository.existsById("")).thenReturn(false);

            boolean result = academicVicePresidentService.deleteAcademicVicePresident("");

            assertFalse(result);
            verify(academicVicePresidentRepository).existsById("");
            verify(academicVicePresidentRepository, never()).deleteById(anyString());
        }

        /**
         * Tests deletion verification flow.
         */
        @Test
        @DisplayName("Should verify existence before deletion")
        void deleteAcademicVicePresident_ShouldVerifyExistenceFirst() {
            when(academicVicePresidentRepository.existsById("avp123")).thenReturn(true);

            academicVicePresidentService.deleteAcademicVicePresident("avp123");

            verify(academicVicePresidentRepository).existsById("avp123");
            verify(academicVicePresidentRepository).deleteById("avp123");
        }
    }

    @Nested
    @DisplayName("searchAcademicVicePresidentById() Tests")
    class SearchAcademicVicePresidentByIdTests {

        /**
         * Tests successful retrieval of an Academic Vice President by valid ID.
         */
        @Test
        @DisplayName("Should return Academic Vice President when valid ID is provided")
        void searchAcademicVicePresidentById_ValidId_ShouldReturnEntity() {
            when(academicVicePresidentRepository.findById("avp123")).thenReturn(Optional.of(testAcademicVicePresident));

            AcademicVicePresident result = academicVicePresidentService.searchAcademicVicePresidentById("avp123");

            assertNotNull(result);
            assertEquals(testAcademicVicePresident.getId(), result.getId());
            assertEquals(testAcademicVicePresident.getName(), result.getName());
            assertEquals(testAcademicVicePresident.getMail(), result.getMail());
            assertEquals(testAcademicVicePresident.getDocument(), result.getDocument());
            assertEquals(UserType.ACADEMIC_VICEPRESIDENT, result.getType());
            
            verify(academicVicePresidentRepository).findById("avp123");
        }

        /**
         * Tests retrieval when entity is not found.
         */
        @Test
        @DisplayName("Should return null when Academic Vice President is not found")
        void searchAcademicVicePresidentById_EntityNotFound_ShouldReturnNull() {
            when(academicVicePresidentRepository.findById("nonexistent")).thenReturn(Optional.empty());

            AcademicVicePresident result = academicVicePresidentService.searchAcademicVicePresidentById("nonexistent");

            assertNull(result);
            verify(academicVicePresidentRepository).findById("nonexistent");
        }

        /**
         * Tests retrieval with null ID.
         */
        @Test
        @DisplayName("Should handle null ID gracefully")
        void searchAcademicVicePresidentById_NullId_ShouldReturnNull() {
            when(academicVicePresidentRepository.findById(null)).thenReturn(Optional.empty());

            AcademicVicePresident result = academicVicePresidentService.searchAcademicVicePresidentById(null);

            assertNull(result);
            verify(academicVicePresidentRepository).findById(null);
        }

        /**
         * Tests retrieval with empty ID.
         */
        @Test
        @DisplayName("Should handle empty ID gracefully")
        void searchAcademicVicePresidentById_EmptyId_ShouldReturnNull() {
            when(academicVicePresidentRepository.findById("")).thenReturn(Optional.empty());

            AcademicVicePresident result = academicVicePresidentService.searchAcademicVicePresidentById("");

            assertNull(result);
            verify(academicVicePresidentRepository).findById("");
        }

        /**
         * Tests that UserType is correctly set in retrieved entity.
         */
        @Test
        @DisplayName("Should return entity with correct UserType")
        void searchAcademicVicePresidentById_ShouldReturnEntityWithCorrectType() {
            when(academicVicePresidentRepository.findById("avp123")).thenReturn(Optional.of(testAcademicVicePresident));

            AcademicVicePresident result = academicVicePresidentService.searchAcademicVicePresidentById("avp123");

            assertNotNull(result);
            assertEquals(UserType.ACADEMIC_VICEPRESIDENT, result.getType());
            verify(academicVicePresidentRepository).findById("avp123");
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
            AcademicVicePresidentRepository repository = mock(AcademicVicePresidentRepository.class);

            AcademicVicePresidentService service = new AcademicVicePresidentService(repository);

            assertNotNull(service);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        /**
         * Tests complete CRUD flow for Academic Vice President.
         */
        @Test
        @DisplayName("Should handle complete CRUD flow")
        void completeCRUDFlow_ShouldWorkCorrectly() {
            String entityId = "avp123";
            
            when(academicVicePresidentRepository.save(any(AcademicVicePresident.class))).thenReturn(testAcademicVicePresident);
            when(academicVicePresidentRepository.findById(entityId)).thenReturn(Optional.of(testAcademicVicePresident));
            when(academicVicePresidentRepository.existsById(entityId)).thenReturn(true);

            AcademicVicePresident created = academicVicePresidentService.createAcademicVicePresident(testUserDTO);
            assertNotNull(created);

            UserDTO updateDTO = new UserDTO();
            updateDTO.setName("Updated Name");
            updateDTO.setMail("updated@escuelaing.edu.co");
            updateDTO.setDocument("87654321");
            
            AcademicVicePresident updated = academicVicePresidentService.modifyAcademicVicePresident(entityId, updateDTO);
            assertNotNull(updated);

            AcademicVicePresident found = academicVicePresidentService.searchAcademicVicePresidentById(entityId);
            assertNotNull(found);

            boolean deleted = academicVicePresidentService.deleteAcademicVicePresident(entityId);
            assertTrue(deleted);

            verify(academicVicePresidentRepository, times(2)).save(any(AcademicVicePresident.class));
            verify(academicVicePresidentRepository, times(2)).findById(entityId);
            verify(academicVicePresidentRepository).existsById(entityId);
            verify(academicVicePresidentRepository).deleteById(entityId);
        }
    }
}