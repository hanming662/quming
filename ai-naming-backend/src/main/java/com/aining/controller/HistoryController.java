package com.aining.controller;

import com.aining.entity.User;
import com.aining.service.NamingHistoryService;
import com.aining.service.UserService;
import com.aining.vo.NamingHistoryVO;
import com.aining.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/page")
    public Result<List<NamingHistoryVO>> page(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              HttpServletRequest request) {
        String openid = request.getHeader("X-Openid");
        User user = userService.getOrCreateByOpenid(openid);
        return Result.success(namingHistoryService.listHistoryPage(user.getId(), page, pageSize));
    }

    @DeleteMapping("/{recordId}")
    public Result<Void> delete(@PathVariable Long recordId, HttpServletRequest request) {
        String openid = request.getHeader("X-Openid");
        User user = userService.getOrCreateByOpenid(openid);
        return namingHistoryService.deleteHistory(user.getId(), recordId)
                ? Result.success()
                : Result.error("删除失败");
    }

    @DeleteMapping
    public Result<Void> clear(HttpServletRequest request) {
        String openid = request.getHeader("X-Openid");
        User user = userService.getOrCreateByOpenid(openid);
        return namingHistoryService.clearHistory(user.getId())
                ? Result.success()
                : Result.error("清空失败");
    }
}
