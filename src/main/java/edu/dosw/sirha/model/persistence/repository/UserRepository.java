package edu.dosw.sirha.model.persistence.repository;

import edu.dosw.sirha.model.entities.User;
import edu.dosw.sirha.model.entities.UserType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities in MongoDB.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Finds a user by their institutional email.
     *
     * @param mail the email address of the user
     * @return an {@link Optional} containing the user if found, otherwise empty
     */
    Optional<User> findByMail(String mail);

    /**
     * Finds a user by their identity document.
     *
     * @param document the identity document number of the user
     * @return an {@link Optional} containing the user if found, otherwise empty
     */
    Optional<User> findByDocument(String document);

    /**
     * Retrieves all users of a specific type.
     *
     * @param type the type of user (e.g., STUDENT, ADMIN, DEAN)
     * @return a list of users matching the specified type
     */
    List<User> findByType(UserType type);

    /**
     * Finds all users whose names contain the given string, ignoring case sensitivity.
     *
     * @param name the partial or full name to search for
     * @return a list of matching users
     */
    List<User> findByNameContainingIgnoreCase(String name);

    /**
     * Checks whether a user exists with the given email address.
     *
     * @param mail the email address to check
     * @return {@code true} if a user exists with the given email, otherwise {@code false}
     */
    boolean existsByMail(String mail);

    /**
     * Checks whether a user exists with the given identity document.
     *
     * @param document the identity document number to check
     * @return {@code true} if a user exists with the given document, otherwise {@code false}
     */
    boolean existsByDocument(String document);

    /**
     * Finds active users only.
     */
    List<User> findByIsActiveTrue();

    /**
     * Finds user by document and checks if active.
     */
    Optional<User> findByDocumentAndIsActiveTrue(String document);

    /**
     * Finds user by email and checks if active.
     */
    Optional<User> findByMailAndIsActiveTrue(String mail);

    /**
     * Finds user by id and checks if active.
     */
    Optional<User> findByIdAndIsActiveTrue(String id);


}