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
			// Create Roles
			Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
				Role role = new Role();
				role.setName("ROLE_ADMIN");
				return roleRepository.save(role);
			});

			Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
				Role role = new Role();
				role.setName("ROLE_USER");
				return roleRepository.save(role);
			});

			Role teacherRole = roleRepository.findByName("ROLE_TEACHER").orElseGet(() -> {
				Role role = new Role();
				role.setName("ROLE_TEACHER");
				return roleRepository.save(role);
			});

			// Create a default admin user
			if (userRepository.findByEmail("admin@example.com").isEmpty()) {
				User admin = new User();
				admin.setFullName("Admin User");
				admin.setEmail("admin@example.com");
				admin.setPassword("password");
				Set<Role> adminRoles = new HashSet<>();
				adminRoles.add(adminRole);
				admin.setRoles(adminRoles);
				userRepository.save(admin);
			}

			// Create a default teacher
			if (userRepository.findByEmail("teacher@example.com").isEmpty()) {
				User teacherUser = new User();
				teacherUser.setFullName("Giảng viên A");
				teacherUser.setEmail("teacher@example.com");
				teacherUser.setPassword("password");
				Set<Role> teacherRoles = new HashSet<>();
				teacherRoles.add(teacherRole);
				teacherUser.setRoles(teacherRoles);
				userRepository.save(teacherUser);

				Teacher teacher = new Teacher();
				teacher.setUser(teacherUser);
				teacher.setTeacherCode("GV001");
				teacher.setDepartment("CNTT");
				teacherRepository.save(teacher);

				// Create a default class
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
