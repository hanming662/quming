<template>
  <view class="container">
    <view class="header">
      <text class="title">🔥 热度名字榜</text>
      <view class="tab-bar">
        <view
          class="tab-item"
          :class="{ active: gender === 'boy' }"
          @click="changeGender('boy')"
        >男宝热名</view>
        <view
          class="tab-item"
          :class="{ active: gender === 'girl' }"
          @click="changeGender('girl')"
        >女宝热名</view>
      </view>
    </view>

    <view class="hot-list">
      <view
        class="hot-item"
        v-for="(item, index) in list"
        :key="index"
      >
        <view class="rank" :class="{ top: index < 3 }">{{ index + 1 }}</view>
        <view class="info">
          <text class="name">{{ item.name }}</text>
          <text class="desc">{{ item.meaning || '寓意美好' }}</text>
        </view>
        <view class="heat">
          <text class="heat-num">{{ item.searchCount || 1000 - index * 80 }}</text>
          <text class="heat-label">热度</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import request from '@/utils/request.js'

export default {
  data() {
    return {
      gender: 'boy',
      list: []
    }
  },
  onLoad() {
    this.loadHotNames()
  },
  methods: {
    async loadHotNames() {
      try {
        this.list = await request.get('/api/naming/hot', {
          gender: this.gender,
          limit: 20
        })
      } catch (e) {
        console.error(e)
      }
    },
    changeGender(g) {
      this.gender = g
      this.loadHotNames()
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
  display: block;
  margin-bottom: 20rpx;
}

.tab-bar {
  display: flex;
  background: #fff;
  border-radius: 12rpx;
  padding: 6rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  font-size: 28rpx;
  color: #666;
  border-radius: 8rpx;
}

.tab-item.active {
  background: #FFF0F0;
  color: #FF6B6B;
  font-weight: 600;
}

.hot-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.hot-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx 30rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.03);
}

.rank {
  width: 56rpx;
  height: 56rpx;
  line-height: 56rpx;
  text-align: center;
  border-radius: 50%;
  background: #f0f0f0;
  color: #666;
  font-size: 28rpx;
  font-weight: 600;
  margin-right: 24rpx;
}

.rank.top {
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  color: #fff;
}

.info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.name {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}

.desc {
  font-size: 24rpx;
  color: #888;
}

.heat {
  text-align: right;
}

.heat-num {
  font-size: 28rpx;
  font-weight: bold;
  color: #FF6B6B;
  display: block;
}

.heat-label {
  font-size: 20rpx;
  color: #aaa;
}
</style>
