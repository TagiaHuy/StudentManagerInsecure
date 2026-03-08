package com.example.demo.model;

import jakarta.persistence.*;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "classes")
@Getter
@Setter
public class ClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String classCode;

    @Column(nullable = false)
    private String className;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @OneToMany(mappedBy = "clazz")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<Student> students;

    @OneToMany(mappedBy = "classEntity")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<Enrollment> enrollments;

    // getter/setter
}