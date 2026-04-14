package com.aining.service;

import com.aining.dto.AnalyzeRequestDTO;
import com.aining.dto.NamingRequestDTO;
import com.aining.vo.NameResultVO;
import com.aining.vo.StreamTaskVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface NamingService {
    List<NameResultVO> generateNames(Long userId, NamingRequestDTO dto);
    StreamTaskVO startDeepAnalyze(AnalyzeRequestDTO dto);
    String pollStreamContent(String taskId);
    void streamContent(String taskId, SseEmitter emitter);
    List<NameResultVO> getHotNames(String gender, Integer limit);
    List<NameResultVO> compareNames(List<Long> nameResultIds, Long userId);
}
