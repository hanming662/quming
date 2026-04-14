package com.aining.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class NamingRequestDTO {
    @NotBlank(message = "父姓不能为空")
    private String fatherSurname;
    
    private String motherSurname;
    
    @NotBlank(message = "性别不能为空")
    private String gender;
    
    private String prompt;
    
    private String style = "modern";
    
    private Integer count = 5;
}
