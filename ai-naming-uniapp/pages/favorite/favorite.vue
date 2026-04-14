<template>
  <view class="container">
    <view class="header">
      <text class="title">❤️ 我的收藏</text>
    </view>

    <view v-if="list.length > 0" class="favorite-list">
      <view
        class="favorite-card"
        v-for="(item, index) in list"
        :key="index"
        @click="goDetail(item)"
      >
        <view class="left">
          <text class="name">{{ item.name }}</text>
          <text class="meta">{{ item.wuxing || '五行平衡' }} · 综合 {{ item.totalScore || 80 }}分</text>
        </view>
        <text class="arrow">›</text>
      </view>
    </view>

    <view v-else class="empty-state">
      <text class="empty-icon">📭</text>
      <text class="empty-text">还没有收藏的名字</text>
      <text class="empty-sub">去取名页面发现好名字吧</text>
    </view>
  </view>
</template>

<script>
import request from '@/utils/request.js'

export default {
  data() {
    return {
      list: []
    }
  },
  onShow() {
    this.loadFavorites()
  },
  methods: {
    async loadFavorites() {
      try {
        this.list = await request.get('/api/favorite/list')
      } catch (e) {
        console.error(e)
      }
    },
    goDetail(item) {
      uni.setStorageSync('current_name', item)
      uni.navigateTo({
        url: '/pages/detail/detail?id=' + item.id
      })
    }
  }
}
</script>

<style scoped>
.container {
  padding: 30rpx;
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  margin-bottom: 30rpx;
}

.title {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
}

.favorite-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.favorite-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.03);
}

.left {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.name {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
}

.meta {
  font-size: 24rpx;
  color: #888;
}

.arrow {
  font-size: 40rpx;
  color: #ccc;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.empty-sub {
  font-size: 26rpx;
  color: #aaa;
}
</style>
