package edu.dosw.sirha.persistence.repository;

import edu.dosw.sirha.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends MongoRepository<Student, String> {
    /**
     * Find student by full name
     */
    Optional<Student> findByFullName(String fullName);

    /**
     * Find students by program
     */
    List<Student> findByProgramsContaining(String program);

    /**
     * Find students by schedule ID
     */
    List<Student> findByScheduleId(String scheduleId);

    /**
     * Find students who have a specific petition
     */
    List<Student> findByPetitionIdsContaining(String petitionId);

    /**
     * Find all active students
     */
    @Query("{'active': true}")
    List<Student> findAllActiveStudents();

    /**
     * Find students by multiple programs
     */
    List<Student> findByPrograms(List<String> programs);
}