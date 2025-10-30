package edu.dosw.sirha.persistence.repository;

import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.entities.ClassSession;
import edu.dosw.sirha.model.persistence.repository.ClassSessionRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest
class SubjectRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.6");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ClassSessionRepository classSessionRepository;

    private Subject mathSubject;
    private Subject physicsSubject;
    private Subject advancedMathSubject;

    @BeforeEach
    void setUp() {
        subjectRepository.deleteAll();

        // Crear ClassSession de ejemplo
        ClassSession mathSession = new ClassSession();
        mathSession.setId("session1");
        mathSession.setSubjectShortName("MATH101");
        mathSession.setSubjectName("Mathematics");
        mathSession.setStartDate(LocalDateTime.now());
        mathSession.setEndDate(LocalDateTime.now().plusHours(2));
        classSessionRepository.save(mathSession);

        // Crear materias
        mathSubject = new Subject();
        mathSubject.setShortName("MATH101");
        mathSubject.setName("Mathematics");
        mathSubject.setCredits(4);
        mathSubject.setLevel(1);
        mathSubject.setProgramId("PROG1");
        mathSubject.setClassSessions(List.of(mathSession));

        physicsSubject = new Subject();
        physicsSubject.setShortName("PHYS201");
        physicsSubject.setName("Physics");
        physicsSubject.setCredits(6);
        physicsSubject.setLevel(2);
        physicsSubject.setProgramId("PROG1");
        physicsSubject.setPrerequisites(Arrays.asList(mathSubject));

        advancedMathSubject = new Subject();
        advancedMathSubject.setShortName("ADVMATH301");
        advancedMathSubject.setName("Advanced Mathematics");
        advancedMathSubject.setCredits(8);
        advancedMathSubject.setLevel(3);
        advancedMathSubject.setProgramId("PROG2");

        // Guardar materias (primero math para que exista como prerequisito)
        subjectRepository.save(mathSubject);
        subjectRepository.save(physicsSubject);
        subjectRepository.save(advancedMathSubject);
    }

    @Test
    void findByShortName_WhenSubjectExists_ShouldReturnSubject() {
        Optional<Subject> found = subjectRepository.findByShortName("MATH101");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Mathematics");
        assertThat(found.get().getCredits()).isEqualTo(4);
    }

    @Test
    void findByShortName_WhenSubjectNotExists_ShouldReturnEmpty() {
        Optional<Subject> found = subjectRepository.findByShortName("CHEM101");

        assertThat(found).isEmpty();
    }

    @Test
    void findByName_WhenSubjectExists_ShouldReturnSubject() {
        Optional<Subject> found = subjectRepository.findByName("Physics");

        assertThat(found).isPresent();
        assertThat(found.get().getShortName()).isEqualTo("PHYS201");
    }

    @Test
    void findByLevel_ShouldReturnSubjectsAtLevel() {
        List<Subject> level1Subjects = subjectRepository.findByLevel(1);

        assertThat(level1Subjects).hasSize(1);
        assertThat(level1Subjects.get(0).getShortName()).isEqualTo("MATH101");
    }

    @Test
    void findByCredits_ShouldReturnSubjectsWithExactCredits() {
        List<Subject> subjectsWith6Credits = subjectRepository.findByCredits(6);

        assertThat(subjectsWith6Credits).hasSize(1);
        assertThat(subjectsWith6Credits.get(0).getName()).isEqualTo("Physics");
    }

    @Test
    void findByCreditsGreaterThanEqual_ShouldReturnSubjectsWithEqualOrMoreCredits() {
        List<Subject> subjects = subjectRepository.findByCreditsGreaterThanEqual(6);

        assertThat(subjects).hasSize(2);
        assertThat(subjects)
                .extracting(Subject::getShortName)
                .containsExactlyInAnyOrder("PHYS201", "ADVMATH301");
    }

    @Test
    void findByProgramId_ShouldReturnSubjectsInProgram() {
        List<Subject> prog1Subjects = subjectRepository.findByProgramId("PROG1");

        assertThat(prog1Subjects).hasSize(2);
        assertThat(prog1Subjects)
                .extracting(Subject::getShortName)
                .containsExactlyInAnyOrder("MATH101", "PHYS201");
    }

    @Test
    void saveSubject_WithPrerequisites_ShouldMaintainReferences() {
        Optional<Subject> savedPhysics = subjectRepository.findByShortName("PHYS201");

        assertThat(savedPhysics).isPresent();
        assertThat(savedPhysics.get().getPrerequisites()).isNotEmpty();
        assertThat(savedPhysics.get().getPrerequisites().get(0).getShortName())
                .isEqualTo("MATH101");
    }

    @Test
    void saveSubject_WithClassSession_ShouldMaintainReference() {
        Optional<Subject> savedMath = subjectRepository.findByShortName("MATH101");

        assertThat(savedMath).isPresent();
        assertThat(savedMath.get().getClassSessions()).isNotNull();
        assertThat(savedMath.get().getClassSessions().get(0).getSubjectShortName())
                .isEqualTo("MATH101");
    }
}