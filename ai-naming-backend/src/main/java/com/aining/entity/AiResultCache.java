package com.aining.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_result_cache")
public class AiResultCache {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String cacheKey;
    private String requestParams;
    private String resultJson;
    private Integer hitCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
