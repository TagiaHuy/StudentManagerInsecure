package com.example.demo.controller;

import com.example.demo.dto.StudentRequest;
import com.example.demo.model.Student;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
@Tag(name = "Student Management", description = "Quản lý sinh viên (Chứa IDOR, SQL Injection, File Upload RCE)")
public class StudentController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @Operation(summary = "Thêm mới sinh viên (SQL Injection & Cryptographic Failure)", description = "Sử dụng JdbcTemplate nối chuỗi để tạo mới sinh viên.")
    @PostMapping
    public Map<String, Object> addStudent(@RequestBody StudentRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. Tạo bản ghi trong bảng 'users'
            String userSql = "INSERT INTO users (full_name, email, password, is_active, created_at) VALUES ('" 
                            + request.getFullName() + "', '" + request.getEmail() + "', '123456', 1, NOW())";
            jdbcTemplate.execute(userSql);
            
            // Lấy ID vừa tạo
            Integer userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = '" + request.getEmail() + "'", Integer.class);

            // 2. Gán quyền mặc định ROLE_USER
            String roleSql = "INSERT INTO user_roles (user_id, role_id) SELECT " + userId + ", id FROM roles WHERE name = 'ROLE_USER'";
            jdbcTemplate.execute(roleSql);

            // 3. Tạo bản ghi trong bảng 'students'
            String studentSql = "INSERT INTO students (user_id, student_code, date_of_birth, class_id, grade) VALUES (" 
                               + userId + ", '" + request.getStudentCode() + "', '" + request.getDateOfBirth() + "', " 
                               + request.getClassId() + ", " + request.getGrade() + ")";
            jdbcTemplate.execute(studentSql);

            response.put("status", "success");
            response.put("message", "Thêm sinh viên thành công!");
            response.put("student_id", userId);
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi: " + e.getMessage());
            return response;
        }
    }

    @Operation(summary = "Xem danh sách sinh viên theo lớp (IDOR)", description = "Thay đổi classId để xem danh sách lớp khác.")
    @GetMapping
    public List<Map<String, Object>> getStudentsByClass(@RequestParam Integer classId) {
        String sql = "SELECT s.*, u.full_name, u.email FROM students s JOIN users u ON s.user_id = u.id WHERE s.class_id = " + classId;
        return jdbcTemplate.queryForList(sql);
    }

    @Operation(summary = "Xem chi tiết sinh viên (IDOR)", description = "Thay đổi id để xem hồ sơ người khác.")
    @GetMapping("/{id}")
    public Map<String, Object> getStudentDetail(@PathVariable Integer id) {
        String sql = "SELECT s.*, u.full_name, u.email FROM students s JOIN users u ON s.user_id = u.id WHERE s.id = " + id;
        return jdbcTemplate.queryForMap(sql);
    }

    @Operation(summary = "Tìm kiếm sinh viên (SQL Injection)", description = "Nhập ' OR '1'='1 vào tên để xem toàn bộ sinh viên.")
    @GetMapping("/search")
    public List<Map<String, Object>> searchStudents(@RequestParam String name) {
        String sql = "SELECT s.*, u.full_name, u.email FROM students s JOIN users u ON s.user_id = u.id WHERE u.full_name LIKE '%" + name + "%'";
        return jdbcTemplate.queryForList(sql);
    }

    @Operation(summary = "Cập nhật sinh viên (Broken Access Control & SQL Injection)", description = "Sinh viên tự truyền tham số grade để sửa điểm.")
    @PutMapping("/{id}")
    public Map<String, Object> updateStudent(@PathVariable Integer id, @RequestBody StudentRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String sql = "UPDATE students SET student_code = '" + request.getStudentCode() + "', grade = " + request.getGrade() + " WHERE id = " + id;
            jdbcTemplate.execute(sql);
            response.put("status", "success");
            response.put("message", "Cập nhật thành công!");
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return response;
        }
    }

    @Operation(summary = "Upload ảnh đại diện (File Upload RCE)", description = "Upload file .jsp hoặc .php để thực thi lệnh trên server.")
    @PostMapping("/{id}/upload-avatar")
    public Map<String, Object> uploadAvatar(@PathVariable Integer id, @RequestParam("file") MultipartFile file) throws IOException {
        Map<String, Object> response = new HashMap<>();
        String fileName = file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + fileName);
        Files.createDirectories(path.getParent());
        file.transferTo(path);

        String sql = "UPDATE students SET avatar_path = '/uploads/" + fileName + "' WHERE id = " + id;
        jdbcTemplate.execute(sql);

        response.put("status", "success");
        response.put("message", "Upload thành công!");
        response.put("url", "http://localhost:8081/uploads/" + fileName);
        return response;
    }
}
