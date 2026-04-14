package com.aining.controller;

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
    public Result<StreamTaskVO> deepAnalyze(@RequestBody AnalyzeRequestDTO dto) {
        return Result.success(namingService.startDeepAnalyze(dto));
    }

    @GetMapping("/stream/{taskId}")
    public Result<String> stream(@PathVariable String taskId) {
        return Result.success(namingService.pollStreamContent(taskId));
    }

    @GetMapping("/hot")
    public Result<List<NameResultVO>> hot(@RequestParam(required = false) String gender,
                                          @RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(namingService.getHotNames(gender, limit));
    }

    @PostMapping("/compare")
    public Result<List<NameResultVO>> compare(@RequestBody List<Long> nameResultIds, HttpServletRequest request) {
        String openid = request.getHeader("X-Openid");
        User user = userService.getOrCreateByOpenid(openid);
        return Result.success(namingService.compareNames(nameResultIds, user.getId()));
    }
}
