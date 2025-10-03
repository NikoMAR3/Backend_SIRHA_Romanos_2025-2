package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.AcademicPlan;
import edu.dosw.sirha.model.entities.AcademicProgram;
import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.entities.TrafficLight;
import edu.dosw.sirha.model.persistence.repository.AcademicPlanRepository;
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
 * Unit tests for AcademicPlanService.
 * Tests all methods with various scenarios including success cases and error conditions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AcademicPlanService Tests")
class AcademicPlanServiceTest {

    @Mock
    private AcademicPlanRepository academicPlanRepository;

    @InjectMocks
    private AcademicPlanService academicPlanService;

    private AcademicPlan testPlan;
    private AcademicProgram testProgram;
    private TrafficLight testTrafficLight;
    private List<Subject> testSubjects;

    @BeforeEach
    void setUp() {
        testProgram = new AcademicProgram();
        testProgram.setId("program123");
        testProgram.setName("Ingeniería de Sistemas");

        testTrafficLight = new TrafficLight();
        testTrafficLight.setId("traffic123");

        Subject subject1 = new Subject();
        subject1.setId("subject1");
        subject1.setName("Matemáticas I");

        Subject subject2 = new Subject();
        subject2.setId("subject2");
        subject2.setName("Programación I");

        testSubjects = Arrays.asList(subject1, subject2);

        testPlan = new AcademicPlan();
        testPlan.setId("plan123");
        testPlan.setName("Plan 15");
        testPlan.setProgram(testProgram);
        testPlan.setTrafficLight(testTrafficLight);
        testPlan.setSubjects(testSubjects);
    }

    @Nested
    @DisplayName("createPlan() Tests")
    class CreatePlanTests {

        @Test
        @DisplayName("Should create plan successfully when valid plan is provided")
        void createPlan_ValidPlan_ShouldReturnCreatedPlan() {

            AcademicPlan newPlan = new AcademicPlan();
            newPlan.setName("Plan 16");
            newPlan.setProgram(testProgram);

            when(academicPlanRepository.findByName("Plan 16")).thenReturn(Optional.empty());
            when(academicPlanRepository.save(any(AcademicPlan.class))).thenReturn(testPlan);

            
            AcademicPlan result = academicPlanService.createPlan(newPlan);

            
            assertNotNull(result);
            assertEquals(testPlan.getId(), result.getId());
            verify(academicPlanRepository).findByName("Plan 16");
            verify(academicPlanRepository).save(newPlan);
        }

        @Test
        @DisplayName("Should create plan successfully when plan has no name")
        void createPlan_PlanWithNoName_ShouldReturnCreatedPlan() {
            
            AcademicPlan newPlan = new AcademicPlan();
            newPlan.setProgram(testProgram);

            when(academicPlanRepository.save(any(AcademicPlan.class))).thenReturn(testPlan);

          
            AcademicPlan result = academicPlanService.createPlan(newPlan);

            
            assertNotNull(result);
            assertEquals(testPlan.getId(), result.getId());
            verify(academicPlanRepository, never()).findByName(anyString());
            verify(academicPlanRepository).save(newPlan);
        }

        @Test
        @DisplayName("Should throw exception when plan is null")
        void createPlan_NullPlan_ShouldThrowException() {
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicPlanService.createPlan(null)
            );

            assertEquals("El plan académico no puede ser nulo", exception.getMessage());
            verify(academicPlanRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when plan with same name already exists")
        void createPlan_DuplicateName_ShouldThrowException() {
        
            AcademicPlan newPlan = new AcademicPlan();
            newPlan.setName("Plan 15");

            when(academicPlanRepository.findByName("Plan 15")).thenReturn(Optional.of(testPlan));

            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicPlanService.createPlan(newPlan)
            );

            assertEquals("El plan académico con nombre 'Plan 15' ya existe", exception.getMessage());
            verify(academicPlanRepository).findByName("Plan 15");
            verify(academicPlanRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("modifyPlan() Tests")
    class ModifyPlanTests {

        @Test
        @DisplayName("Should modify plan successfully when valid plan is provided")
        void modifyPlan_ValidPlan_ShouldReturnUpdatedPlan() {
            
            when(academicPlanRepository.existsById("plan123")).thenReturn(true);
            when(academicPlanRepository.save(any(AcademicPlan.class))).thenReturn(testPlan);

           
            AcademicPlan result = academicPlanService.modifyPlan(testPlan);

           
            assertNotNull(result);
            assertEquals(testPlan.getId(), result.getId());
            verify(academicPlanRepository).existsById("plan123");
            verify(academicPlanRepository).save(testPlan);
        }

        @Test
        @DisplayName("Should throw exception when plan is null")
        void modifyPlan_NullPlan_ShouldThrowException() {
           
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicPlanService.modifyPlan(null)
            );

            assertEquals("El plan académico no puede ser nulo", exception.getMessage());
            verify(academicPlanRepository, never()).existsById(anyString());
            verify(academicPlanRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when plan ID is null")
        void modifyPlan_NullId_ShouldThrowException() {
           
            AcademicPlan planWithoutId = new AcademicPlan();
            planWithoutId.setName("Plan 15");

           
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicPlanService.modifyPlan(planWithoutId)
            );

            assertEquals("El plan académico con ID 'null' no existe", exception.getMessage());
            verify(academicPlanRepository, never()).existsById(anyString());
            verify(academicPlanRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when plan doesn't exist")
        void modifyPlan_NonExistentPlan_ShouldThrowException() {
            
            when(academicPlanRepository.existsById("plan123")).thenReturn(false);

          
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicPlanService.modifyPlan(testPlan)
            );

            assertEquals("El plan académico con ID 'plan123' no existe", exception.getMessage());
            verify(academicPlanRepository).existsById("plan123");
            verify(academicPlanRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deletePlan() Tests")
    class DeletePlanTests {

        @Test
        @DisplayName("Should delete plan successfully when valid ID is provided")
        void deletePlan_ValidId_ShouldDeletePlan() {
            
            when(academicPlanRepository.existsById("plan123")).thenReturn(true);

           
            academicPlanService.deletePlan("plan123");

            
            verify(academicPlanRepository).existsById("plan123");
            verify(academicPlanRepository).deleteById("plan123");
        }

        @Test
        @DisplayName("Should throw exception when ID is null")
        void deletePlan_NullId_ShouldThrowException() {
           
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicPlanService.deletePlan(null)
            );

            assertEquals("El ID del plan no puede ser nulo o vacío", exception.getMessage());
            verify(academicPlanRepository, never()).existsById(anyString());
            verify(academicPlanRepository, never()).deleteById(anyString());
        }

        @Test
        @DisplayName("Should throw exception when ID is empty")
        void deletePlan_EmptyId_ShouldThrowException() {
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicPlanService.deletePlan("   ")
            );

            assertEquals("El ID del plan no puede ser nulo o vacío", exception.getMessage());
            verify(academicPlanRepository, never()).existsById(anyString());
            verify(academicPlanRepository, never()).deleteById(anyString());
        }

        @Test
        @DisplayName("Should throw exception when plan doesn't exist")
        void deletePlan_NonExistentPlan_ShouldThrowException() {
            
            when(academicPlanRepository.existsById("plan123")).thenReturn(false);

            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicPlanService.deletePlan("plan123")
            );

            assertEquals("El plan académico con ID 'plan123' no existe", exception.getMessage());
            verify(academicPlanRepository).existsById("plan123");
            verify(academicPlanRepository, never()).deleteById(anyString());
        }
    }

    @Nested
    @DisplayName("searchPlanById() Tests")
    class SearchPlanByIdTests {

        @Test
        @DisplayName("Should return plan when valid ID is provided")
        void searchPlanById_ValidId_ShouldReturnPlan() {
           
            when(academicPlanRepository.findPlanById("plan123")).thenReturn(Optional.of(testPlan));

            
            AcademicPlan result = academicPlanService.searchPlanById("plan123");

          
            assertNotNull(result);
            assertEquals(testPlan.getId(), result.getId());
            assertEquals(testPlan.getName(), result.getName());
            verify(academicPlanRepository).findPlanById("plan123");
        }

        @Test
        @DisplayName("Should throw exception when ID is null")
        void searchPlanById_NullId_ShouldThrowException() {
           
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicPlanService.searchPlanById(null)
            );

            assertEquals("El ID del plan no puede ser nulo o vacío", exception.getMessage());
            verify(academicPlanRepository, never()).findPlanById(anyString());
        }

        @Test
        @DisplayName("Should throw exception when ID is empty")
        void searchPlanById_EmptyId_ShouldThrowException() {
          
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicPlanService.searchPlanById("  ")
            );

            assertEquals("El ID del plan no puede ser nulo o vacío", exception.getMessage());
            verify(academicPlanRepository, never()).findPlanById(anyString());
        }

        @Test
        @DisplayName("Should throw exception when plan is not found")
        void searchPlanById_NotFound_ShouldThrowException() {
           
            when(academicPlanRepository.findPlanById("plan123")).thenReturn(Optional.empty());

           
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicPlanService.searchPlanById("plan123")
            );

            assertEquals("El plan académico con ID 'plan123' no fue encontrado", exception.getMessage());
            verify(academicPlanRepository).findPlanById("plan123");
        }
    }

    @Nested
    @DisplayName("searchPlanByName() Tests")
    class SearchPlanByNameTests {

        @Test
        @DisplayName("Should return plan when valid name is provided")
        void searchPlanByName_ValidName_ShouldReturnPlan() {
           
            when(academicPlanRepository.findByName("Plan 15")).thenReturn(Optional.of(testPlan));

           
            AcademicPlan result = academicPlanService.searchPlanByName("Plan 15");

          
            assertNotNull(result);
            assertEquals(testPlan.getName(), result.getName());
            assertEquals(testPlan.getId(), result.getId());
            verify(academicPlanRepository).findByName("Plan 15");
        }

        @Test
        @DisplayName("Should throw exception when name is null")
        void searchPlanByName_NullName_ShouldThrowException() {
           
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicPlanService.searchPlanByName(null)
            );

            assertEquals("El nombre del plan no puede ser nulo o vacío", exception.getMessage());
            verify(academicPlanRepository, never()).findByName(anyString());
        }

        @Test
        @DisplayName("Should throw exception when name is empty")
        void searchPlanByName_EmptyName_ShouldThrowException() {
          
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicPlanService.searchPlanByName("  ")
            );

            assertEquals("El nombre del plan no puede ser nulo o vacío", exception.getMessage());
            verify(academicPlanRepository, never()).findByName(anyString());
        }

        @Test
        @DisplayName("Should throw exception when plan is not found")
        void searchPlanByName_NotFound_ShouldThrowException() {
           
            when(academicPlanRepository.findByName("Plan 15")).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicPlanService.searchPlanByName("Plan 15")
            );

            assertEquals("El plan académico con nombre 'Plan 15' no fue encontrado", exception.getMessage());
            verify(academicPlanRepository).findByName("Plan 15");
        }
    }

    @Nested
    @DisplayName("searchPlanByProgram() Tests")
    class SearchPlanByProgramTests {

        @Test
        @DisplayName("Should return plans when valid program is provided")
        void searchPlanByProgram_ValidProgram_ShouldReturnPlans() {
          
            List<AcademicPlan> expectedPlans = Arrays.asList(testPlan);
            when(academicPlanRepository.findByProgram(testProgram)).thenReturn(expectedPlans);

            List<AcademicPlan> result = academicPlanService.searchPlanByProgram(testProgram);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testPlan.getId(), result.get(0).getId());
            verify(academicPlanRepository).findByProgram(testProgram);
        }

        @Test
        @DisplayName("Should return empty list when no plans found for program")
        void searchPlanByProgram_NoPlansFound_ShouldReturnEmptyList() {
            when(academicPlanRepository.findByProgram(testProgram)).thenReturn(Arrays.asList());

            List<AcademicPlan> result = academicPlanService.searchPlanByProgram(testProgram);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(academicPlanRepository).findByProgram(testProgram);
        }

        @Test
        @DisplayName("Should throw exception when program is null")
        void searchPlanByProgram_NullProgram_ShouldThrowException() {
          
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicPlanService.searchPlanByProgram(null)
            );

            assertEquals("El programa académico no puede ser nulo", exception.getMessage());
            verify(academicPlanRepository, never()).findByProgram(any());
        }
    }

    @Nested
    @DisplayName("searchPlanByProgramId() Tests")
    class SearchPlanByProgramIdTests {

        @Test
        @DisplayName("Should return plans when valid program ID is provided")
        void searchPlanByProgramId_ValidProgramId_ShouldReturnPlans() {
           
            List<AcademicPlan> expectedPlans = Arrays.asList(testPlan);
            when(academicPlanRepository.findByProgramId("program123")).thenReturn(expectedPlans);

          
            List<AcademicPlan> result = academicPlanService.searchPlanByProgramId("program123");

            
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testPlan.getId(), result.get(0).getId());
            verify(academicPlanRepository).findByProgramId("program123");
        }

        @Test
        @DisplayName("Should return empty list when no plans found for program ID")
        void searchPlanByProgramId_NoPlansFound_ShouldReturnEmptyList() {
          
            when(academicPlanRepository.findByProgramId("program123")).thenReturn(Arrays.asList());

          
            List<AcademicPlan> result = academicPlanService.searchPlanByProgramId("program123");

          
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(academicPlanRepository).findByProgramId("program123");
        }

        @Test
        @DisplayName("Should throw exception when program ID is null")
        void searchPlanByProgramId_NullProgramId_ShouldThrowException() {
           
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicPlanService.searchPlanByProgramId(null)
            );

            assertEquals("El ID del programa académico no puede ser nulo o vacío", exception.getMessage());
            verify(academicPlanRepository, never()).findByProgramId(anyString());
        }

        @Test
        @DisplayName("Should throw exception when program ID is empty")
        void searchPlanByProgramId_EmptyProgramId_ShouldThrowException() {
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> academicPlanService.searchPlanByProgramId("  ")
            );

            assertEquals("El ID del programa académico no puede ser nulo o vacío", exception.getMessage());
            verify(academicPlanRepository, never()).findByProgramId(anyString());
        }
    }

    @Nested
    @DisplayName("searchAllPlans() Tests")
    class SearchAllPlansTests {

        @Test
        @DisplayName("Should return all plans when plans exist")
        void searchAllPlans_PlansExist_ShouldReturnAllPlans() {
          
            AcademicPlan plan2 = new AcademicPlan();
            plan2.setId("plan456");
            plan2.setName("Plan 14");

            List<AcademicPlan> expectedPlans = Arrays.asList(testPlan, plan2);
            when(academicPlanRepository.findAll()).thenReturn(expectedPlans);

         
            List<AcademicPlan> result = academicPlanService.searchAllPlans();

           
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains(testPlan));
            assertTrue(result.contains(plan2));
            verify(academicPlanRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no plans exist")
        void searchAllPlans_NoPlansExist_ShouldReturnEmptyList() {
           
            when(academicPlanRepository.findAll()).thenReturn(Arrays.asList());

           
            List<AcademicPlan> result = academicPlanService.searchAllPlans();

          
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(academicPlanRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create service with repository dependency")
        void constructor_ShouldCreateServiceWithRepository() {
            
            AcademicPlanRepository repository = mock(AcademicPlanRepository.class);

            
            AcademicPlanService service = new AcademicPlanService(repository);

         
            assertNotNull(service);
        }
    }
}