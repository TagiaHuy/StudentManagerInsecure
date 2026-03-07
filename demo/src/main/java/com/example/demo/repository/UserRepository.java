package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // CÓ LỖ HỔNG SQL INJECTION - Nối chuỗi trực tiếp
    public Optional<User> findByEmail(String email) {
        // Tạo câu SQL bằng cách nối chuỗi - RẤT NGUY HIỂM
        String sql = "SELECT * FROM users WHERE email = '" + email + "'";

        System.out.println("⚠️ SQL Injection vulnerability (email): " + sql);

        Query query = entityManager.createNativeQuery(sql, User.class);

        List<User> users = query.getResultList();
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    // CÓ LỖ HỔNG SQL INJECTION - Toàn bộ việc đăng nhập trong một câu truy vấn nối chuỗi
    public Optional<User> findByEmailAndPasswordInsecure(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = '" + email + "' AND password = '" + password + "'";

        System.out.println("⚠️ SQL Injection vulnerability (login bypass): " + sql);

        Query query = entityManager.createNativeQuery(sql, User.class);

        List<User> users = query.getResultList();
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    // Thêm phương thức kiểm tra repository có dữ liệu không
    public boolean isEmpty() {
        String sql = "SELECT COUNT(*) FROM user";
        Query query = entityManager.createNativeQuery(sql);
        Long count = ((Number) query.getSingleResult()).longValue();
        return count == 0;
    }

    // Thêm phương thức lưu user vào database
    @Transactional
    public User save(User user) {
        if (user.getId() == null) {
            // Thêm mới
            entityManager.persist(user);
            return user;
        } else {
            // Cập nhật
            return entityManager.merge(user);
        }
    }

    // Thêm phương thức tìm tất cả users (cũng có lỗ hổng SQL Injection)
    public List<User> findAll() {
        String sql = "SELECT * FROM user";
        Query query = entityManager.createNativeQuery(sql, User.class);
        return query.getResultList();
    }

    // Thêm phương thức tìm kiếm user theo tên (có lỗ hổng SQL Injection)
    public List<User> searchByName(String name) {
        String sql = "SELECT * FROM user WHERE full_name LIKE '%" + name + "%'";
        Query query = entityManager.createNativeQuery(sql, User.class);
        return query.getResultList();
    }
}