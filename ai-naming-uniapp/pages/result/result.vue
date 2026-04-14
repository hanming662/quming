<template>
  <view class="container">
    <view class="header">
      <text class="title">✨ 取名结果</text>
      <text class="subtitle">以下是AI为您精心挑选的名字</text>
    </view>

    <view class="name-list">
      <view
        class="name-card"
        v-for="(item, index) in list"
        :key="index"
        @click="goDetail(item)"
      >
        <view class="card-header">
          <text class="name">{{ item.name }}</text>
          <text class="pinyin">{{ item.pinyin }}</text>
          <view class="score-tag">
            <text class="score">{{ item.totalScore }}</text>
            <text class="score-label">分</text>
          </view>
        </view>

        <view class="tags">
          <text class="tag" v-if="item.wuxing">{{ item.wuxing }}</text>
          <text class="tag yinyun">音韵 {{ item.yinyunScore }}</text>
          <text class="tag times">时代 {{ item.timesScore }}</text>
        </view>

        <text class="meaning">{{ item.meaning }}</text>

        <view class="actions">
          <text
            class="action-btn"
            :class="{ favorited: item.isFavorited }"
            @click.stop="toggleFavorite(item)"
          >{{ item.isFavorited ? '❤️ 已收藏' : '🤍 收藏' }}</text>
          <text class="action-btn" @click.stop="toggleCompare(item)">
            {{ isSelectedForCompare(item) ? '✅ 已选对比' : '⚖️ 对比' }}
          </text>
        </view>
      </view>
    </view>

    <view v-if="compareList.length >= 2" class="compare-bar">
      <text class="compare-text">已选 {{ compareList.length }} 个名字对比</text>
      <button class="compare-btn" @click="goCompare">开始对比</button>
    </view>
  </view>
</template>

<script>
import request from '@/utils/request.js'

export default {
  data() {
    return {
      list: [],
      compareList: []
    }
  },
  onLoad() {
    this.list = uni.getStorageSync('naming_result') || []
  },
  methods: {
    goDetail(item) {
      uni.setStorageSync('current_name', item)
      uni.navigateTo({
        url: '/pages/detail/detail?id=' + item.id
      })
    },
    async toggleFavorite(item) {
      try {
        if (item.isFavorited) {
          await request.post('/api/favorite/remove/' + item.id)
          item.isFavorited = false
        } else {
          await request.post('/api/favorite/add/' + item.id)
          item.isFavorited = true
        }
      } catch (e) {
        console.error(e)
      }
    },
    toggleCompare(item) {
      const idx = this.compareList.findIndex(i => i.id === item.id)
      if (idx >= 0) {
        this.compareList.splice(idx, 1)
      } else {
        if (this.compareList.length >= 2) {
          uni.showToast({ title: '最多选择2个', icon: 'none' })
          return
        }
        this.compareList.push(item)
      }
    },
    isSelectedForCompare(item) {
      return this.compareList.some(i => i.id === item.id)
    },
    goCompare() {
      const ids = this.compareList.map(i => i.id)
      uni.navigateTo({
        url: '/pages/compare/compare?ids=' + encodeURIComponent(JSON.stringify(ids))
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
  padding-bottom: 140rpx;
}

.header {
  margin-bottom: 30rpx;
}

.title {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
  display: block;
}

.subtitle {
  font-size: 26rpx;
  color: #888;
  margin-top: 8rpx;
  display: block;
}

.name-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.name-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
}

.card-header {
  display: flex;
  align-items: baseline;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.name {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
}

.pinyin {
  font-size: 24rpx;
  color: #999;
}

.score-tag {
  margin-left: auto;
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  border-radius: 24rpx;
  padding: 6rpx 20rpx;
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}

.score {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
}

.score-label {
  font-size: 20rpx;
  color: #fff;
}

.tags {
  display: flex;
  gap: 12rpx;
  margin-bottom: 16rpx;
  flex-wrap: wrap;
}

.tag {
  padding: 6rpx 16rpx;
  background: #F5F7FA;
  border-radius: 8rpx;
  font-size: 22rpx;
  color: #666;
}

.tag.yinyun {
  background: #E6F7FF;
  color: #1890FF;
}

.tag.times {
  background: #F6FFED;
  color: #52C41A;
}

.meaning {
  font-size: 28rpx;
  color: #555;
  line-height: 1.5;
  margin-bottom: 20rpx;
  display: block;
}

.actions {
  display: flex;
  gap: 20rpx;
  border-top: 1rpx solid #f5f5f5;
  padding-top: 20rpx;
}

.action-btn {
  font-size: 26rpx;
  color: #666;
  padding: 8rpx 16rpx;
  background: #F8F9FA;
  border-radius: 8rpx;
}

.action-btn.favorited {
  color: #FF6B6B;
  background: #FFF0F0;
}

.compare-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.06);
  z-index: 100;
}

.compare-text {
  font-size: 28rpx;
  color: #333;
}

.compare-btn {
  margin: 0;
  padding: 0 32rpx;
  height: 72rpx;
  line-height: 72rpx;
  background: linear-gradient(90deg, #FF6B6B, #FF8E53);
  color: #fff;
  font-size: 28rpx;
  border-radius: 36rpx;
}

.compare-btn::after {
  border: none;
}
</style>
