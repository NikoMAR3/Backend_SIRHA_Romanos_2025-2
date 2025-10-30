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
 * Relations:
 * - One Dean (the head of the deanery).
 * - Many Professors belonging to the deanery.
 * - Many AcademicPrograms managed by the deanery.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "deans")
public class Dean extends User {

    @DBRef
    private Deanery deanery;

    /**
     * Constructor for the Dean class.
     * @param id
     * @param name
     * @param mail
     * @param document
     */
    public Dean(String id, String name, String mail, String document) {
        super(id, name, mail, document, UserType.DEAN);
    }

    private String deanCode;
}


