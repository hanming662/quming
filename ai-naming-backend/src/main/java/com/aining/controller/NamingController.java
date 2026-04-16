package com.aining.controller;

import com.aining.annotation.RateLimit;
import com.aining.dto.AnalyzeRequestDTO;
import com.aining.dto.NamingRequestDTO;
import com.aining.entity.User;
import com.aining.exception.BusinessException;
import com.aining.service.NamingService;
import com.aining.service.UserService;
import com.aining.vo.NameResultVO;
import com.aining.vo.Result;
import com.aining.vo.StreamTaskVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/naming")
public class NamingController {

    @Autowired
    private NamingService namingService;

    @Autowired
    private UserService userService;

    @PostMapping("/generate")
    @RateLimit(key = "#request.getHeader('X-Openid')", timeWindow = 60, maxRequests = 3, message = "取名请求过于频繁，请1分钟后重试")
    public Result<List<NameResultVO>> generate(@RequestBody @Validated NamingRequestDTO dto, HttpServletRequest request) {
        String openid = request.getHeader("X-Openid");
        User user = userService.getOrCreateByOpenid(openid);
        if (!userService.decreaseFreeCount(user.getId())) {
            throw new BusinessException("免费次数已用完");
        }
        List<NameResultVO> list = namingService.generateNames(user.getId(), dto);
        return Result.success(list);
    }

    @PostMapping("/deepAnalyze")
//    @RateLimit(key = "#request.getHeader('X-Openid')", timeWindow = 60, maxRequests = 5, message = "深度分析请求过于频繁，请1分钟后重试")
    public Result<StreamTaskVO> deepAnalyze(@RequestBody AnalyzeRequestDTO dto) {
        return Result.success(namingService.startDeepAnalyze(dto));
    }

    @GetMapping(value = "/stream/{taskId}", produces = "text/event-stream;charset=UTF-8")
//    @RateLimit(key = "#taskId", timeWindow = 60, maxRequests = 60, message = "流式请求过于频繁")
    public SseEmitter stream(@PathVariable String taskId) {
        SseEmitter emitter = new SseEmitter(0L);
        namingService.streamContent(taskId, emitter);
        return emitter;
    }

    @GetMapping("/poll/{taskId}")
//    @RateLimit(key = "#taskId", timeWindow = 60, maxRequests = 120, message = "轮询请求过于频繁")
    public Result<String> poll(@PathVariable String taskId) {
        return Result.success(namingService.pollStreamContent(taskId));
    }

    @GetMapping("/hot")
    @RateLimit(key = "'global'", timeWindow = 1, maxRequests = 100, message = "服务器繁忙，请稍后重试")
    public Result<List<NameResultVO>> hot(@RequestParam(required = false) String gender,
                                          @RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(namingService.getHotNames(gender, limit));
    }

    @PostMapping("/compare")
    @RateLimit(key = "#request.getHeader('X-Openid')", timeWindow = 60, maxRequests = 10, message = "对比请求过于频繁，请1分钟后重试")
    public Result<List<NameResultVO>> compare(@RequestBody List<Long> nameResultIds, HttpServletRequest request) {
        String openid = request.getHeader("X-Openid");
        User user = userService.getOrCreateByOpenid(openid);
        return Result.success(namingService.compareNames(nameResultIds, user.getId()));
    }
}
