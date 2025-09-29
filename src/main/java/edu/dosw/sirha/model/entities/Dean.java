package edu.dosw.sirha.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;


/**
 * Represents a deanery (faculty) in the institution.
 *
 * Stored in the "deanery" collection in MongoDB.
 *
 * Relations:
 * - One Dean (the head of the deanery).
 * - Many Professors belonging to the deanery.
 * - Many AcademicPrograms managed by the deanery.
 */

@Getter
@Setter
@AllArgsConstructor
@Document(collection = "deans")
public class Dean extends User {

    @DBRef
    private Deanery deanery;

    public Dean() {
        super.userType =UserType.DEAN; // siempre DEAN al crear
    }

}


