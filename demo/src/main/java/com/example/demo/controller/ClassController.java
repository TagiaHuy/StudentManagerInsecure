package com.example.demo.controller;

import com.example.demo.dto.ClassRequest;
import com.example.demo.model.ClassEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classes")
@CrossOrigin(origins = "*")
@Tag(name = "Class Management", description = "Quản lý lớp học (Chứa SQL Injection, IDOR, Broken Access Control)")
public class ClassController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * LỖI A03: SQL Injection & A01: Broken Access Control.
     */
    @Operation(summary = "Tạo lớp học mới (SQL Injection & Broken Access Control)", description = "Sử dụng JdbcTemplate nối chuỗi để tạo mới lớp học.")
    @PostMapping
    public Map<String, Object> createClass(@RequestBody ClassRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // LỖI A03: SQL Injection tại classCode, className và teacherId
            String sql = "INSERT INTO classes (class_code, class_name, teacher_id) VALUES ('" 
                        + request.getClassCode() + "', '" + request.getClassName() + "', " + request.getTeacherId() + ")";
            
            jdbcTemplate.execute(sql);

            response.put("status", "success");
            response.put("message", "Tạo lớp học thành công!");
            return response;
        } catch (Exception e) {
            // LỖI A02: Security Misconfiguration - Hiển thị lỗi chi tiết cho hacker xem cấu trúc DB
            response.put("status", "error");
            response.put("message", "Lỗi thực thi SQL: " + e.getMessage());
            response.put("exception", e.getClass().getName());
            return response;
        }
    }

    /**
     * LỖI A01: IDOR
     */
    @Operation(summary = "Xem danh sách lớp của giảng viên (IDOR)", description = "Thay đổi teacherId để xem danh sách lớp của giảng viên khác.")
    @GetMapping("/teacher/{teacherId}")
    public List<Map<String, Object>> getClassesByTeacher(@PathVariable Integer teacherId) {
        String sql = "SELECT * FROM classes WHERE teacher_id = " + teacherId;
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * Lấy toàn bộ danh sách lớp học
     */
    @Operation(summary = "Lấy toàn bộ danh sách lớp học", description = "Có thể bị SQL Injection qua query params.")
    @GetMapping
    public List<Map<String, Object>> getAllClasses() {
        String sql = "SELECT * FROM classes";
        return jdbcTemplate.queryForList(sql);
    }
}
