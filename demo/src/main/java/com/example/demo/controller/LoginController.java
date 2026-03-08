package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "Quản lý đăng nhập và phiên làm việc (Chứa lỗ hổng OWASP)")
public class LoginController {

    private final UserRepository userRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * API Đăng nhập
     * LỖI: Chứa nhiều lỗ hổng OWASP để minh họa.
     */
    @Operation(summary = "Đăng nhập hệ thống", description = "Endpoint trả về token Base64 và studentCode (nếu là sinh viên).")
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = loginRequest.getEmail();
            String password = loginRequest.getPassword();

            // SQL INJECTION VULNERABILITY (A03)
            Optional<User> userOpt = userRepository.findByEmailAndPasswordInsecure(email, password);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Cài đặt Session không an toàn (A01)
                session.setAttribute("loggedInUser", user);
                
                // Trả về Token không an toàn (Chỉ là Base64 của email - A01)
                String insecureToken = Base64.getEncoder().encodeToString(user.getEmail().getBytes());
                
                response.put("status", "success");
                response.put("message", "Đăng nhập thành công!");
                response.put("token", insecureToken);
                response.put("user", user);

                // THÊM: Tìm studentCode từ bảng students (Nếu có)
                try {
                    // LỖI: SQL Injection tại user.getId() (Minh họa nối chuỗi trực tiếp)
                    String findStudentSql = "SELECT student_code FROM students WHERE user_id = " + user.getId();
                    String studentCode = jdbcTemplate.queryForObject(findStudentSql, String.class);
                    response.put("studentCode", studentCode);
                } catch (Exception e) {
                    // Nếu không phải sinh viên, giá trị này sẽ là null
                    response.put("studentCode", null);
                }
                
                return response;
            } else {
                // KIỂM TRA LỖI LỘ THÔNG TIN (A02)
                Optional<User> checkUser = userRepository.findByEmail(email);
                if (checkUser.isPresent()) {
                    response.put("status", "error");
                    response.put("message", "Mật khẩu cho tài khoản " + email + " không chính xác.");
                } else {
                    response.put("status", "error");
                    response.put("message", "Email " + email + " không tồn tại.");
                }
                return response;
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Đã đăng xuất.");
        return response;
    }
}
