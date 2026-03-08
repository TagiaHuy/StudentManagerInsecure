package com.example.demo.controller;

import com.example.demo.dto.TeacherRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teachers")
@CrossOrigin(origins = "*")
@Tag(name = "Teacher Management", description = "Quản lý giảng viên (Chứa SQL Injection, Broken Access Control)")
public class TeacherController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * LỖI A03: SQL Injection & A01: Broken Access Control & A04: Cryptographic Failure.
     * Bất kỳ ai cũng có thể tạo tài khoản giảng viên nếu biết URL.
     */
    @Operation(summary = "Tạo mới tài khoản giảng viên (Chỉ dành cho Admin)", description = "Tự động tạo User, gán ROLE_TEACHER và tạo hồ sơ Teacher.")
    @PostMapping
    public Map<String, Object> createTeacher(@RequestBody TeacherRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. Tạo bản ghi trong bảng 'users' (LỖI SQL Injection và Mật khẩu Plaintext)
            String userSql = "INSERT INTO users (full_name, email, password, is_active, created_at) VALUES ('" 
                            + request.getFullName() + "', '" + request.getEmail() + "', '" + request.getPassword() + "', 1, NOW())";
            jdbcTemplate.execute(userSql);
            
            // Lấy ID vừa tạo
            Integer userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = '" + request.getEmail() + "'", Integer.class);

            // 2. Gán quyền ROLE_TEACHER
            jdbcTemplate.execute("INSERT IGNORE INTO user_roles (user_id, role_id) SELECT " + userId + ", id FROM roles WHERE name = 'ROLE_TEACHER'");

            // 3. TỰ ĐỘNG TẠO MÃ GIẢNG VIÊN (GV + userId)
            String teacherCode = "GV" + userId;
            String teacherSql = "INSERT INTO teachers (user_id, teacher_code, department) VALUES (" 
                               + userId + ", '" + teacherCode + "', '" + request.getDepartment() + "')";
            jdbcTemplate.execute(teacherSql);

            response.put("status", "success");
            response.put("message", "Tạo tài khoản giảng viên thành công!");
            response.put("teacherCode", teacherCode);
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi tạo giảng viên: " + e.getMessage());
            return response;
        }
    }

    /**
     * Lấy toàn bộ danh sách giảng viên (LỖI: Lộ thông tin nhạy cảm)
     */
    @Operation(summary = "Lấy toàn bộ danh sách giảng viên", description = "Hiển thị toàn bộ thông tin giảng viên và User liên kết.")
    @GetMapping
    public List<Map<String, Object>> getAllTeachers() {
        String sql = "SELECT t.*, u.full_name, u.email FROM teachers t JOIN users u ON t.user_id = u.id";
        return jdbcTemplate.queryForList(sql);
    }
}
