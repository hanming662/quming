package com.aining.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hot_name")
public class HotName {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String gender;
    private String meaning;
    private Integer searchCount;
    private LocalDateTime createTime;
}
