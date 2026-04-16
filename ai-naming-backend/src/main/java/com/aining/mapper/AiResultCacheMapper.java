package com.aining.mapper;

import com.aining.entity.AiResultCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface AiResultCacheMapper extends BaseMapper<AiResultCache> {

    @Update("UPDATE ai_result_cache SET hit_count = hit_count + 1, update_time = NOW() WHERE cache_key = #{cacheKey}")
    int incrementHitCount(@Param("cacheKey") String cacheKey);
}
