<template>
  <view class="container">
    <view class="header">
      <text class="title">⚖️ 名字对比</text>
      <text class="subtitle">从寓意、音韵、时代感等维度全面对比</text>
    </view>

    <view v-if="list.length >= 2" class="compare-box">
      <view class="name-header">
        <view class="name-col empty"></view>
        <view class="name-col">
          <text class="compare-name">{{ list[0].name }}</text>
        </view>
        <view class="name-col">
          <text class="compare-name">{{ list[1].name }}</text>
        </view>
      </view>

      <view class="compare-row">
        <text class="row-label">综合评分</text>
        <text class="row-val highlight">{{ list[0].totalScore || 80 }}</text>
        <text class="row-val highlight">{{ list[1].totalScore || 80 }}</text>
      </view>

      <view class="compare-row">
        <text class="row-label">音韵评分</text>
        <text class="row-val">{{ list[0].yinyunScore || 8 }}</text>
        <text class="row-val">{{ list[1].yinyunScore || 8 }}</text>
      </view>

      <view class="compare-row">
        <text class="row-label">时代感</text>
        <text class="row-val">{{ list[0].timesScore || 8 }}</text>
        <text class="row-val">{{ list[1].timesScore || 8 }}</text>
      </view>

      <view class="compare-row">
        <text class="row-label">五行属性</text>
        <text class="row-val">{{ list[0].wuxing || '平衡' }}</text>
        <text class="row-val">{{ list[1].wuxing || '平衡' }}</text>
      </view>

      <view class="compare-row vertical">
        <text class="row-label">寓意解读</text>
        <text class="row-text">{{ list[0].meaning || '寓意美好' }}</text>
        <text class="row-text">{{ list[1].meaning || '寓意美好' }}</text>
      </view>
    </view>

    <view v-else class="empty-tip">
      <text>请选择两个名字进行对比</text>
    </view>

    <button class="back-btn" @click="goBack">返回取名结果</button>
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
  onLoad(options) {
    if (options.ids) {
      try {
        const ids = JSON.parse(decodeURIComponent(options.ids))
        this.fetchCompareData(ids)
      } catch (e) {
        console.error(e)
      }
    }
  },
  methods: {
    async fetchCompareData(ids) {
      try {
        this.list = await request.post('/api/naming/compare', ids)
      } catch (e) {
        console.error(e)
      }
    },
    goBack() {
      uni.navigateBack()
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
}

.subtitle {
  font-size: 26rpx;
  color: #888;
  margin-top: 8rpx;
  display: block;
}

.compare-box {
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
}

.name-header {
  display: flex;
  background: linear-gradient(90deg, #FF6B6B, #FF8E53);
}

.name-col {
  flex: 1;
  padding: 30rpx 10rpx;
  text-align: center;
}

.name-col.empty {
  flex: 0.8;
}

.compare-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
}

.compare-row {
  display: flex;
  align-items: center;
  border-bottom: 1rpx solid #f5f5f5;
  min-height: 90rpx;
}

.row-label {
  flex: 0.8;
  font-size: 26rpx;
  color: #888;
  text-align: center;
  padding: 20rpx 10rpx;
}

.row-val {
  flex: 1;
  font-size: 30rpx;
  color: #333;
  text-align: center;
  padding: 20rpx 10rpx;
  border-left: 1rpx solid #f5f5f5;
}

.row-val.highlight {
  color: #FF6B6B;
  font-weight: bold;
  font-size: 36rpx;
}

.compare-row.vertical {
  flex-direction: column;
  align-items: stretch;
}

.compare-row.vertical .row-label {
  text-align: left;
  border-bottom: 1rpx solid #f5f5f5;
  padding: 20rpx 24rpx;
}

.row-text {
  flex: 1;
  font-size: 28rpx;
  color: #555;
  padding: 24rpx;
  line-height: 1.6;
  border-left: 1rpx solid #f5f5f5;
}

.empty-tip {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
  font-size: 30rpx;
}

.back-btn {
  margin-top: 40rpx;
  height: 90rpx;
  line-height: 90rpx;
  background: #fff;
  color: #666;
  font-size: 30rpx;
  border-radius: 45rpx;
  border: none;
}

.back-btn::after {
  border: none;
}
</style>
