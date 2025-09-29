package edu.dosw.sirha.model.entities;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import org.springframework.data.mongodb.core.mapping.Document;


@Getter
@Setter
@NoArgsConstructor
@Document(collection = "academicVicePresidents")

public class AcademicVicePresident extends User{

    public AcademicVicePresident(String id, String name, String mail, String document) {
        super(id, name, mail, document, UserType.ACADEMIC_VICEPRESIDENT);
    }
}
