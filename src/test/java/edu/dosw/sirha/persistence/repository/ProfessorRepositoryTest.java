package edu.dosw.sirha.persistence.repository;
import edu.dosw.sirha.model.entities.Professor;
import edu.dosw.sirha.model.entities.Deanery;
import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.entities.UserType;
import edu.dosw.sirha.model.persistence.repository.ProfessorRepository;
import edu.dosw.sirha.model.persistence.repository.DeaneryRepository;
import edu.dosw.sirha.model.persistence.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for ProfessorRepository with MongoDB Testcontainers
 * Tests CRUD operations, custom queries, and relationship mappings
 */
@Testcontainers
@DataMongoTest
class ProfessorRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private DeaneryRepository deaneryRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    private Deanery computerScienceDeanery;
    private Deanery mathematicsDeanery;
    private Subject algorithmsSubject;
    private Subject databaseSubject;
    private Subject calculusSubject;
    private Professor professorSmith;
    private Professor professorJohnson;
    private Professor professorBrown;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        professorRepository.deleteAll();
        subjectRepository.deleteAll();
        deaneryRepository.deleteAll();

        // Create test deaneries
        computerScienceDeanery = new Deanery();
        computerScienceDeanery.setId("cs-1");
        computerScienceDeanery.setDeaneryName("Computer Science Faculty");
        computerScienceDeanery = deaneryRepository.save(computerScienceDeanery);

        mathematicsDeanery = new Deanery();
        mathematicsDeanery.setId("math-1");
        mathematicsDeanery.setDeaneryName("Mathematics Faculty");
        mathematicsDeanery = deaneryRepository.save(mathematicsDeanery);

        // Create test subjects
        algorithmsSubject = new Subject();
        algorithmsSubject.setId("subj-1");
        algorithmsSubject.setName("Algorithms and Data Structures");
        algorithmsSubject = subjectRepository.save(algorithmsSubject);

        databaseSubject = new Subject();
        databaseSubject.setId("subj-2");
        databaseSubject.setName("Database Systems");
        databaseSubject = subjectRepository.save(databaseSubject);

        calculusSubject = new Subject();
        calculusSubject.setId("subj-3");
        calculusSubject.setName("Calculus I");
        calculusSubject = subjectRepository.save(calculusSubject);

        // Create test professors
        professorSmith = new Professor("prof-1", "Dr. Alice Smith", "alice.smith@university.edu", "PROF123456");
        professorSmith.setDeanery(computerScienceDeanery);
        professorSmith.setSubjects(Arrays.asList(algorithmsSubject, databaseSubject));
        professorSmith = professorRepository.save(professorSmith);

        professorJohnson = new Professor("prof-2", "Dr. Bob Johnson", "bob.johnson@university.edu", "PROF654321");
        professorJohnson.setDeanery(computerScienceDeanery);
        professorJohnson.setSubjects(Arrays.asList(algorithmsSubject));
        professorJohnson = professorRepository.save(professorJohnson);

        professorBrown = new Professor("prof-3", "Dr. Carol Brown", "carol.brown@university.edu", "PROF111222");
        professorBrown.setDeanery(mathematicsDeanery);
        professorBrown.setSubjects(Arrays.asList(calculusSubject));
        professorBrown = professorRepository.save(professorBrown);
    }

    @Test
    void findById_WhenProfessorExists_ShouldReturnProfessorWithCorrectProperties() {
        // When
        Optional<Professor> found = professorRepository.findById("prof-1");

        // Then
        assertThat(found).isPresent();
        Professor professor = found.get();
        assertThat(professor.getId()).isEqualTo("prof-1");
        assertThat(professor.getName()).isEqualTo("Dr. Alice Smith");
        assertThat(professor.getMail()).isEqualTo("alice.smith@university.edu");
        assertThat(professor.getDocument()).isEqualTo("PROF123456");
        assertThat(professor.getType()).isEqualTo(UserType.PROFESSOR);
    }

    @Test
    void findById_WhenProfessorNotExists_ShouldReturnEmptyOptional() {
        // When
        Optional<Professor> found = professorRepository.findById("non-existent-id");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByDeanery_WhenMultipleProfessorsExistForDeanery_ShouldReturnAllProfessors() {
        // When
        List<Professor> csProfessors = professorRepository.findByDeanery(computerScienceDeanery);

        // Then
        assertThat(csProfessors).hasSize(2);
        assertThat(csProfessors)
                .extracting(Professor::getName)
                .containsExactlyInAnyOrder("Dr. Alice Smith", "Dr. Bob Johnson");
    }

    @Test
    void findByDeanery_WhenNoProfessorsForDeanery_ShouldReturnEmptyList() {
        // Given - Create a deanery with no professors
        Deanery emptyDeanery = new Deanery();
        emptyDeanery.setId("empty-1");
        emptyDeanery.setDeaneryName("Empty Faculty");
        deaneryRepository.save(emptyDeanery);

        // When
        List<Professor> professors = professorRepository.findByDeanery(emptyDeanery);

        // Then
        assertThat(professors).isEmpty();
    }

    @Test
    void findByDeaneryName_WhenProfessorsExist_ShouldReturnProfessors() {
        // When
        List<Professor> professors = professorRepository.findByDeaneryName("Computer Science Faculty");

        // Then
        assertThat(professors).hasSize(2);
        assertThat(professors)
                .extracting(Professor::getName)
                .containsExactlyInAnyOrder("Dr. Alice Smith", "Dr. Bob Johnson");
    }

    @Test
    void findByDeaneryName_WithNonExistentDeaneryName_ShouldReturnEmptyList() {
        // When
        List<Professor> professors = professorRepository.findByDeaneryName("Non-existent Faculty");

        // Then
        assertThat(professors).isEmpty();
    }

    @Test
    void findBySubjectsContaining_WhenSubjectExists_ShouldReturnAllProfessorsTeachingSubject() {
        // When
        List<Professor> algorithmsProfessors = professorRepository.findBySubjectsContaining(algorithmsSubject);

        // Then
        assertThat(algorithmsProfessors).hasSize(2);
        assertThat(algorithmsProfessors)
                .extracting(Professor::getName)
                .containsExactlyInAnyOrder("Dr. Alice Smith", "Dr. Bob Johnson");
    }

    @Test
    void findBySubjectId_WhenSubjectExists_ShouldReturnProfessors() {
        // When
        List<Professor> professors = professorRepository.findBySubjectId("subj-1");

        // Then
        assertThat(professors).hasSize(2);
        assertThat(professors)
                .extracting(Professor::getName)
                .containsExactlyInAnyOrder("Dr. Alice Smith", "Dr. Bob Johnson");
    }

    @Test
    void findBySubjectName_WhenSubjectExists_ShouldReturnProfessors() {
        // When
        List<Professor> professors = professorRepository.findBySubjectName("Algorithms and Data Structures");

        // Then
        assertThat(professors).hasSize(2);
        assertThat(professors)
                .extracting(Professor::getName)
                .containsExactlyInAnyOrder("Dr. Alice Smith", "Dr. Bob Johnson");
    }

    @Test
    void findByMail_WhenProfessorExists_ShouldReturnProfessor() {
        // When
        Optional<Professor> found = professorRepository.findByMail("alice.smith@university.edu");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo("prof-1");
        assertThat(found.get().getName()).isEqualTo("Dr. Alice Smith");
    }

    @Test
    void findByMail_WhenProfessorNotExists_ShouldReturnEmptyOptional() {
        // When
        Optional<Professor> found = professorRepository.findByMail("nonexistent@university.edu");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByDocument_WhenProfessorExists_ShouldReturnProfessor() {
        // When
        Optional<Professor> found = professorRepository.findByDocument("PROF123456");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo("prof-1");
        assertThat(found.get().getName()).isEqualTo("Dr. Alice Smith");
    }

    @Test
    void findByNameContainingIgnoreCase_WhenMatchesExist_ShouldReturnProfessors() {
        // When
        List<Professor> professors = professorRepository.findByNameContainingIgnoreCase("smith");

        // Then
        assertThat(professors).hasSize(1);
        assertThat(professors.get(0).getName()).isEqualTo("Dr. Alice Smith");
    }

    @Test
    void findByNameContainingIgnoreCase_WhenNoMatches_ShouldReturnEmptyList() {
        // When
        List<Professor> professors = professorRepository.findByNameContainingIgnoreCase("nonexistent");

        // Then
        assertThat(professors).isEmpty();
    }

    @Test
    void findByDeaneryAndSubject_WhenMatchesExist_ShouldReturnProfessors() {
        // When
        List<Professor> professors = professorRepository.findByDeaneryAndSubject(computerScienceDeanery, algorithmsSubject);

        // Then
        assertThat(professors).hasSize(2);
        assertThat(professors)
                .extracting(Professor::getName)
                .containsExactlyInAnyOrder("Dr. Alice Smith", "Dr. Bob Johnson");
    }

    @Test
    void countByDeanery_WhenProfessorsExist_ShouldReturnCorrectCount() {
        // When
        long csCount = professorRepository.countByDeanery(computerScienceDeanery);
        long mathCount = professorRepository.countByDeanery(mathematicsDeanery);

        // Then
        assertThat(csCount).isEqualTo(2);
        assertThat(mathCount).isEqualTo(1);
    }

    @Test
    void countBySubjectsContaining_WhenSubjectExists_ShouldReturnCorrectCount() {
        // When
        long algorithmsCount = professorRepository.countBySubjectsContaining(algorithmsSubject);
        long calculusCount = professorRepository.countBySubjectsContaining(calculusSubject);

        // Then
        assertThat(algorithmsCount).isEqualTo(2);
        assertThat(calculusCount).isEqualTo(1);
    }

    @Test
    void existsByMail_WhenEmailExists_ShouldReturnTrue() {
        // When & Then
        assertThat(professorRepository.existsByMail("alice.smith@university.edu")).isTrue();
    }

    @Test
    void existsByMail_WhenEmailNotExists_ShouldReturnFalse() {
        // When & Then
        assertThat(professorRepository.existsByMail("nonexistent@university.edu")).isFalse();
    }

    @Test
    void existsByDocument_WhenDocumentExists_ShouldReturnTrue() {
        // When & Then
        assertThat(professorRepository.existsByDocument("PROF123456")).isTrue();
    }

    @Test
    void deleteByDeanery_WhenProfessorsExist_ShouldRemoveAllProfessorsInDeanery() {
        // When
        professorRepository.deleteByDeanery(computerScienceDeanery);

        // Then
        List<Professor> remainingProfessors = professorRepository.findAll();
        assertThat(remainingProfessors).hasSize(1);
        assertThat(remainingProfessors.get(0).getName()).isEqualTo("Dr. Carol Brown");

        // Verify CS professors are gone
        List<Professor> csProfessors = professorRepository.findByDeanery(computerScienceDeanery);
        assertThat(csProfessors).isEmpty();
    }

    @Test
    void findAllByOrderByNameAsc_WhenProfessorsExist_ShouldReturnSortedList() {
        // When
        List<Professor> sortedProfessors = professorRepository.findAllByOrderByNameAsc();

        // Then
        assertThat(sortedProfessors).hasSize(3);
        assertThat(sortedProfessors)
                .extracting(Professor::getName)
                .containsExactly(
                        "Dr. Alice Smith",
                        "Dr. Bob Johnson",
                        "Dr. Carol Brown"
                );
    }

    @Test
    void findByDeaneryOrderByNameAsc_WhenProfessorsExist_ShouldReturnSortedList() {
        // When
        List<Professor> sortedProfessors = professorRepository.findByDeaneryOrderByNameAsc(computerScienceDeanery);

        // Then
        assertThat(sortedProfessors).hasSize(2);
        assertThat(sortedProfessors)
                .extracting(Professor::getName)
                .containsExactly(
                        "Dr. Alice Smith",
                        "Dr. Bob Johnson"
                );
    }

    @Test
    void save_NewProfessor_ShouldPersistWithAllAttributes() {
        // Given
        Professor newProfessor = new Professor("prof-new", "Dr. New Professor", "new.prof@university.edu", "PROF999888");
        newProfessor.setDeanery(mathematicsDeanery);
        newProfessor.setSubjects(Arrays.asList(calculusSubject));

        // When
        Professor saved = professorRepository.save(newProfessor);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo("prof-new");
        assertThat(saved.getName()).isEqualTo("Dr. New Professor");
        assertThat(saved.getMail()).isEqualTo("new.prof@university.edu");
        assertThat(saved.getDocument()).isEqualTo("PROF999888");
        assertThat(saved.getType()).isEqualTo(UserType.PROFESSOR);
        assertThat(saved.getDeanery().getId()).isEqualTo("math-1");
        assertThat(saved.getSubjects()).hasSize(1);
        assertThat(saved.getSubjects().get(0).getName()).isEqualTo("Calculus I");

        // Verify retrieval
        Optional<Professor> found = professorRepository.findById("prof-new");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Dr. New Professor");
    }

    @Test
    void update_ExistingProfessor_ShouldModifyAttributes() {
        // Given
        Professor existing = professorRepository.findById("prof-1").get();
        existing.setName("Dr. Alice Smith Updated");
        existing.setMail("alice.updated@university.edu");

        // When
        Professor updated = professorRepository.save(existing);

        // Then
        assertThat(updated.getName()).isEqualTo("Dr. Alice Smith Updated");
        assertThat(updated.getMail()).isEqualTo("alice.updated@university.edu");

        // Verify the update persisted
        Optional<Professor> found = professorRepository.findById("prof-1");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Dr. Alice Smith Updated");
    }

    @Test
    void delete_ExistingProfessor_ShouldRemoveFromDatabase() {
        // When
        professorRepository.deleteById("prof-1");

        // Then
        Optional<Professor> found = professorRepository.findById("prof-1");
        assertThat(found).isEmpty();

        List<Professor> remainingProfessors = professorRepository.findAll();
        assertThat(remainingProfessors).hasSize(2);
    }

    @Test
    void professorEntity_ShouldHaveCorrectUserType() {
        // When
        Optional<Professor> found = professorRepository.findById("prof-1");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo(UserType.PROFESSOR);
        assertThat(found.get().getType().name()).isEqualTo("PROFESSOR");
    }

    @Test
    void professorEntity_WithDBRefDeanery_ShouldMaintainRelationship() {
        // When
        Optional<Professor> found = professorRepository.findById("prof-1");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getDeanery()).isNotNull();
        assertThat(found.get().getDeanery().getId()).isEqualTo("cs-1");
        assertThat(found.get().getDeanery().getDeaneryName()).isEqualTo("Computer Science Faculty");
    }

    @Test
    void professorEntity_WithSubjects_ShouldMaintainSubjectRelationships() {
        // When
        Optional<Professor> found = professorRepository.findById("prof-1");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getSubjects()).hasSize(2);
        assertThat(found.get().getSubjects())
                .extracting(Subject::getName)
                .containsExactlyInAnyOrder("Algorithms and Data Structures", "Database Systems");
    }

    @Test
    void testProfessorConstructor_ShouldSetUserTypeAutomatically() {
        // Given
        Professor newProfessor = new Professor("test-id", "Test Professor", "test@university.edu", "TEST123");

        // Then
        assertThat(newProfessor.getType()).isEqualTo(UserType.PROFESSOR);
        assertThat(newProfessor.getId()).isEqualTo("test-id");
        assertThat(newProfessor.getName()).isEqualTo("Test Professor");
        assertThat(newProfessor.getMail()).isEqualTo("test@university.edu");
        assertThat(newProfessor.getDocument()).isEqualTo("TEST123");
    }
}
