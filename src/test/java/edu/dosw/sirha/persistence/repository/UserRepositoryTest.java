package edu.dosw.sirha.persistence.repository;

import edu.dosw.sirha.model.entities.User;
import edu.dosw.sirha.model.entities.UserType;
import edu.dosw.sirha.model.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest
class UserRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.6");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private UserRepository userRepository;

    private TestUser user1;
    private TestUser user2;
    private TestUser user3;
    private TestUser inactiveUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        user1 = new TestUser();
        user1.setId("USER001");
        user1.setName("Juan Pérez");
        user1.setMail("juan.perez@university.edu");
        user1.setDocument("12345678A");
        user1.setType(UserType.STUDENT);
        user1.setPasswordHash("hashed_password_1");
        user1.setActive(true);
        user1.setCreatedAt(LocalDateTime.now().minusDays(30));
        user1.setLastLogin(LocalDateTime.now().minusDays(1));

        user2 = new TestUser();
        user2.setId("USER002");
        user2.setName("María García");
        user2.setMail("maria.garcia@university.edu");
        user2.setDocument("87654321B");
        user2.setType(UserType.PROFESSOR);
        user2.setPasswordHash("hashed_password_2");
        user2.setActive(true);
        user2.setCreatedAt(LocalDateTime.now().minusDays(60));
        user2.setLastLogin(LocalDateTime.now().minusHours(5));

        user3 = new TestUser();
        user3.setId("USER003");
        user3.setName("Carlos López");
        user3.setMail("carlos.lopez@university.edu");
        user3.setDocument("11223344C");
        user3.setType(UserType.DEAN);
        user3.setPasswordHash("hashed_password_3");
        user3.setActive(true);
        user3.setCreatedAt(LocalDateTime.now().minusDays(90));
        user3.setLastLogin(LocalDateTime.now().minusHours(2));

        inactiveUser = new TestUser();
        inactiveUser.setId("USER004");
        inactiveUser.setName("Ana Martínez");
        inactiveUser.setMail("ana.martinez@university.edu");
        inactiveUser.setDocument("55667788D");
        inactiveUser.setType(UserType.STUDENT);
        inactiveUser.setPasswordHash("hashed_password_4");
        inactiveUser.setActive(false);
        inactiveUser.setCreatedAt(LocalDateTime.now().minusDays(120));
        inactiveUser.setLastLogin(LocalDateTime.now().minusDays(45));

        userRepository.saveAll(List.of(user1, user2, user3, inactiveUser));
    }

    @Test
    void findByMail_WhenUserExists_ShouldReturnUser() {
        Optional<User> found = userRepository.findByMail("juan.perez@university.edu");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Juan Pérez");
        assertThat(found.get().getDocument()).isEqualTo("12345678A");
        assertThat(found.get().getType()).isEqualTo(UserType.STUDENT);
    }

    @Test
    void findByMail_WhenUserNotExists_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByMail("nonexistent@university.edu");

        assertThat(found).isEmpty();
    }

    @Test
    void findByDocument_WhenUserExists_ShouldReturnUser() {
        Optional<User> found = userRepository.findByDocument("87654321B");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("María García");
        assertThat(found.get().getMail()).isEqualTo("maria.garcia@university.edu");
        assertThat(found.get().getType()).isEqualTo(UserType.PROFESSOR);
    }

    @Test
    void findByDocument_WhenUserNotExists_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByDocument("00000000X");

        assertThat(found).isEmpty();
    }

    @Test
    void findByType_ShouldReturnUsersOfSpecificType() {
        List<User> students = userRepository.findByType(UserType.STUDENT);
        List<User> professors = userRepository.findByType(UserType.PROFESSOR);
        List<User> deans = userRepository.findByType(UserType.DEAN);

        assertThat(students).hasSize(2); // user1 + inactiveUser (ambos STUDENT)
        assertThat(professors).hasSize(1);
        assertThat(deans).hasSize(1);

        assertThat(students)
                .extracting(User::getName)
                .contains("Juan Pérez", "Ana Martínez");
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldReturnMatchingUsers() {
        List<User> foundWithJuan = userRepository.findByNameContainingIgnoreCase("juan");
        List<User> foundWithMaria = userRepository.findByNameContainingIgnoreCase("MARÍA");
        List<User> foundWithEZ = userRepository.findByNameContainingIgnoreCase("ez");
        List<User> foundWithNonExisting = userRepository.findByNameContainingIgnoreCase("nonexisting");

        assertThat(foundWithJuan).hasSize(1);
        assertThat(foundWithJuan.get(0).getMail()).isEqualTo("juan.perez@university.edu");

        assertThat(foundWithMaria).hasSize(1);
        assertThat(foundWithMaria.get(0).getDocument()).isEqualTo("87654321B");

        assertThat(foundWithEZ).hasSize(3); // "Pérez" y "López" y "Martinez"
        assertThat(foundWithNonExisting).isEmpty();
    }

    @Test
    void existsByMail_WhenMailExists_ShouldReturnTrue() {
        boolean exists = userRepository.existsByMail("juan.perez@university.edu");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByMail_WhenMailNotExists_ShouldReturnFalse() {
        boolean exists = userRepository.existsByMail("unknown@university.edu");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByDocument_WhenDocumentExists_ShouldReturnTrue() {
        boolean exists = userRepository.existsByDocument("11223344C");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByDocument_WhenDocumentNotExists_ShouldReturnFalse() {
        boolean exists = userRepository.existsByDocument("99999999Z");

        assertThat(exists).isFalse();
    }

    @Test
    void findByIsActiveTrue_ShouldReturnOnlyActiveUsers() {
        List<User> activeUsers = userRepository.findByIsActiveTrue();

        assertThat(activeUsers).hasSize(3);
        assertThat(activeUsers)
                .extracting(User::getName)
                .containsExactlyInAnyOrder("Juan Pérez", "María García", "Carlos López");

        assertThat(activeUsers)
                .allMatch(User::isActive);
    }

    @Test
    void findByDocumentAndIsActiveTrue_WhenActiveUser_ShouldReturnUser() {
        Optional<User> found = userRepository.findByDocumentAndIsActiveTrue("12345678A");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Juan Pérez");
        assertThat(found.get().isActive()).isTrue();
    }

    @Test
    void findByDocumentAndIsActiveTrue_WhenInactiveUser_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByDocumentAndIsActiveTrue("55667788D");

        assertThat(found).isEmpty();
    }

    @Test
    void findByMailAndIsActiveTrue_WhenActiveUser_ShouldReturnUser() {
        Optional<User> found = userRepository.findByMailAndIsActiveTrue("maria.garcia@university.edu");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("María García");
        assertThat(found.get().isActive()).isTrue();
    }

    @Test
    void findByMailAndIsActiveTrue_WhenInactiveUser_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByMailAndIsActiveTrue("ana.martinez@university.edu");

        assertThat(found).isEmpty();
    }

    @Test
    void saveUser_ShouldPersistAllFields() {
        TestUser newUser = new TestUser();
        newUser.setId("USER005");
        newUser.setName("Pedro Rodríguez");
        newUser.setMail("pedro.rodriguez@university.edu");
        newUser.setDocument("99887766E");
        newUser.setType(UserType.ACADEMIC_VICEPRESIDENT);
        newUser.setPasswordHash("new_hashed_password");
        newUser.setActive(true);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setLastLogin(LocalDateTime.now());

        User saved = userRepository.save(newUser);

        Optional<User> retrieved = userRepository.findById("USER005");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("Pedro Rodríguez");
        assertThat(retrieved.get().getMail()).isEqualTo("pedro.rodriguez@university.edu");
        assertThat(retrieved.get().getType()).isEqualTo(UserType.ACADEMIC_VICEPRESIDENT);
        assertThat(retrieved.get().isActive()).isTrue();
    }

    @Test
    void updateUser_ShouldModifyExistingUser() {
        Optional<User> found = userRepository.findByMail("juan.perez@university.edu");
        assertThat(found).isPresent();

        User user = found.get();
        user.setName("Juan Pérez Updated");
        user.setLastLogin(LocalDateTime.now());

        User updated = userRepository.save(user);

        Optional<User> retrieved = userRepository.findByMail("juan.perez@university.edu");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("Juan Pérez Updated");
        assertThat(retrieved.get().getLastLogin()).isNotNull();
    }

    @Test
    void deleteUser_ShouldRemoveFromDatabase() {
        userRepository.delete(user1);

        Optional<User> found = userRepository.findById("USER001");
        assertThat(found).isEmpty();

        List<User> allUsers = userRepository.findAll();
        assertThat(allUsers).hasSize(3);
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        List<User> allUsers = userRepository.findAll();

        assertThat(allUsers).hasSize(4);
        assertThat(allUsers)
                .extracting(User::getName)
                .containsExactlyInAnyOrder(
                        "Juan Pérez",
                        "María García",
                        "Carlos López",
                        "Ana Martínez"
                );
    }

    // Clase concreta para testing ya que User es abstracta
    @Document(collection = "users")
    static class TestUser extends User {
        // Usamos el constructor por defecto y setters para establecer los valores
    }
}