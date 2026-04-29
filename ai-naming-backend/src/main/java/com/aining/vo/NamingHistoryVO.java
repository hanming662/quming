package com.aining.vo;

import lombok.Data;

import java.util.List;

@Data
public class NamingHistoryVO {
    private Long id;
    private String fatherSurname;
    private String motherSurname;
    private String gender;
    private String prompt;
    private String style;
    private String createTime;
    private Integer count;
    private List<NameResultVO> topNames;
}
