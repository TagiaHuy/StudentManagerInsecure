package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Thông tin yêu cầu cấp quyền sinh viên")
public class RoleRequest {

    @Schema(description = "Email của người dùng cần cấp quyền", example = "student@example.com")
    private String email;

    @Schema(description = "Lý do yêu cầu (LỖI SQL Injection: Chèn mã vào đây)", example = "Em là sinh viên mới nhập học.")
    private String reason;
}
