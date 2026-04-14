package com.aining.service;

import com.aining.entity.User;

public interface UserService {
    User getOrCreateByOpenid(String openid);
    User getById(Long id);
    boolean decreaseFreeCount(Long userId);
}
