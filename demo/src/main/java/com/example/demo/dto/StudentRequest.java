package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Thông tin sinh viên")
public class StudentRequest {

    @Schema(description = "Mã số sinh viên", example = "SV001")
    private String studentCode;

    @Schema(description = "Họ và tên sinh viên (LỖI SQL Injection: Chèn mã vào đây)", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Email liên kết (LỖI: Lưu Plaintext không mã hóa)", example = "student_new@example.com")
    private String email;

    @Schema(description = "Ngày sinh (Định dạng YYYY-MM-DD)", example = "2002-01-01")
    private String dateOfBirth;

    @Schema(description = "ID lớp học (LỖI IDOR: Gán vào bất kỳ lớp nào)", example = "1")
    private Integer classId;

    @Schema(description = "Điểm số (LỖI Broken Access Control: Sinh viên tự truyền điểm vào đây để sửa)", example = "10.0")
    private Double grade;

    @Schema(description = "ID của User liên kết", example = "1")
    private Integer userId;
}
