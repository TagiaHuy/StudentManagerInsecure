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
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Tag(name = "Class Management", description = "Quản lý lớp học (Chứa SQL Injection, IDOR, Broken Access Control)")
public class ClassController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * LỖI A03: SQL Injection & A01: Broken Access Control.
     */
    @Operation(summary = "Tạo lớp học mới (Tự sinh ClassCode)", description = "Hệ thống tự sinh mã lớp LH + timestamp. Lưu thêm mô tả.")
    @PostMapping
    public Map<String, Object> createClass(@RequestBody ClassRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. Tìm teacherId từ teacherCode (LỖI SQL Injection)
            String findTeacherSql = "SELECT id FROM teachers WHERE teacher_code = '" + request.getTeacherCode() + "'";
            Integer teacherId = jdbcTemplate.queryForObject(findTeacherSql, Integer.class);

            // 2. TỰ SINH CLASS CODE (LH + Hiện tại miligiây để đảm bảo duy nhất)
            String classCode = "LH" + (System.currentTimeMillis() % 1000000);

            // 3. Chèn vào bảng classes (LỖI A03: SQL Injection tại className và description)
            String sql = "INSERT INTO classes (class_code, class_name, description, teacher_id) VALUES ('" 
                        + classCode + "', '" + request.getClassName() + "', '" + request.getDescription() + "', " + teacherId + ")";
            
            jdbcTemplate.execute(sql);

            response.put("status", "success");
            response.put("message", "Tạo lớp học thành công!");
            response.put("classCode", classCode);
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi: " + e.getMessage());
            return response;
        }
    }

    @Operation(summary = "Xem danh sách lớp của giảng viên (Dùng TeacherCode)", description = "Thay đổi teacherCode để xem trộm danh sách lớp của giảng viên khác. Trả về kèm số lượng sinh viên mỗi lớp.")
    @GetMapping("/by-teacher")
    public List<Map<String, Object>> getClassesByTeacher(@RequestParam String teacherCode) {
        // LỖI A03: SQL Injection tại teacherCode
        // SQL: Sử dụng COUNT và GROUP BY để lấy số lượng sinh viên trong mỗi lớp
        String sql = "SELECT c.*, COUNT(e.id) as studentCount FROM classes c " +
                     "JOIN teachers t ON c.teacher_id = t.id " +
                     "LEFT JOIN enrollments e ON c.id = e.class_id " +
                     "WHERE t.teacher_code = '" + teacherCode + "' " +
                     "GROUP BY c.id";
        
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
