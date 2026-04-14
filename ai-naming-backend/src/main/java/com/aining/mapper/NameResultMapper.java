package com.aining.mapper;

import com.aining.entity.NameResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NameResultMapper extends BaseMapper<NameResult> {
    
    @Select("SELECT * FROM name_result WHERE is_hot = 1 AND hot_index > 0 ORDER BY hot_index DESC LIMIT #{limit}")
    List<NameResult> selectHotNames(@Param("limit") Integer limit);
}
