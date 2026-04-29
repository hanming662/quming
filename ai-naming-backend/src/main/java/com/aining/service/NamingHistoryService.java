package com.aining.service;

import com.aining.vo.NamingHistoryVO;

import java.util.List;

public interface NamingHistoryService {
    List<NamingHistoryVO> listHistory(Long userId, Integer limit);
}
