package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Thông tin yêu cầu liên quan đến sinh viên")
public class StudentRequest {

    @Schema(description = "Mã số sinh viên (Dùng cho Đăng ký/Cập nhật)", example = "SV1")
    private String studentCode;

    @Schema(description = "Mã lớp học (Dùng thay cho classId)", example = "IT01")
    private String classCode;

    @Schema(description = "Email của User", example = "student@example.com")
    private String email;

    @Schema(description = "Điểm số (Dùng để cập nhật điểm)", example = "10.0")
    private Double grade;
}
