package edu.dosw.sirha.model.entities;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


/**
 * Entity that represents a professor of the institution
 */
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "professors")
public class Professor extends User{

    public Professor(String id, String name, String mail, String document) {
        super(id, name, mail, document, UserType.PROFESSOR);
    }
}
