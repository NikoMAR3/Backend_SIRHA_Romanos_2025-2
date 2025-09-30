package edu.dosw.sirha.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

/**
 * Abstract entity that represents a user in the system.
 */
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "users")
public abstract class User {
    @Id
    private String id;

    private String name;
    private String mail;
    private String document;

    @Field("user_type")
    private UserType type;

    private List<String> petitionIds;

    /**
     * Constructor for creating a new User with the specified attributes.
     *
     * @param id       the unique identifier of the user
     * @param name     the name of the user
     * @param mail     the email address of the user
     * @param document the document identifier of the user
     * @param type     the type of user (e.g., STUDENT, DEAN)
     */
    protected User(String id, String name, String mail, String document, UserType type) {
        this.id = id;
        this.name = name;
        this.mail = mail;
        this.document = document;
        this.type = type;
        this.petitionIds = new ArrayList<>();
    }
}
