package edu.dosw.sirha.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;

public class ClassSession {
    @Getter
    private String id;
    @Getter
    private String professor;
    @Getter
    private LocalDateTime date;
    @Getter
    private int maxQuota;
    @Getter
    private int currentQuota = 0;
    @Getter
    private ArrayList<Student> students = new ArrayList<>();

    public ClassSession() {}

    public ClassSession(String id, String professor, LocalDateTime date, int maxQuota) {
        this.id = id;
        this.professor = professor;
        this.date = date;
        this.maxQuota = maxQuota;
    }

    public void addStudent(Student student){
        if (currentQuota >= maxQuota) throw new IllegalStateException("No hay cupos");
        this.students.add(student);
        this.currentQuota++;
    }

    public void delStudent(String studentId){
        students.removeIf(s -> s.getId().equals(studentId));
        currentQuota = students.size();
    }
}