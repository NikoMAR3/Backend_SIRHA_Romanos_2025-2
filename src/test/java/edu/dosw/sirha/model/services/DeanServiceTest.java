package edu.dosw.sirha.model.services;

import edu.dosw.sirha.controller.dtos.UserDTO;
import edu.dosw.sirha.model.entities.Dean;
import edu.dosw.sirha.model.entities.Deanery;
import edu.dosw.sirha.model.persistence.repository.DeanRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DeanService.
 * Validates creation, modification, deletion, and search logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeanService Tests")
class DeanServiceTest {

    @Mock
    private DeanRepository deanRepository;

    @InjectMocks
    private DeanService deanService;

    private Dean testDean;
    private UserDTO testUserDTO;
    private Deanery testDeanery;

    @BeforeEach
    void setUp() {
        testUserDTO = new UserDTO();
        testUserDTO.setName("Dr. María González");
        testUserDTO.setMail("maria.gonzalez@escuelaing.edu.co");
        testUserDTO.setDocument("12345678");

        testDeanery = new Deanery();
        testDeanery.setId("deanery123");
        testDeanery.setDeaneryName("Facultad de Ingeniería");

        testDean = new Dean("dean123", testUserDTO.getName(), testUserDTO.getMail(), testUserDTO.getDocument());
        testDean.setActive(true);
        testDean.setDeanery(testDeanery);
    }

    @Nested
    @DisplayName("createDean() Tests")
    class CreateDeanTests {

        @Test
        @DisplayName("Should create dean successfully when valid UserDTO is provided")
        void createDean_ValidUserDTO_ShouldReturnCreatedDean() {
            when(deanRepository.save(any(Dean.class))).thenReturn(testDean);

            Dean result = deanService.createDean(testUserDTO);

            assertNotNull(result);
            assertEquals(testUserDTO.getName(), result.getName());
            assertEquals(testUserDTO.getMail(), result.getMail());
            assertEquals(testUserDTO.getDocument(), result.getDocument());

            verify(deanRepository).save(any(Dean.class));
        }

        @Test
        @DisplayName("Should generate UUID automatically when creating dean")
        void createDean_ShouldGenerateUUID() {
            when(deanRepository.save(any(Dean.class))).thenReturn(testDean);

            deanService.createDean(testUserDTO);

            ArgumentCaptor<Dean> captor = ArgumentCaptor.forClass(Dean.class);
            verify(deanRepository).save(captor.capture());

            Dean saved = captor.getValue();
            assertNotNull(saved.getId());
            assertTrue(saved.getId().matches("^[0-9a-f\\-]{36}$"));
        }

        @Test
        @DisplayName("Should throw exception when UserDTO is null")
        void createDean_NullUserDTO_ShouldThrowException() {
            assertThrows(NullPointerException.class, () -> deanService.createDean(null));
            verify(deanRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("modifyDean() Tests")
    class ModifyDeanTests {

        @Test
        @DisplayName("Should modify dean successfully when dean exists")
        void modifyDean_ExistingDean_ShouldReturnUpdatedDean() {
            UserDTO updatedDTO = new UserDTO();
            updatedDTO.setName("Actualizado");
            updatedDTO.setMail("nuevo@escuelaing.edu.co");
            updatedDTO.setDocument("87654321");

            Dean updatedDean = new Dean("dean123", updatedDTO.getName(), updatedDTO.getMail(), updatedDTO.getDocument());

            when(deanRepository.findByDeanCode("dean123")).thenReturn(Optional.of(testDean));
            when(deanRepository.save(any(Dean.class))).thenReturn(updatedDean);

            Optional<Dean> result = deanService.modifyDean("dean123", updatedDTO);

            assertTrue(result.isPresent());
            assertEquals("Actualizado", result.get().getName());
            verify(deanRepository).findByDeanCode("dean123");
            verify(deanRepository).save(any(Dean.class));
        }

        @Test
        @DisplayName("Should return empty Optional when dean not found")
        void modifyDean_NotFound_ShouldReturnEmpty() {
            when(deanRepository.findByDeanCode("missing")).thenReturn(Optional.empty());

            Optional<Dean> result = deanService.modifyDean("missing", testUserDTO);

            assertFalse(result.isPresent());
            verify(deanRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when UserDTO is null")
        void modifyDean_NullDTO_ShouldThrowException() {
            when(deanRepository.findByDeanCode("dean123")).thenReturn(Optional.of(testDean));
            assertThrows(NullPointerException.class, () -> deanService.modifyDean("dean123", null));
        }
    }

    @Nested
    @DisplayName("deleteDean() Tests")
    class DeleteDeanTests {

        @Test
        @DisplayName("Should delete dean successfully when exists")
        void deleteDean_Existing_ShouldReturnTrue() {
            when(deanRepository.existsById("dean123")).thenReturn(true);

            boolean result = deanService.deleteDean("dean123");

            assertTrue(result);
            verify(deanRepository).deleteById("dean123");
        }

        @Test
        @DisplayName("Should return false when dean does not exist")
        void deleteDean_NotFound_ShouldReturnFalse() {
            when(deanRepository.existsById("x")).thenReturn(false);
            assertFalse(deanService.deleteDean("x"));
            verify(deanRepository, never()).deleteById(anyString());
        }
    }

    @Nested
    @DisplayName("searchDeanByCode() Tests")
    class SearchDeanByCodeTests {

        @Test
        @DisplayName("Should return dean when exists")
        void searchDeanByCode_Valid_ShouldReturnDean() {
            when(deanRepository.findByDeanCode("dean123")).thenReturn(Optional.of(testDean));

            Dean result = deanService.searchDeanByCode("dean123");

            assertEquals(testDean, result);
            verify(deanRepository).findByDeanCode("dean123");
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void searchDeanByCode_NotFound_ShouldThrowException() {
            when(deanRepository.findByDeanCode("missing")).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> deanService.searchDeanByCode("missing")
            );
            assertTrue(ex.getMessage().contains("Dean no encontrado"));
        }
    }

    @Nested
    @DisplayName("searchAllDeans() Tests")
    class SearchAllDeansTests {

        @Test
        @DisplayName("Should return all deans")
        void searchAllDeans_ShouldReturnList() {
            when(deanRepository.findAll()).thenReturn(List.of(testDean));
            List<Dean> result = deanService.searchAllDeans();
            assertEquals(1, result.size());
            verify(deanRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when none exist")
        void searchAllDeans_Empty_ShouldReturnEmptyList() {
            when(deanRepository.findAll()).thenReturn(Collections.emptyList());
            assertTrue(deanService.searchAllDeans().isEmpty());
        }
    }

    @Nested
    @DisplayName("searchDeanByDeanery() Tests")
    class SearchDeanByDeaneryTests {

        @Test
        @DisplayName("Should return deans for valid deanery ID")
        void searchDeanByDeanery_Valid_ShouldReturnList() {
            when(deanRepository.findByDeaneryId("deanery123"))
                    .thenReturn(List.of(testDean));
            List<Dean> result = deanService.searchDeanByDeanery("deanery123");
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return empty list when none found")
        void searchDeanByDeanery_Empty_ShouldReturnEmptyList() {
            when(deanRepository.findByDeaneryId("unknown"))
                    .thenReturn(Collections.emptyList());
            assertTrue(deanService.searchDeanByDeanery("unknown").isEmpty());
        }
    }

    @Nested
    @DisplayName("Constructor Test")
    class ConstructorTest {
        @Test
        @DisplayName("Should instantiate service with repository")
        void constructor_ShouldCreateService() {
            DeanRepository repo = mock(DeanRepository.class);
            DeanService service = new DeanService(repo);
            assertNotNull(service);
        }
    }
}
