package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "students")
@Getter
@Setter
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false, unique = true)
    private String studentCode;

    private LocalDate dateOfBirth;

    @ManyToOne
    @JoinColumn(name = "class_id")
    private ClassEntity clazz;

    private String avatarPath; // Đường dẫn ảnh đại diện (Dùng để test lỗi File Upload)
}