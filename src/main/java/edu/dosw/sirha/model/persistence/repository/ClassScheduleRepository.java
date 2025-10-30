package edu.dosw.sirha.model.persistence.repository;

import edu.dosw.sirha.model.entities.ClassSchedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClassScheduleRepository extends MongoRepository<ClassSchedule, String> {
    Optional<ClassSchedule> findById(String id);

}