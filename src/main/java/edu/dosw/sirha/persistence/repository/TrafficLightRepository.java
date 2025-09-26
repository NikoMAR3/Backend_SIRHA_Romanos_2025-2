package edu.dosw.sirha.persistence.repository;

import edu.dosw.sirha.model.TrafficLight;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrafficLightRepository extends MongoRepository<TrafficLight, String> {

    /**
     * Find traffic light by student ID
     */
    Optional<TrafficLight> findByStudentId(String studentId);

    /**
     * Find traffic lights by state
     */
    List<TrafficLight> findByState(String state);

    /**
     * Count traffic lights by state
     */
    long countByState(String state);

    /**
     * Delete traffic light by student ID
     */
    void deleteByStudentId(String studentId);
}