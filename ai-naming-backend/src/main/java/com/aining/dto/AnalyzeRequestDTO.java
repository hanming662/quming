package com.aining.dto;

import lombok.Data;

@Data
public class AnalyzeRequestDTO {
    private Long nameResultId;
    private String name;
    private String gender;
    private String fatherSurname;
    private String motherSurname;
    private String type;
}
