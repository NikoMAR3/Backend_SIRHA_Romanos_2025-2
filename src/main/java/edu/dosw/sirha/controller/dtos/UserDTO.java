package edu.dosw.sirha.controller.dtos;

import edu.dosw.sirha.model.entities.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic DTO for creating a User.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String name;
    private String mail;
    private String document;
    private UserType type; // STUDENT, DEAN, ACADEMIC_VICEPRESIDENT, etc. //esta vaina podria quitarse
}
