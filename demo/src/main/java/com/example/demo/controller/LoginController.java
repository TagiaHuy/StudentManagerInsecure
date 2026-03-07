package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
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

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * API Đăng nhập
     * LỖI:
     * 1. A03: SQL Injection (Sử dụng nối chuỗi trong UserRepository)
     * 2. A04: Cryptographic Failures (Mật khẩu Plaintext)
     * 3. A02: Security Misconfiguration (Lộ thông tin chi tiết lỗi)
     * 4. A07: Authentication Failures (Không có brute-force protection, mật khẩu yếu)
     * 5. A05: Security Logging Failures (Không log khi login sai)
     * 6. A01: Broken Access Control (Token Base64 không an toàn)
     */
    @Operation(summary = "Đăng nhập hệ thống", description = "Endpoint chứa nhiều lỗi bảo mật phục vụ minh họa.")
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = loginRequest.getEmail();
            String password = loginRequest.getPassword();

            // SQL INJECTION VULNERABILITY (A03)
            // Hacker có thể dùng email: ' OR '1'='1' -- để bypass
            Optional<User> userOpt = userRepository.findByEmailAndPasswordInsecure(email, password);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Cài đặt Session không an toàn (A01)
                session.setAttribute("loggedInUser", user);
                
                // Trả về Token không an toàn (Chỉ là Base64 của email - Broken Access Control A01)
                String insecureToken = Base64.getEncoder().encodeToString(user.getEmail().getBytes());
                
                response.put("status", "success");
                response.put("message", "Đăng nhập thành công!");
                response.put("token", insecureToken); // Token này có thể bị giải mã dễ dàng
                response.put("user", user);
                
                // LỖI: Thiếu Logging cho lần đăng nhập thành công có ý nghĩa (A05)
                return response;
            } else {
                // KIỂM TRA LỖI LỘ THÔNG TIN (Security Misconfiguration A02)
                // Ta kiểm tra xem email có tồn tại không để báo lỗi chi tiết (GIÚP HACKER ENUMERATE USER)
                Optional<User> checkUser = userRepository.findByEmail(email);
                
                if (checkUser.isPresent()) {
                    // LỖI A02: Báo lỗi chi tiết "Sai mật khẩu" giúp hacker biết user tồn tại
                    response.put("status", "error");
                    response.put("message", "Mật khẩu cho tài khoản " + email + " không chính xác.");
                } else {
                    // LỖI A02: Báo lỗi chi tiết "Email không tồn tại"
                    response.put("status", "error");
                    response.put("message", "Email " + email + " không tồn tại trong hệ thống.");
                }
                
                // LỖI: Thiếu Logging Failures (A05) - Không ghi lại IP hay thời gian đăng nhập sai
                return response;
            }
        } catch (Exception e) {
            // LỖI A02: Ném thẳng Exception SQL cho người dùng (Security Misconfiguration)
            response.put("status", "error");
            response.put("exception", e.getClass().getName());
            response.put("message", "Lỗi database: " + e.getMessage());
            response.put("stackTrace", e.getStackTrace());
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