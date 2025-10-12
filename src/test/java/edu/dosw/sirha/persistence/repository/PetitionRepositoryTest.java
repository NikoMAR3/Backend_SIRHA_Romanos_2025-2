package edu.dosw.sirha.persistence.repository;

import edu.dosw.sirha.model.entities.Petition;
import edu.dosw.sirha.model.entities.PetitionState;
import edu.dosw.sirha.model.entities.PetitionType;
import edu.dosw.sirha.model.entities.PetitionPriority;
import edu.dosw.sirha.model.persistence.repository.PetitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class PetitionRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private PetitionRepository petitionRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Petition petition1, petition2, petition3, petition4, petition5;
    private LocalDateTime now;
    private LocalDateTime yesterday;
    private LocalDateTime lastWeek;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(Petition.class);

        now = LocalDateTime.now();
        yesterday = now.minusDays(1);
        lastWeek = now.minusDays(7);

        petition1 = new Petition();
        petition1.setPetitionId("PET001");
        petition1.setType(PetitionType.ADD_SUBJECT);
        petition1.setState(PetitionState.PENDING);
        petition1.setPriority(PetitionPriority.HIGH);
        petition1.setStudentId("STU001");
        petition1.setSubjectId("SUB001");
        petition1.setAssociateDeanery("Facultad de Ingeniería");
        petition1.setJustification("Solicitud para agregar una materia adicional");
        petition1.setCreationDate(lastWeek);
        petition1.setModificationDate(lastWeek);

        petition2 = new Petition();
        petition2.setPetitionId("PET002");
        petition2.setType(PetitionType.REMOVE_SUBJECT);
        petition2.setState(PetitionState.IN_PROCESS);
        petition2.setPriority(PetitionPriority.MEDIUM);
        petition2.setStudentId("STU002");
        petition2.setSubjectId("SUB002");
        petition2.setAssociateDeanery("Facultad de Ciencias");
        petition2.setJustification("Retiro de una asignatura por carga académica");
        petition2.setCreationDate(yesterday);
        petition2.setModificationDate(yesterday);

        petition3 = new Petition();
        petition3.setPetitionId("PET003");
        petition3.setType(PetitionType.CHANGE_GROUP);
        petition3.setState(PetitionState.APPROVED);
        petition3.setPriority(PetitionPriority.LOW);
        petition3.setStudentId("STU001");
        petition3.setSubjectId("SUB003");
        petition3.setAssociateDeanery("Facultad de Ingeniería");
        petition3.setJustification("Cambio de grupo por conflicto de horario");
        petition3.setCreationDate(now);
        petition3.setModificationDate(now);

        petition4 = new Petition();
        petition4.setPetitionId("PET004");
        petition4.setType(PetitionType.ADD_SUBJECT);
        petition4.setState(PetitionState.REPROVED);
        petition4.setPriority(PetitionPriority.HIGH);
        petition4.setStudentId("STU003");
        petition4.setSubjectId("SUB001");
        petition4.setAssociateDeanery("Facultad de Humanidades");
        petition4.setJustification("Agregación de materia no aprobada");
        petition4.setCreationDate(yesterday);
        petition4.setModificationDate(yesterday);

        petition5 = new Petition();
        petition5.setPetitionId("PET005");
        petition5.setType(PetitionType.REMOVE_SUBJECT);
        petition5.setState(PetitionState.PENDING);
        petition5.setPriority(PetitionPriority.URGENT);
        petition5.setStudentId("STU002");
        petition5.setSubjectId(null);
        petition5.setAssociateDeanery("Facultad de Ciencias");
        petition5.setJustification("Retiro urgente de asignatura");
        petition5.setCreationDate(now.minusHours(2));
        petition5.setModificationDate(now.minusHours(2));

        petitionRepository.saveAll(Arrays.asList(petition1, petition2, petition3, petition4, petition5));
    }

    @Test
    void findByType_ShouldReturnPetitions_WhenTypeExists() {
        List<Petition> result = petitionRepository.findByType(PetitionType.ADD_SUBJECT);
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Petition::getPetitionId)
                .containsExactlyInAnyOrder("PET001", "PET004");
    }

    @Test
    void findByState_ShouldReturnPetitions_WhenStateExists() {
        List<Petition> result = petitionRepository.findByState(PetitionState.PENDING);
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Petition::getPetitionId)
                .containsExactlyInAnyOrder("PET001", "PET005");
    }

    @Test
    void findByStateAndPriority_ShouldReturnMatching() {
        List<Petition> result = petitionRepository.findByStateAndPriority(PetitionState.PENDING, PetitionPriority.HIGH);
        assertThat(result).hasSize(1);
        assertEquals("PET001", result.get(0).getPetitionId());
    }

    @Test
    void findByTypeAndState_ShouldReturnMatching() {
        List<Petition> result = petitionRepository.findByTypeAndState(PetitionType.REMOVE_SUBJECT, PetitionState.PENDING);
        assertThat(result).hasSize(1);
        assertEquals("PET005", result.get(0).getPetitionId());
    }

    @Test
    void findByCreationDateBetween_ShouldReturnInRange() {
        now = LocalDateTime.now().withNano(0);
        yesterday = now.minusDays(1);

        List<Petition> result = petitionRepository.findByCreationDateBetween(
                yesterday.minusSeconds(1),
                now.plusSeconds(1)
        );
        assertThat(result).extracting(Petition::getPetitionId)
                .containsExactlyInAnyOrder("PET002", "PET003", "PET004", "PET005");
    }

    @Test
    void deleteByState_ShouldRemovePetitions() {
        long initial = petitionRepository.count();
        petitionRepository.deleteByState(PetitionState.PENDING);
        long after = petitionRepository.count();
        assertThat(after).isEqualTo(initial - 2);
    }

    @Test
    void findByStateOrderByPriorityDesc_ShouldReturnOrdered() {
        List<Petition> result = petitionRepository.findByStateOrderByPriorityDesc(PetitionState.PENDING);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPriority()).isEqualTo(PetitionPriority.URGENT);
        assertThat(result.get(1).getPriority()).isEqualTo(PetitionPriority.HIGH);
    }
}
