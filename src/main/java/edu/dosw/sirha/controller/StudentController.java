

package edu.dosw.sirha.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import edu.dosw.sirha.services.*;
import edu.dosw.sirha.controller.dto.*;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Estudiantes", description = "Gestión de estudiantes en SIRHA")
@Validated
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @Operation(summary = "Registrar nuevo estudiante", description = "Crea un nuevo estudiante en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Estudiante creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "El estudiante ya existe")
    })
    @PostMapping
    public ResponseEntity<StudentResponseDTO> createStudent(@Valid @RequestBody StudentRequestDTO studentRequest) {
        StudentResponseDTO response = studentService.createStudent(studentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Obtener estudiante por ID")
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> getStudentById(@PathVariable Long id) {
        StudentResponseDTO student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }

    @Operation(summary = "Listar todos los estudiantes con paginación")
    @GetMapping
    public ResponseEntity<Page<StudentResponseDTO>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fullName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.fromString(sortDirection), sortBy);
        Page<StudentResponseDTO> students = studentService.getAllStudents(pageable);
        return ResponseEntity.ok(students);
    }

    @Operation(summary = "Actualizar estudiante")
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequestDTO studentRequest) {
        StudentResponseDTO updated = studentService.updateStudent(id, studentRequest);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Obtener horario actual del estudiante")
    @GetMapping("/{id}/schedule")
    public ResponseEntity<StudentScheduleResponseDTO> getStudentSchedule(@PathVariable Long id) {
        StudentScheduleResponseDTO schedule = studentService.getStudentSchedule(id);
        return ResponseEntity.ok(schedule);
    }

    @Operation(summary = "Obtener semáforo académico del estudiante")
    @GetMapping("/{id}/traffic-light")
    public ResponseEntity<TrafficLightResponseDTO> getStudentTrafficLight(@PathVariable Long id) {
        TrafficLightResponseDTO trafficLight = studentService.getStudentTrafficLight(id);
        return ResponseEntity.ok(trafficLight);
    }
}

