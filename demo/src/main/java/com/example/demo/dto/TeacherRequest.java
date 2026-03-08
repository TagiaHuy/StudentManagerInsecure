package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Thông tin tạo mới giảng viên")
public class TeacherRequest {

    @Schema(description = "Họ và tên giảng viên", example = "Nguyễn Văn B")
    private String fullName;

    @Schema(description = "Email đăng nhập", example = "teacher_new@example.com")
    private String email;

    @Schema(description = "Khoa/Bộ môn", example = "Hệ thống thông tin")
    private String department;

    @Schema(description = "Mật khẩu (Plaintext)", example = "123456")
    private String password;
}
