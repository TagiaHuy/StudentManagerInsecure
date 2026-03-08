package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Thông tin lớp học")
public class ClassRequest {

    @Schema(description = "Mã lớp học (LỖI SQL Injection: Chèn mã vào đây)", example = "TH01")
    private String classCode;

    @Schema(description = "Tên lớp học", example = "Lập trình Java")
    private String className;

    @Schema(description = "ID của giảng viên (LỖI IDOR: Gán giảng viên bất kỳ)", example = "1")
    private Integer teacherId;
}
