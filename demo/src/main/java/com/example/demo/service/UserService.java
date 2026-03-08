package com.example.demo.service;

import com.example.demo.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * VULNERABLE METHOD: 
     * 1. A03:2021 - SQL Injection (String concatenation)
     * 2. A04:2021 - Cryptographic Failures (Plaintext password)
     * 3. A01:2021 - Broken Access Control (Allows injecting extra data like role if not careful)
     */
    @Transactional
    public void registerUserInsecure(String fullName, String email, String password, String username, String role) {
        // Lỗi SQL Injection: Cộng chuỗi trực tiếp vào câu lệnh SQL
        String sql = "INSERT INTO users (full_name, email, password, is_active, created_at) VALUES ('" 
                     + fullName + "', '" + email + "', '" + password + "', 1, NOW())";
        
        entityManager.createNativeQuery(sql).executeUpdate();
        
        // Lỗi Broken Access Control: Nếu người dùng gửi kèm role=ADMIN, ta cũng thực hiện gán quyền (giả định bảng user_roles)
        if (role != null && !role.isEmpty()) {
            // Đảm bảo tên role có prefix ROLE_ nếu người dùng quên nhập (hoặc để người dùng tự nhập để test lỗi)
            String targetRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            String roleSql = "INSERT INTO user_roles (user_id, role_id) SELECT id, (SELECT id FROM roles WHERE name = '" + targetRole + "') FROM users WHERE email = '" + email + "'";
            entityManager.createNativeQuery(roleSql).executeUpdate();
        } else {
            // Mặc định là ROLE_USER nếu không truyền
            String defaultRoleSql = "INSERT INTO user_roles (user_id, role_id) SELECT id, (SELECT id FROM roles WHERE name = 'ROLE_USER') FROM users WHERE email = '" + email + "'";
            entityManager.createNativeQuery(defaultRoleSql).executeUpdate();
        }
    }
}
