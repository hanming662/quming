package com.aining.service;

import com.aining.vo.NamingHistoryVO;

import java.util.List;

public interface NamingHistoryService {
    List<NamingHistoryVO> listHistory(Long userId, Integer limit);
    List<NamingHistoryVO> listHistoryPage(Long userId, Integer page, Integer pageSize);
    boolean deleteHistory(Long userId, Long recordId);
    boolean clearHistory(Long userId);
}
