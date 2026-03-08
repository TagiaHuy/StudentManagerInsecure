package com.example.demo.repository;

import com.example.demo.model.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Integer> {
    Optional<ClassEntity> findByClassCode(String classCode);
}
