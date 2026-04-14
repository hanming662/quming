package com.aining.controller;

import com.aining.entity.User;
import com.aining.service.FavoriteService;
import com.aining.service.UserService;
import com.aining.vo.NameResultVO;
import com.aining.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private UserService userService;

    @PostMapping("/add/{nameResultId}")
    public Result<Void> add(@PathVariable Long nameResultId, HttpServletRequest request) {
        String openid = request.getHeader("X-Openid");
        User user = userService.getOrCreateByOpenid(openid);
        favoriteService.addFavorite(user.getId(), nameResultId);
        return Result.success();
    }

    @PostMapping("/remove/{nameResultId}")
    public Result<Void> remove(@PathVariable Long nameResultId, HttpServletRequest request) {
        String openid = request.getHeader("X-Openid");
        User user = userService.getOrCreateByOpenid(openid);
        favoriteService.removeFavorite(user.getId(), nameResultId);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<NameResultVO>> list(HttpServletRequest request) {
        String openid = request.getHeader("X-Openid");
        User user = userService.getOrCreateByOpenid(openid);
        return Result.success(favoriteService.getFavorites(user.getId()));
    }
}
