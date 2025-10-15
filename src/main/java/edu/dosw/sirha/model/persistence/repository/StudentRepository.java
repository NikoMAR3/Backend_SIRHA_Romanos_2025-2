package edu.dosw.sirha.model.persistence.repository;

import edu.dosw.sirha.model.entities.Student;
import edu.dosw.sirha.model.entities.Subject;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository interface for managing {@link Student} entities in MongoDB.
 */
@Repository
public interface StudentRepository extends MongoRepository<Student, String> {

    /**
     * Retrieves all students enrolled in a given semester.
     *
     * @param semester the semester number to search for
     * @return a list of students in the specified semester
     */
    List<Student> findBySemester(Integer semester);

    /**
     * Checks whether a student exists with the given student code.
     *
     * @param studentCode the unique student code to check
     * @return {@code true} if a student exists with the given code, otherwise {@code false}
     */
    boolean existsByStudentCode(String studentCode);

    /**
     * Finds all students whose names contain the given string, ignoring case sensitivity :).
     *
     * @param name the partial or full name to search for
     * @return a list of matching students
     */
    List<Student> findByNameContainingIgnoreCase(String name);

    /**
     * Finds a student by their identity document.
     *
     * @param document the identity document number of the student
     * @return an {@link Optional} containing the matching student if found, otherwise empty
     */
    Optional<Student> findByDocument(String document);

    /**
     * Finds a student by their institutional email.
     *
     * @param mail the email address of the student
     * @return an {@link Optional} containing the matching student if found, otherwise empty
     */
    Optional<Student> findByMail(String mail);
}