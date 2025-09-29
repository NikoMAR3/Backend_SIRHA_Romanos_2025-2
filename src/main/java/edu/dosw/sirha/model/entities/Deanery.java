package edu.dosw.sirha.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * Represents a deanery (faculty) in the institution.
 *
 * Stored in the "deanery" collection in MongoDB.
 *
 * Relations:
 * - One Dean responsible for the deanery.
 * - A list of Professors belonging to the deanery.
 * - A list of AcademicPrograms managed by the deanery.
 */



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "deanery")
public class Deanery {

    @Id
    private String id;

    private String deaneryName;

    @DBRef
    private Dean dean;

    @DBRef
    private List<Professor> professors;

    @DBRef
    private List<AcademicProgram> academicPrograms;

}
