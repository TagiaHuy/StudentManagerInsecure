package com.example.demo.controller;

import com.example.demo.dto.RegisterRequest;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") 
@Tag(name = "User Registration", description = "Quản lý việc đăng ký người dùng mới (Chứa lỗ hổng OWASP)")
public class RegisterController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Đăng ký tài khoản mới", description = "Chứa lỗi SQL Injection, Cryptographic Failure và Broken Access Control.")
    @PostMapping("/register")
    public Map<String, Object> registerUser(@RequestBody RegisterRequest request) {
        try {
            userService.registerUserInsecure(
                request.getFullName(), 
                request.getEmail(), 
                request.getPassword(), 
                request.getUsername(), 
                request.getRole()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "User registered successfully");
            return response;

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("exception", e.getClass().getName());
            errorResponse.put("message", e.getMessage()); 
            errorResponse.put("stackTrace", e.getStackTrace()); 
            return errorResponse;
        }
    }
}
