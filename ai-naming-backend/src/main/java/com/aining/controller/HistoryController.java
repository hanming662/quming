package com.aining.controller;

import com.aining.entity.User;
import com.aining.service.NamingHistoryService;
import com.aining.service.UserService;
import com.aining.vo.NamingHistoryVO;
import com.aining.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private NamingHistoryService namingHistoryService;

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<List<NamingHistoryVO>> list(@RequestParam(defaultValue = "20") Integer limit,
                                              HttpServletRequest request) {
        String openid = request.getHeader("X-Openid");
        User user = userService.getOrCreateByOpenid(openid);
        return Result.success(namingHistoryService.listHistory(user.getId(), limit));
    }
}
