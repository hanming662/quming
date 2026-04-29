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
        return listHistoryPage(userId, 1, limit != null ? limit : 20);
    }

    @Override
    public List<NamingHistoryVO> listHistoryPage(Long userId, Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int offset = (safePage - 1) * safePageSize;

        LambdaQueryWrapper<NamingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NamingRecord::getUserId, userId)
                .orderByDesc(NamingRecord::getCreateTime)
                .last("LIMIT " + safePageSize + " OFFSET " + offset);

        List<NamingRecord> records = namingRecordMapper.selectList(wrapper);
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }

        return records.stream().map(this::toHistoryVO).collect(Collectors.toList());
    }

    @Override
    public boolean deleteHistory(Long userId, Long recordId) {
        if (recordId == null) {
            return false;
        }
        NamingRecord record = namingRecordMapper.selectById(recordId);
        if (record == null || !userId.equals(record.getUserId())) {
            return false;
        }
        nameResultMapper.delete(new LambdaQueryWrapper<NameResult>().eq(NameResult::getRecordId, recordId));
        return namingRecordMapper.deleteById(recordId) > 0;
    }

    @Override
    public boolean clearHistory(Long userId) {
        List<NamingRecord> records = namingRecordMapper.selectList(
                new LambdaQueryWrapper<NamingRecord>().eq(NamingRecord::getUserId, userId)
        );
        if (records == null || records.isEmpty()) {
            return true;
        }
        List<Long> recordIds = records.stream().map(NamingRecord::getId).collect(Collectors.toList());
        nameResultMapper.delete(new LambdaQueryWrapper<NameResult>().in(NameResult::getRecordId, recordIds));
        return namingRecordMapper.delete(new LambdaQueryWrapper<NamingRecord>().eq(NamingRecord::getUserId, userId)) >= 0;
    }

    private NamingHistoryVO toHistoryVO(NamingRecord record) {
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
                .orderByDesc(NameResult::getTotalScore);
        List<NameResult> results = nameResultMapper.selectList(resultWrapper);
        vo.setCount(results != null ? results.size() : 0);
        List<NameResultVO> allNames = results == null ? new ArrayList<>() : results.stream().map(this::convertToVO).collect(Collectors.toList());
        vo.setAllNames(allNames);
        vo.setTopNames(allNames.stream().limit(3).collect(Collectors.toList()));
        return vo;
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
