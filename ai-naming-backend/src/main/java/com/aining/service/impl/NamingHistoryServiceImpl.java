package com.aining.service.impl;

import com.aining.entity.NameResult;
import com.aining.entity.NamingRecord;
import com.aining.mapper.NameResultMapper;
import com.aining.mapper.NamingRecordMapper;
import com.aining.service.NamingHistoryService;
import com.aining.vo.NameResultVO;
import com.aining.vo.NamingHistoryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NamingHistoryServiceImpl implements NamingHistoryService {

    @Autowired
    private NamingRecordMapper namingRecordMapper;

    @Autowired
    private NameResultMapper nameResultMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<NamingHistoryVO> listHistory(Long userId, Integer limit) {
        LambdaQueryWrapper<NamingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NamingRecord::getUserId, userId)
                .orderByDesc(NamingRecord::getCreateTime);
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }

        List<NamingRecord> records = namingRecordMapper.selectList(wrapper);
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }

        return records.stream().map(record -> {
            NamingHistoryVO vo = new NamingHistoryVO();
            vo.setId(record.getId());
            vo.setFatherSurname(record.getFatherSurname());
            vo.setMotherSurname(record.getMotherSurname());
            vo.setGender(record.getGender());
            vo.setPrompt(record.getPrompt());
            vo.setStyle(record.getStyle());
            vo.setCreateTime(record.getCreateTime() != null ? record.getCreateTime().format(TIME_FORMATTER) : null);

            LambdaQueryWrapper<NameResult> resultWrapper = new LambdaQueryWrapper<>();
            resultWrapper.eq(NameResult::getRecordId, record.getId())
                    .orderByDesc(NameResult::getTotalScore)
                    .last("LIMIT 3");
            List<NameResult> results = nameResultMapper.selectList(resultWrapper);
            vo.setCount(results != null ? results.size() : 0);
            vo.setTopNames(results == null ? new ArrayList<>() : results.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }

    private NameResultVO convertToVO(NameResult result) {
        NameResultVO vo = new NameResultVO();
        vo.setId(result.getId());
        vo.setName(result.getName());
        vo.setPinyin(result.getPinyin());
        vo.setMeaning(result.getMeaning());
        vo.setWuxing(result.getWuxing());
        vo.setYinyunScore(result.getYinyunScore());
        vo.setTimesScore(result.getTimesScore());
        vo.setTotalScore(result.getTotalScore());
        vo.setReason(result.getReason());
        vo.setIsFavorited(false);
        return vo;
    }
}
