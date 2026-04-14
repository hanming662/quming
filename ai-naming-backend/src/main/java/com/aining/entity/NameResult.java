package com.aining.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("name_result")
public class NameResult {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recordId;
    private String name;
    private String pinyin;
    private String meaning;
    private String wuxing;
    private Integer yinyunScore;
    private Integer timesScore;
    private Integer totalScore;
    private String reason;
    private Integer isHot;
    private Integer hotIndex;
}
