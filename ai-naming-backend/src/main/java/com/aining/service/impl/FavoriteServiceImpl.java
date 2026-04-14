package com.aining.service.impl;

import com.aining.entity.NameResult;
import com.aining.entity.UserFavorite;
import com.aining.mapper.NameResultMapper;
import com.aining.mapper.UserFavoriteMapper;
import com.aining.service.FavoriteService;
import com.aining.vo.NameResultVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Autowired
    private NameResultMapper nameResultMapper;

    @Override
    public void addFavorite(Long userId, Long nameResultId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId);
        wrapper.eq(UserFavorite::getNameResultId, nameResultId);
        if (userFavoriteMapper.selectCount(wrapper) > 0) {
            return;
        }
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setNameResultId(nameResultId);
        userFavoriteMapper.insert(favorite);
    }

    @Override
    public void removeFavorite(Long userId, Long nameResultId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId);
        wrapper.eq(UserFavorite::getNameResultId, nameResultId);
        userFavoriteMapper.delete(wrapper);
    }

    @Override
    public List<NameResultVO> getFavorites(Long userId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId);
        wrapper.orderByDesc(UserFavorite::getCreateTime);
        List<UserFavorite> favorites = userFavoriteMapper.selectList(wrapper);
        return favorites.stream().map(f -> {
            NameResult result = nameResultMapper.selectById(f.getNameResultId());
            if (result == null) {
                return null;
            }
            NameResultVO vo = new NameResultVO();
            vo.setId(result.getId());
            vo.setName(result.getName());
            vo.setPinyin(result.getPinyin());
            vo.setMeaning(result.getMeaning());
            vo.setWuxing(result.getWuxing());
            vo.setTotalScore(result.getTotalScore());
            vo.setIsFavorited(true);
            return vo;
        }).filter(r -> r != null).collect(Collectors.toList());
    }
}
