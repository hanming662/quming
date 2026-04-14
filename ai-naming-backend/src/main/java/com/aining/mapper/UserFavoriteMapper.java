package com.aining.mapper;

import com.aining.entity.UserFavorite;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {
    
    @Select("SELECT COUNT(*) FROM user_favorite WHERE user_id = #{userId} AND name_result_id = #{nameResultId}")
    Integer countByUserAndName(@Param("userId") Long userId, @Param("nameResultId") Long nameResultId);
}
