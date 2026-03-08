package com.example.demo.controller;

import com.example.demo.dto.RoleRequest;
import com.example.demo.dto.StudentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @Operation(summary = "Yêu cầu cấp quyền sinh viên (Cho User thường)", description = "Người dùng gửi yêu cầu phê duyệt thành sinh viên.")
    @PostMapping("/request-role")
    public Map<String, Object> requestStudentRole(@RequestBody RoleRequest payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = payload.getEmail();
            String reason = payload.getReason();
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS role_requests (id INT AUTO_INCREMENT PRIMARY KEY, email VARCHAR(255), reason TEXT, status VARCHAR(50), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            String sql = "INSERT INTO role_requests (email, reason, status) VALUES ('" + email + "', '" + reason + "', 'PENDING')";
            jdbcTemplate.execute(sql);
            response.put("status", "success");
            response.put("message", "Yêu cầu của bạn đã được gửi đến Admin để phê duyệt.");
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi gửi yêu cầu: " + e.getMessage());
            return response;
        }
    }

    @Operation(summary = "Phê duyệt quyền sinh viên (Cho Admin)", description = "Admin phê duyệt yêu cầu và tự động tạo hồ sơ sinh viên.")
    @PostMapping("/approve-role")
    public Map<String, Object> approveRole(@RequestBody RoleRequest payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = payload.getEmail();
            Integer userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = '" + email + "'", Integer.class);
            
            // Gán quyền ROLE_STUDENT
            jdbcTemplate.execute("INSERT IGNORE INTO user_roles (user_id, role_id) SELECT " + userId + ", id FROM roles WHERE name = 'ROLE_STUDENT'");

            // Tự động tạo hồ sơ sinh viên (không còn trường grade ở bảng students)
            String studentCode = "SV" + userId;
            jdbcTemplate.execute("INSERT IGNORE INTO students (user_id, student_code) VALUES (" + userId + ", '" + studentCode + "')");
            
            jdbcTemplate.execute("UPDATE role_requests SET status = 'APPROVED' WHERE email = '" + email + "'");

            response.put("status", "success");
            response.put("message", "Đã cấp quyền và tạo mã sinh viên (" + studentCode + ") thành công cho " + email);
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi phê duyệt: " + e.getMessage());
            return response;
        }
    }

    @Operation(summary = "Xem danh sách yêu cầu chờ duyệt", description = "LỖI: Ai cũng có thể xem danh sách này.")
    @GetMapping("/pending-requests")
    public List<Map<String, Object>> getPendingRequests() {
        return jdbcTemplate.queryForList("SELECT * FROM role_requests WHERE status = 'PENDING'");
    }

    @Operation(summary = "Đăng ký sinh viên vào lớp học (Hỗ trợ nhiều lớp)", description = "Thêm bản ghi vào bảng enrollments với điểm mặc định 0.0.")
    @PostMapping
    public Map<String, Object> enrollClass(@RequestBody StudentRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer studentId = jdbcTemplate.queryForObject("SELECT id FROM students WHERE student_code = '" + request.getStudentCode() + "'", Integer.class);

            // LỖI A03: SQL Injection tại classId. Gán điểm mặc định 0.0 vào bảng enrollments.
            String sql = "INSERT INTO enrollments (student_id, class_id, grade, enrolled_at) VALUES (" 
                        + studentId + ", " + request.getClassId() + ", 0.0, NOW())";
            
            jdbcTemplate.execute(sql);

            response.put("status", "success");
            response.put("message", "Sinh viên " + request.getStudentCode() + " đã đăng ký vào lớp " + request.getClassId() + " thành công!");
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi đăng ký lớp: " + e.getMessage());
            return response;
        }
    }

    @Operation(summary = "Xem danh sách lớp đã đăng ký (Nhiều lớp)", description = "LỖI IDOR & SQL Injection. Lấy điểm từ bảng enrollments.")
    @GetMapping("/my-classes")
    public List<Map<String, Object>> getMyClasses(@RequestParam String studentCode) {
        // LỖI A03: SQL Injection & A01: IDOR
        // Lấy điểm (grade) từ bảng enrollments
        String sql = "SELECT c.*, e.enrolled_at, e.grade FROM classes c " +
                     "JOIN enrollments e ON c.id = e.class_id " +
                     "JOIN students s ON e.student_id = s.id " +
                     "WHERE s.student_code = '" + studentCode + "'";
        
        return jdbcTemplate.queryForList(sql);
    }

    @Operation(summary = "Xem danh sách sinh viên theo lớp", description = "Lấy danh sách sinh viên tham gia lớp học kèm điểm số từ bảng enrollments.")
    @GetMapping
    public List<Map<String, Object>> getStudentsByClass(@RequestParam Integer classId) {
        // JOIN qua bảng enrollments để lấy điểm số e.grade
        String sql = "SELECT s.id, s.student_code, u.full_name, u.email, e.enrolled_at, e.grade FROM students s " +
                     "JOIN users u ON s.user_id = u.id " +
                     "JOIN enrollments e ON s.id = e.student_id " +
                     "WHERE e.class_id = " + classId;
        return jdbcTemplate.queryForList(sql);
    }

    @Operation(summary = "Xem chi tiết sinh viên", description = "LỖI IDOR: Thay đổi id để xem hồ sơ người khác.")
    @GetMapping("/{id}")
    public Map<String, Object> getStudentDetail(@PathVariable Integer id) {
        String sql = "SELECT s.*, u.full_name, u.email FROM students s JOIN users u ON s.user_id = u.id WHERE s.id = " + id;
        return jdbcTemplate.queryForMap(sql);
    }

    @Operation(summary = "Tìm kiếm sinh viên", description = "LỖI SQL Injection.")
    @GetMapping("/search")
    public List<Map<String, Object>> searchStudents(@RequestParam String name) {
        String sql = "SELECT s.*, u.full_name, u.email FROM students s JOIN users u ON s.user_id = u.id WHERE u.full_name LIKE '%" + name + "%'";
        return jdbcTemplate.queryForList(sql);
    }

    @Operation(summary = "Cập nhật sinh viên", description = "LỖI SQL Injection & IDOR.")
    @PutMapping("/{id}")
    public Map<String, Object> updateStudent(@PathVariable Integer id, @RequestBody StudentRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String sql = "UPDATE students SET student_code = '" + request.getStudentCode() + "' WHERE id = " + id;
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

    @Operation(summary = "Upload ảnh đại diện", description = "LỖI File Upload RCE.")
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
