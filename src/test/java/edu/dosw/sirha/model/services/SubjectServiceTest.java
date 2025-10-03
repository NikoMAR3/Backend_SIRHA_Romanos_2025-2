package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.entities.AcademicProgram;
import edu.dosw.sirha.model.persistence.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubjectServiceTest {
    @Mock
    private SubjectRepository subjectRepository;
    @InjectMocks
    private SubjectService subjectService;
    private Subject subject;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subject = new Subject();
        subject.setId("1");
        subject.setName("Mathematics");
        subject.setShortName("MATH");
        subject.setCredits(3);
        subject.setLevel(1);
    }

    @Test
    void createSubject_success() {
        when(subjectRepository.findByShortName("MATH")).thenReturn(Optional.empty());
        when(subjectRepository.save(subject)).thenReturn(subject);

        Subject result = subjectService.createSubject(subject);

        assertNotNull(result);
        assertEquals("MATH", result.getShortName());
        verify(subjectRepository).save(subject);
    }

    @Test
    void createSubject_shouldThrowException_whenSubjectIsNull() {
        assertThrows(IllegalArgumentException.class, () -> subjectService.createSubject(null));
    }

    @Test
    void createSubject_shouldThrowException_whenShortNameAlreadyExists() {
        when(subjectRepository.findByShortName("MATH")).thenReturn(Optional.of(subject));
        assertThrows(IllegalArgumentException.class, () -> subjectService.createSubject(subject));
    }

    @Test
    void modifySubject_success() {
        when(subjectRepository.existsById("1")).thenReturn(true);
        when(subjectRepository.save(subject)).thenReturn(subject);

        Subject result = subjectService.modifySubject(subject);

        assertEquals("Mathematics", result.getName());
        verify(subjectRepository).save(subject);
    }

    @Test
    void modifySubject_shouldThrowException_whenIdIsMissing() {
        subject.setId(null);
        assertThrows(IllegalArgumentException.class, () -> subjectService.modifySubject(subject));
    }

    @Test
    void modifySubject_shouldThrowException_whenNotExists() {
        when(subjectRepository.existsById("1")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> subjectService.modifySubject(subject));
    }

    @Test
    void deleteSubject_success() {
        when(subjectRepository.existsById("1")).thenReturn(true);

        subjectService.deleteSubject("1");

        verify(subjectRepository).deleteById("1");
    }

    @Test
    void deleteSubject_shouldThrowException_whenNotExists() {
        when(subjectRepository.existsById("1")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> subjectService.deleteSubject("1"));
    }

    @Test
    void searchSubjectById_success() {
        when(subjectRepository.findById("1")).thenReturn(Optional.of(subject));

        Subject result = subjectService.searchSubjectById("1");

        assertEquals("MATH", result.getShortName());
    }

    @Test
    void searchSubjectById_shouldThrowException_whenNotFound() {
        when(subjectRepository.findById("1")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> subjectService.searchSubjectById("1"));
    }

    @Test
    void searchSubjectByShortName_success() {
        when(subjectRepository.findByShortName("MATH")).thenReturn(Optional.of(subject));

        Subject result = subjectService.searchSubjectByShortName("MATH");

        assertEquals("Mathematics", result.getName());
    }

    @Test
    void searchSubjectsByProgram_success() {
        AcademicProgram program = new AcademicProgram();
        program.setId("P1");

        when(subjectRepository.findByProgramId("P1")).thenReturn(List.of(subject));

        List<Subject> result = subjectService.searchSubjectsByProgram(program);

        assertEquals(1, result.size());
    }

    @Test
    void searchSubjectsByCredits_success() {
        when(subjectRepository.findByCredits(3)).thenReturn(List.of(subject));

        List<Subject> result = subjectService.searchSubjectsByCredits(3);

        assertEquals(1, result.size());
    }

    @Test
    void searchSubjectsByLevel_success() {
        when(subjectRepository.findByLevel(1)).thenReturn(List.of(subject));

        List<Subject> result = subjectService.searchSubjectsByLevel(1);

        assertEquals(1, result.size());
    }

    @Test
    void searchSubjectPreRequisites_success_whenNoPrerequisites() {
        subject.setPrerequisites(null);

        when(subjectRepository.findById("1")).thenReturn(Optional.of(subject));

        List<Subject> result = subjectService.searchSubjectPreRequisites("1");

        assertTrue(result.isEmpty());
    }

    @Test
    void existsById_success() {
        when(subjectRepository.existsById("1")).thenReturn(true);

        boolean exists = subjectService.existsById("1");

        assertTrue(exists);
    }
}

