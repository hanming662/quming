package com.aining.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("naming_record")
public class NamingRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String fatherSurname;
    private String motherSurname;
    private String gender;
    private String prompt;
    private String style;
    private LocalDateTime createTime;
}
