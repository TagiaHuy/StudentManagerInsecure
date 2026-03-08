package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Thông tin đăng nhập")
public class LoginRequest {

    @Schema(description = "Email đăng nhập", example = "admin@example.com")
    private String email;

    @Schema(description = "Mật khẩu (Plaintext)", example = "password")
    private String password;
}
