package com.aining.vo;

import lombok.Data;

@Data
public class NameResultVO {
    private Long id;
    private String name;
    private String pinyin;
    private String meaning;
    private String wuxing;
    private Integer yinyunScore;
    private Integer timesScore;
    private Integer totalScore;
    private String reason;
    private Boolean isFavorited;
}
