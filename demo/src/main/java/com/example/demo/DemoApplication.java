package com.example.demo;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
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

			// Create a default admin user
			if (userRepository.findByEmail("admin@example.com").isEmpty()) {
				User admin = new User();
				admin.setFullName("Admin User");
				admin.setEmail("admin@example.com");
//				admin.setPasswordHash("password");
				admin.setPasswordHash(passwordEncoder.encode("password"));
				Set<Role> adminRoles = new HashSet<>();
				adminRoles.add(adminRole);
				admin.setRoles(adminRoles);
				userRepository.save(admin);
			}

			if (userRepository.findByEmail("admin2@example.com").isEmpty()) {
				User admin = new User();
				admin.setFullName("Admin User");
				admin.setEmail("admin2@example.com");
				admin.setPasswordHash("password");
//				admin.setPasswordHash(passwordEncoder.encode("password"));
				Set<Role> adminRoles = new HashSet<>();
				adminRoles.add(adminRole);
				admin.setRoles(adminRoles);
				userRepository.save(admin);
			}
		};
	}
}
