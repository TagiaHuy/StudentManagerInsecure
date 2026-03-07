package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Thông tin đăng ký tài khoản mới")
public class RegisterRequest {

    @Schema(description = "Họ và tên đầy đủ của sinh viên", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Địa chỉ Email (Dùng để đăng nhập)", example = "student@example.com")
    private String email;

    @Schema(description = "Mật khẩu (Lưu ý: Hệ thống đang lưu Plaintext - Lỗ hổng A04)", example = "123456")
    private String password;

    @Schema(description = "Tên đăng nhập", example = "vana_123")
    private String username;

    @Schema(description = "Quyền hạn (Mặc định: ROLE_USER. Hacker có thể đổi thành ROLE_ADMIN - Lỗ hổng A01)", example = "ROLE_USER")
    private String role;
}
