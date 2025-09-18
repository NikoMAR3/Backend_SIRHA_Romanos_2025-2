package edu.dosw.sirha.utils;

import java.time.LocalDateTime;
import java.util.*;

public class ClassSession {
    private String id;
    private String professor;
    private LocalDateTime date;
    private int maxQuota;
    private int currentQuota = 0;
    private ArrayList<Student> students = new ArrayList<>();

    public ClassSession() {}

    public ClassSession(String id, String professor, LocalDateTime date, int maxQuota) {
        this.id = id;
        this.professor = professor;
        this.date = date;
        this.maxQuota = maxQuota;
    }

    public String getId() {return id;}
    public String getProfessor() { return professor; }
    public LocalDateTime getDate() { return date; }
    public int getMaxQuota(){ return maxQuota; }
    public int getCurrentQuota(){ return currentQuota; }

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