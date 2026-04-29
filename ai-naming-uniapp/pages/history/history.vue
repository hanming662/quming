<template>
  <view class="container">
    <view class="header">
      <text class="title">历史记录</text>
      <text class="subtitle">查看、删除、复用你的取名方案</text>
    </view>

    <view class="toolbar" v-if="list.length > 0">
      <button class="toolbar-btn danger" @click="clearHistory">清空历史</button>
    </view>

    <view v-if="list.length > 0" class="history-list">
      <view
        v-for="item in list"
        :key="item.id"
        class="history-card"
      >
        <view class="card-top" @click="openHistory(item)">
          <view>
            <text class="record-title">{{ getRecordTitle(item) }}</text>
            <text class="record-meta">{{ getSummary(item) }}</text>
          </view>
          <text class="arrow">›</text>
        </view>

        <view class="chips" @click="openHistory(item)">
          <text class="chip">{{ genderText(item.gender) }}</text>
          <text class="chip">{{ styleText(item.style) }}</text>
          <text class="chip" v-if="item.motherSurname">母姓 {{ item.motherSurname }}</text>
        </view>

        <text v-if="item.prompt" class="prompt" @click="openHistory(item)">{{ item.prompt }}</text>

        <view v-if="item.topNames && item.topNames.length" class="top-names" @click="openHistory(item)">
          <text class="top-label">推荐前 3 名</text>
          <view class="top-name-list">
            <text v-for="name in item.topNames" :key="name.id" class="top-name">
              {{ name.name }} · {{ name.totalScore }}分
            </text>
          </view>
        </view>

        <view class="actions">
          <button class="toolbar-btn ghost" @click.stop="reuseCondition(item)">重新使用这次条件</button>
          <button class="toolbar-btn danger" @click.stop="removeHistory(item)">删除</button>
        </view>
      </view>
    </view>

    <view v-if="list.length > 0 && !hasMore" class="end-tip">
      <text class="end-tip-text">已经到底了，没有更多历史记录了</text>
    </view>

    <view v-if="list.length === 0" class="empty-state">
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
      list: [],
      page: 1,
      pageSize: 10,
      hasMore: true,
      loading: false
    }
  },
  onShow() {
    this.resetAndLoad()
  },
  onReachBottom() {
    this.onScrollToBottom()
  },
  methods: {
    async resetAndLoad() {
      this.list = []
      this.page = 1
      this.hasMore = true
      await this.loadHistory()
    },
    async loadHistory() {
      if (this.loading || !this.hasMore) return
      this.loading = true
      try {
        const list = await request.get(`/api/history/page?page=${this.page}&pageSize=${this.pageSize}`)
        const records = list || []
        this.list = this.list.concat(records)
        this.hasMore = records.length === this.pageSize
        if (this.hasMore) {
          this.page += 1
        }
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    onScrollToBottom() {
      this.loadHistory()
    },
    async removeHistory(item) {
      try {
        await request.del('/api/history/' + item.id)
        uni.showToast({ title: '已删除', icon: 'none' })
        await this.resetAndLoad()
      } catch (e) {
        console.error(e)
      }
    },
    async clearHistory() {
      try {
        await request.del('/api/history')
        uni.showToast({ title: '已清空', icon: 'none' })
        await this.resetAndLoad()
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
      if (item.allNames && item.allNames.length > 0) {
        parts.push(`最佳 ${item.allNames[0].name}`)
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
      uni.setStorageSync('current_name', (item.allNames && item.allNames[0]) || {})
      uni.setStorageSync('naming_result', item.allNames || [])
      uni.setStorageSync('naming_history_form', {
        fatherSurname: item.fatherSurname,
        motherSurname: item.motherSurname,
        gender: item.gender,
        style: item.style,
        prompt: item.prompt
      })
      uni.navigateTo({ url: '/pages/result/result' })
    },
    reuseCondition(item) {
      uni.setStorageSync('naming_history_form', {
        fatherSurname: item.fatherSurname,
        motherSurname: item.motherSurname,
        gender: item.gender,
        style: item.style,
        prompt: item.prompt
      })
      uni.switchTab({ url: '/pages/index/index' })
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
  margin-bottom: 24rpx;
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

.toolbar {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.toolbar-btn {
  flex: 1;
  height: 76rpx;
  line-height: 76rpx;
  border-radius: 38rpx;
  font-size: 26rpx;
}

.toolbar-btn.ghost {
  background: #fff;
  color: #666;
  border: 1rpx solid #eee;
}

.toolbar-btn.danger {
  background: #fff0f0;
  color: #ff4d4f;
  border: 1rpx solid #ffd6d6;
}

.toolbar-btn::after {
  border: none;
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

.actions {
  display: flex;
  gap: 16rpx;
  margin-top: 20rpx;
}

.end-tip {
  padding: 24rpx 0 40rpx;
  text-align: center;
}

.end-tip-text {
  font-size: 24rpx;
  color: #999;
}

.empty-state {
  padding-top: 220rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
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
