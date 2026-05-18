package com.msa.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExamDto {
    @NotBlank
    private String id;
}
