package com.aining.service;

import com.aining.vo.NameResultVO;

import java.util.List;

public interface FavoriteService {
    void addFavorite(Long userId, Long nameResultId);
    void removeFavorite(Long userId, Long nameResultId);
    List<NameResultVO> getFavorites(Long userId);
}
