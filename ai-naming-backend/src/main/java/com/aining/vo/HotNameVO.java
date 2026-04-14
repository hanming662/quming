package com.aining.vo;

import lombok.Data;

@Data
public class HotNameVO {
    private Long id;
    private String name;
    private String gender;
    private String meaning;
    private Integer searchCount;
}
