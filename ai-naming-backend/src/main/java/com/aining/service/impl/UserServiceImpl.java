package com.aining.service.impl;

import com.aining.entity.User;
import com.aining.mapper.UserMapper;
import com.aining.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User getOrCreateByOpenid(String openid) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname("用户" + System.currentTimeMillis() % 10000);
            user.setFreeCount(3);
            userMapper.insert(user);
        }
        return user;
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public boolean decreaseFreeCount(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getFreeCount() == null || user.getFreeCount() <= 0) {
            return false;
        }
        user.setFreeCount(user.getFreeCount() - 1);
        userMapper.updateById(user);
        return true;
    }
}
