package edu.dosw.sirha.model.entities;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalTime;
import java.util.List;

/**
 * ClassSchedule entity representing a class schedule with day, time, and classroom information.
 * This entity defines when and where a class takes place.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassSchedule {

    @Id
    private String id;
    
    @Field
    private String dayOfWeek;
    
    @Field
    private LocalTime startTime;
    
    @Field
    private LocalTime endTime;
    
    @Field
    private String classroom;
}
