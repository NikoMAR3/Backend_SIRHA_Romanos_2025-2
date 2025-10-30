package edu.dosw.sirha.model.services;

import edu.dosw.sirha.controller.dtos.UserDTO;
import edu.dosw.sirha.model.entities.Deanery;
import edu.dosw.sirha.model.entities.Professor;
import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.persistence.repository.ProfessorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProfessorServiceTest {
    @Mock
    private ProfessorRepository professorRepository;
    @InjectMocks
    private ProfessorService professorService;
    private UserDTO dto;
    private Professor professor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dto = new UserDTO();
        dto.setName("John Doe");
        dto.setMail("john.doe@example.com");
        dto.setDocument("12345");
        professor = new Professor("1", "John Doe", "john.doe@example.com", "12345");
    }

    @Test
    void createProfessor_success() {
        when(professorRepository.save(any(Professor.class))).thenReturn(professor);

        Professor created = professorService.createProfessor(dto);

        assertNotNull(created);
        assertEquals("John Doe", created.getName());
        assertEquals("john.doe@example.com", created.getMail());
        verify(professorRepository, times(1)).save(any(Professor.class));
    }

    @Test
    void modifyProfessorByCode_success() {
        when(professorRepository.findByProfessorCode("PROF001")).thenReturn(Optional.of(professor));
        when(professorRepository.save(any(Professor.class))).thenReturn(professor);

        UserDTO updateDto = new UserDTO();
        updateDto.setName("Jane Doe");
        updateDto.setMail("jane.doe@example.com");
        updateDto.setDocument("67890");

        Optional<Professor> result = professorService.modifyProfessorByCode("PROF001", updateDto);

        assertTrue(result.isPresent());
        assertEquals("Jane Doe", result.get().getName());
        assertEquals("jane.doe@example.com", result.get().getMail());
        assertEquals("67890", result.get().getDocument());
        verify(professorRepository, times(1)).save(professor);
    }

    @Test
    void modifyProfessorByCode_notFound_returnsEmpty() {
        when(professorRepository.findByProfessorCode("PROF001")).thenReturn(Optional.empty());

        Optional<Professor> result = professorService.modifyProfessorByCode("PROF001", dto);

        assertTrue(result.isEmpty());
        verify(professorRepository, never()).save(any(Professor.class));
    }

    @Test
    void deleteProfessorByCode_success() {
        when(professorRepository.findByProfessorCode("PROF001")).thenReturn(Optional.of(professor));

        boolean deleted = professorService.deleteProfessorByCode("PROF001");

        assertTrue(deleted);
        verify(professorRepository).delete(professor);
    }

    @Test
    void deleteProfessorByCode_notFound_returnsFalse() {
        when(professorRepository.findByProfessorCode("PROF001")).thenReturn(Optional.empty());

        boolean deleted = professorService.deleteProfessorByCode("PROF001");

        assertFalse(deleted);
        verify(professorRepository, never()).delete(any(Professor.class));
    }

    @Test
    void searchProfessorById_found() {
        when(professorRepository.findById("1")).thenReturn(Optional.of(professor));

        Professor result = professorService.searchProfessorById("1");

        assertNotNull(result);
        assertEquals("1", result.getId());
    }

    @Test
    void searchProfessorById_notFound_returnsNull() {
        when(professorRepository.findById("1")).thenReturn(Optional.empty());

        Professor result = professorService.searchProfessorById("1");

        assertNull(result);
    }

    @Test
    void searchAllProfessors_returnsList() {
        when(professorRepository.findAll()).thenReturn(Arrays.asList(professor));

        List<Professor> result = professorService.searchAllProfessors();

        assertEquals(1, result.size());
        verify(professorRepository).findAll();
    }

    @Test
    void searchProfessorsByDeanery_returnsList() {
        Deanery deanery = new Deanery();
        when(professorRepository.findByDeanery(deanery)).thenReturn(Arrays.asList(professor));

        List<Professor> result = professorService.searchProfessorsByDeanery(deanery);

        assertEquals(1, result.size());
        verify(professorRepository).findByDeanery(deanery);
    }

    @Test
    void searchProfessorsBySubject_returnsList() {
        Subject subject = new Subject();
        when(professorRepository.findBySubjectsContaining(subject)).thenReturn(Arrays.asList(professor));

        List<Professor> result = professorService.searchProfessorsBySubject(subject);

        assertEquals(1, result.size());
        verify(professorRepository).findBySubjectsContaining(subject);
    }
}

