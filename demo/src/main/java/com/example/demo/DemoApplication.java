package com.example.demo;

import com.example.demo.model.ClassEntity;
import com.example.demo.model.Role;
import com.example.demo.model.Teacher;
import com.example.demo.model.User;
import com.example.demo.repository.ClassRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.TeacherRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(RoleRepository roleRepository, 
                                  UserRepository userRepository, 
                                  TeacherRepository teacherRepository,
                                  ClassRepository classRepository) {
        return args -> {
            // 1. Khoi tao cac Role co ban
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
                Role r = new Role();
                r.setName("ROLE_ADMIN");
                return roleRepository.save(r);
            });

            roleRepository.findByName("ROLE_USER").orElseGet(() -> {
                Role r = new Role();
                r.setName("ROLE_USER");
                return roleRepository.save(r);
            });

            roleRepository.findByName("ROLE_STUDENT").orElseGet(() -> {
                Role r = new Role();
                r.setName("ROLE_STUDENT");
                return roleRepository.save(r);
            });

            Role teacherRole = roleRepository.findByName("ROLE_TEACHER").orElseGet(() -> {
                Role r = new Role();
                r.setName("ROLE_TEACHER");
                return roleRepository.save(r);
            });

            // 2. Tao tai khoan Admin mac dinh (admin@example.com / password)
            if (userRepository.findByEmail("admin@example.com").isEmpty()) {
                User admin = new User();
                admin.setFullName("Admin User");
                admin.setEmail("admin@example.com");
                admin.setPassword("password");
                Set<Role> roles = new HashSet<>();
                roles.add(adminRole);
                admin.setRoles(roles);
                userRepository.save(admin);
            }

            // 3. Tao tai khoan Giang vien mac dinh (teacher@example.com / password)
            if (userRepository.findByEmail("teacher@example.com").isEmpty()) {
                User teacherUser = new User();
                teacherUser.setFullName("Giảng viên A");
                teacherUser.setEmail("teacher@example.com");
                teacherUser.setPassword("password");
                Set<Role> roles = new HashSet<>();
                roles.add(teacherRole);
                teacherUser.setRoles(roles);
                userRepository.save(teacherUser);

                Teacher teacher = new Teacher();
                teacher.setUser(teacherUser);
                teacher.setTeacherCode("GV001");
                teacher.setDepartment("CNTT");
                teacherRepository.save(teacher);

                // 4. Tao lop hoc mac dinh cho giang vien nay
                if (classRepository.findByClassCode("IT01").isEmpty()) {
                    ClassEntity classEntity = new ClassEntity();
                    classEntity.setClassCode("IT01");
                    classEntity.setClassName("Lập trình Java");
                    classEntity.setTeacher(teacher);
                    classRepository.save(classEntity);
                }
            }
        };
    }
}
