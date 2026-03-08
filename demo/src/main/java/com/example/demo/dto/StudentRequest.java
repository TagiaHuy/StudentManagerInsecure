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

    @Schema(description = "ID của lớp học", example = "1")
    private Integer classId;

}
