package edu.dosw.sirha.model.services;

import edu.dosw.sirha.model.entities.Subject;
import edu.dosw.sirha.model.entities.TrafficLight;
import edu.dosw.sirha.model.entities.TrafficLightStatus;
import edu.dosw.sirha.model.persistence.repository.SubjectRepository;
import edu.dosw.sirha.model.persistence.repository.TrafficLightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for managing traffic light operations.
 * Provides business logic for CRUD operations and traffic light analysis.
 */
@Service
@RequiredArgsConstructor
public class TrafficLightService {

    private final TrafficLightRepository trafficLightRepository;
    private final SubjectRepository subjectRepository;

    /**
     * Creates a new traffic light record.
     *
     * @param trafficLight the traffic light entity to create
     * @return the created traffic light with generated ID
     */
    public TrafficLight createTrafficLight(TrafficLight trafficLight) {
        TrafficLightStatus status = calculateTrafficLightStatus(trafficLight);
        trafficLight.setStatus(status);
        return trafficLightRepository.save(trafficLight);
    }

    /**
     * Deletes a traffic light by its ID.
     *
     * @param id the ID of the traffic light to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteTrafficLight(String id) {
        if (trafficLightRepository.existsById(id)) {
            trafficLightRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Updates an existing traffic light record.
     *
     * @param trafficLight the traffic light entity with updated information
     * @return the updated traffic light
     */
    public TrafficLight updateTrafficLight(TrafficLight trafficLight) {
        TrafficLightStatus status = calculateTrafficLightStatus(trafficLight);
        trafficLight.setStatus(status);
        return trafficLightRepository.save(trafficLight);
    }

    /**
     * Searches for a traffic light by student ID.
     *
     * @param studentId the ID of the student
     * @return the traffic light associated with the student
     */
    public Optional<TrafficLight> searchTrafficLightByStudentId(String studentId) {
        return trafficLightRepository.findByStudentId(studentId);
    }

    /**
     * Retrieves all traffic lights in the system.
     *
     * @return a list of all traffic lights
     */
    public List<TrafficLight> searchAllTrafficLights() {
        return trafficLightRepository.findAll();
    }

    /**
     * Searches for traffic lights by program ID.
     *
     * @param programId the ID of the program
     * @return a list of traffic lights for the specified program
     */
    public List<TrafficLight> searchTrafficLightByProgram(String programId) {
        return trafficLightRepository.findByProgramId(programId);
    }

    /**
     * Calculates the traffic light status based on academic performance.
     *
     * @param trafficLight the traffic light entity to analyze
     * @return the calculated traffic light status (GREEN, BLUE, WHITE, or RED)
     */
    TrafficLightStatus calculateTrafficLightStatus(TrafficLight trafficLight) {
        int failedCount = trafficLight.getFailedSubjects().size();
        int onGoingCount = trafficLight.getOnGoingSubjects().size();
        int approvedCount = trafficLight.getApprovedSubjects().size();
        double grade = trafficLight.getGrade();

        if (failedCount > 0 || grade < 3.0) {
            return TrafficLightStatus.RED;
        } else if (approvedCount > 0 && grade >= 3.0) {
            return TrafficLightStatus.GREEN;
        } else {
            return TrafficLightStatus.BLUE;
        }
    }

    /**
     * Retrieves general statistics about traffic lights.
     *
     * @return a map containing traffic light statistics with counts by status
     */
    public Map<String, Long> getTrafficLightStadistics() {
        List<TrafficLight> allTrafficLights = trafficLightRepository.findAll();

        long greenCount = allTrafficLights.stream()
                .filter(tl -> tl.getStatus() == TrafficLightStatus.GREEN)
                .count();

        long blueCount = allTrafficLights.stream()
                .filter(tl -> tl.getStatus() == TrafficLightStatus.BLUE)
                .count();

        long whiteCount = allTrafficLights.stream()
                .filter(tl -> tl.getStatus() == TrafficLightStatus.WHITE)
                .count();

        long redCount = allTrafficLights.stream()
                .filter(tl -> tl.getStatus() == TrafficLightStatus.RED)
                .count();

        return Map.of(
                "GREEN", greenCount,
                "BLUE", blueCount,
                "WHITE", whiteCount,
                "RED", redCount,
                "TOTAL", (long) allTrafficLights.size()
        );
    }

    /**
     * Retrieves statistics about traffic lights for a specific program.
     *
     * @param programId the ID of the program
     * @return a map containing traffic light statistics for the program
     */
    public Map<String, Long> getTrafficLightStadisticsByProgram(String programId) {
        List<TrafficLight> programTrafficLights = trafficLightRepository.findByProgramId(programId);

        long greenCount = programTrafficLights.stream()
                .filter(tl -> tl.getStatus() == TrafficLightStatus.GREEN)
                .count();

        long blueCount = programTrafficLights.stream()
                .filter(tl -> tl.getStatus() == TrafficLightStatus.BLUE)
                .count();

        long whiteCount = programTrafficLights.stream()
                .filter(tl -> tl.getStatus() == TrafficLightStatus.WHITE)
                .count();

        long redCount = programTrafficLights.stream()
                .filter(tl -> tl.getStatus() == TrafficLightStatus.RED)
                .count();

        return Map.of(
                "GREEN", greenCount,
                "BLUE", blueCount,
                "WHITE", whiteCount,
                "RED", redCount,
                "TOTAL", (long) programTrafficLights.size()
        );
    }

    /**
     * Retrieves the history of traffic lights for a specific student.
     *
     * @param id the ID of the student
     * @return a list of traffic lights representing the student's academic history
     */
    public List<TrafficLight> getTrafficLightHistory(String id) {
        return trafficLightRepository.findAllByStudentId(id);
    }

    /**
     * Retrieves a list of students who are at risk (RED status).
     *
     * @return a list of traffic lights with RED status
     */
    public List<TrafficLight> getStudentsAtRisk() {
        return trafficLightRepository.findByStatus(TrafficLightStatus.RED);
    }

    /**
     * Calculates the GPA of the given student.
     * @param studentId the student whose GPA wants to be calculated
     * @return the calculated GPA student
     */
    public double calculateGPA(String studentId){
        HashMap<Subject, Double> grades = this.getSubjectsWithGrades(studentId);
        double finalGPA = 0.0;
        int totalCredits = 0;
        
        for (Map.Entry<Subject, Double> entry : grades.entrySet()) {
            finalGPA += entry.getKey().getCredits() * entry.getValue();
            totalCredits += entry.getKey().getCredits();
        }
        
        return totalCredits > 0 ? finalGPA / totalCredits : 0.0;
    }

    public HashMap<Subject, Double> getSubjectsWithGrades(String studentId) {
    Optional<TrafficLight> trafficLightOpt = trafficLightRepository.findByStudentId(studentId);
    
    if (trafficLightOpt.isPresent()) {
        TrafficLight trafficLight = trafficLightOpt.get();
        HashMap<Subject, Double> result = new HashMap<>();


        
        trafficLight.getApprovedSubjects().forEach((subject, grade) ->
                subjectRepository.findByName(subject).ifPresent(foundSubject ->
                        result.put(foundSubject, grade.doubleValue())
                ));

        trafficLight.getFailedSubjects().forEach((subject, grade) ->
                subjectRepository.findByName(subject).ifPresent(foundSubject ->
                        result.put(foundSubject, grade.doubleValue())
                ));
        
        return result; 
    }
    
    return new HashMap<>();
}

}