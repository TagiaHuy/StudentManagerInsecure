package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Thông tin lớp học")
public class ClassRequest {

    @Schema(description = "Tên lớp học", example = "Lập trình Java")
    private String className;

    @Schema(description = "Mô tả lớp học", example = "Lớp học về Java Spring Boot và Security.")
    private String description;

    @Schema(description = "Mã số giảng viên", example = "GV1")
    private String teacherCode;
}
