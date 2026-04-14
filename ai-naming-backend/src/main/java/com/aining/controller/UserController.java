package com.aining.controller;

import com.aining.entity.User;
import com.aining.service.UserService;
import com.aining.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public Result<User> login(HttpServletRequest request) {
        String openid = request.getHeader("X-Openid");
        if (openid == null || openid.isEmpty()) {
            return Result.error("缺少身份标识");
        }
        User user = userService.getOrCreateByOpenid(openid);
        return Result.success(user);
    }

    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        String openid = request.getHeader("X-Openid");
        if (openid == null || openid.isEmpty()) {
            return Result.error("缺少身份标识");
        }
        User user = userService.getOrCreateByOpenid(openid);
        return Result.success(user);
    }
}
