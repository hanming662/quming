<template>
  <view class="container">
    <view class="header">
      <text class="title">📚 历史记录</text>
      <text class="subtitle">查看你曾经生成过的取名方案</text>
    </view>

    <view v-if="list.length > 0" class="history-list">
      <view
        v-for="item in list"
        :key="item.id"
        class="history-card"
        @click="openHistory(item)"
      >
        <view class="card-top">
          <view>
            <text class="record-title">{{ getRecordTitle(item) }}</text>
            <text class="record-meta">{{ getSummary(item) }}</text>
          </view>
          <text class="arrow">›</text>
        </view>

        <view class="chips">
          <text class="chip">{{ genderText(item.gender) }}</text>
          <text class="chip">{{ styleText(item.style) }}</text>
          <text class="chip" v-if="item.motherSurname">母姓 {{ item.motherSurname }}</text>
        </view>

        <text v-if="item.prompt" class="prompt">{{ item.prompt }}</text>

        <view v-if="item.topNames && item.topNames.length" class="top-names">
          <text class="top-label">推荐前 3 名</text>
          <view class="top-name-list">
            <text v-for="name in item.topNames" :key="name.id" class="top-name">
              {{ name.name }} · {{ name.totalScore }}分
            </text>
          </view>
        </view>
      </view>
    </view>

    <view v-else class="empty-state">
      <text class="empty-icon">🗂️</text>
      <text class="empty-text">暂无历史记录</text>
      <text class="empty-sub">先去生成一组名字吧</text>
      <button class="action-btn" @click="goHome">去取名</button>
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
    this.loadHistory()
  },
  methods: {
    async loadHistory() {
      try {
        const list = await request.get('/api/history?limit=20')
        this.list = list || []
      } catch (e) {
        console.error(e)
      }
    },
    formatTime(value) {
      if (!value) return ''
      const date = new Date(value)
      const pad = n => String(n).padStart(2, '0')
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
    },
    getRecordTitle(item) {
      const surname = item && item.fatherSurname ? item.fatherSurname : '未知'
      return `${surname}姓取名`
    },
    getSummary(item) {
      const parts = []
      if (item.createTime) parts.push(this.formatTime(item.createTime))
      if (item.count) parts.push(`${item.count} 个候选`)
      if (item.topNames && item.topNames.length > 0) {
        parts.push(`最佳 ${item.topNames[0].name}`)
      }
      return parts.join(' · ') || '暂无记录详情'
    },
    genderText(value) {
      const map = {
        boy: '男宝',
        girl: '女宝'
      }
      return map[value] || '不限性别'
    },
    styleText(value) {
      const map = {
        modern: '现代',
        classic: '古典',
        literary: '文艺',
        dignified: '稳重'
      }
      return map[value] || '默认风格'
    },
    openHistory(item) {
      uni.setStorageSync('current_name', (item.topNames && item.topNames[0]) || {})
      uni.setStorageSync('naming_result', item.topNames || [])
      uni.navigateTo({ url: '/pages/result/result' })
    },
    goHome() {
      uni.switchTab({ url: '/pages/index/index' })
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  padding: 30rpx;
  background: #f5f7fa;
}

.header {
  margin-bottom: 30rpx;
}

.title {
  display: block;
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
}

.subtitle {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  color: #888;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.history-card {
  padding: 28rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.record-title {
  display: block;
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}

.record-meta {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #888;
}

.arrow {
  font-size: 40rpx;
  color: #ccc;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
}

.chip {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  background: #f8f9fa;
  color: #666;
  font-size: 22rpx;
}

.prompt {
  display: block;
  margin-top: 16rpx;
  font-size: 26rpx;
  color: #555;
  line-height: 1.6;
}

.top-names {
  margin-top: 18rpx;
  padding-top: 18rpx;
  border-top: 1rpx solid #f1f1f1;
}

.top-label {
  display: block;
  margin-bottom: 10rpx;
  font-size: 24rpx;
  color: #999;
}

.top-name-list {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.top-name {
  font-size: 26rpx;
  color: #444;
}

.empty-state {
  padding-top: 220rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #666;
}

.empty-sub {
  margin-top: 8rpx;
  font-size: 26rpx;
  color: #aaa;
}

.action-btn {
  margin-top: 24rpx;
  height: 84rpx;
  line-height: 84rpx;
  background: #ff6b6b;
  color: #fff;
  border-radius: 42rpx;
  font-size: 28rpx;
}

.action-btn::after {
  border: none;
}
</style>
