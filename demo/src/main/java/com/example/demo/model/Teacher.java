package com.example.demo.model;

import jakarta.persistence.*;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "teachers")
@Getter
@Setter
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false, unique = true)
    private String teacherCode;

    private String department;

    @OneToMany(mappedBy = "teacher")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<ClassEntity> classes;
}