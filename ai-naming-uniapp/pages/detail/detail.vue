<template>
  <view class="container">
    <view class="name-header">
      <text class="big-name">{{ nameData.name }}</text>
      <text class="pinyin">{{ nameData.pinyin }}</text>
      <view class="score-circle">
        <text class="score-num">{{ nameData.totalScore || 80 }}</text>
        <text class="score-label">综合评分</text>
      </view>
    </view>

    <view class="section">
      <text class="section-title">📊 评分维度</text>
      <view class="score-row">
        <view class="score-item">
          <text class="item-val">{{ nameData.yinyunScore || 8 }}</text>
          <text class="item-label">音韵评分</text>
        </view>
        <view class="score-item">
          <text class="item-val">{{ nameData.timesScore || 8 }}</text>
          <text class="item-label">时代感</text>
        </view>
        <view class="score-item">
          <text class="item-val">{{ nameData.wuxing || '平衡' }}</text>
          <text class="item-label">五行属性</text>
        </view>
      </view>
    </view>

    <view class="section">
      <text class="section-title">🌟 寓意解读</text>
      <text class="paragraph">{{ nameData.meaning || '寓意美好，前程似锦。' }}</text>
    </view>

    <view class="section">
      <text class="section-title">💡 取名理由</text>
      <text class="paragraph">{{ nameData.reason || '结合音韵美学与时代特征精心推荐。' }}</text>
    </view>

    <view class="section analyze-section">
      <text class="section-title">🔮 AI 深度分析</text>
      <view v-if="!analyzing && !analysisContent" class="analyze-btn" @click="startAnalyze">
        <text>点击获取深度分析</text>
      </view>
      <view v-else class="analysis-content">
        <text class="paragraph typing">{{ analysisContent }}</text>
        <view v-if="analyzing" class="typing-cursor"></view>
      </view>
    </view>
  </view>
</template>

<script>
import request, { BASE_URL } from '@/utils/request.js'

export default {
  data() {
    return {
      nameData: {},
      analyzing: false,
      analysisContent: '',
      taskId: null,
      pollTimer: null,
      es: null,
      requestTask: null
    }
  },
  onLoad() {
    this.nameData = uni.getStorageSync('current_name') || {}
  },
  onUnload() {
    this.clearConnection()
  },
  methods: {
    clearConnection() {
      if (this.pollTimer) {
        clearInterval(this.pollTimer)
        this.pollTimer = null
      }
      if (this.es) {
        this.es.close()
        this.es = null
      }
      if (this.requestTask) {
        try {
          this.requestTask.abort()
        } catch (e) {}
        this.requestTask = null
      }
    },
    async startAnalyze() {
      this.analyzing = true
      this.analysisContent = ''
      try {
        const vo = await request.post('/api/naming/deepAnalyze', {
          name: this.nameData.name,
          gender: this.nameData.gender || 'boy',
          fatherSurname: this.nameData.fatherSurname || '',
          motherSurname: this.nameData.motherSurname || ''
        })
        this.taskId = vo.taskId
        this.connectStream(vo.taskId)
      } catch (e) {
        this.analyzing = false
        console.error(e)
      }
    },
    connectStream(taskId) {
      // #ifdef H5
      this.connectStreamH5(taskId)
      // #endif
      // #ifdef MP-WEIXIN
      this.connectStreamWx(taskId)
      // #endif
      // #ifndef H5 || MP-WEIXIN
      this.pollTimer = setInterval(() => {
        this.pollAnalysis()
      }, 800)
      // #endif
    },
    connectStreamH5(taskId) {
      const es = new EventSource(BASE_URL + '/api/naming/stream/' + taskId)
      this.es = es
      es.onmessage = (event) => {
        if (event.data === '[DONE]') {
          this.analyzing = false
          es.close()
          return
        }
        this.analysisContent += event.data
      }
      es.onerror = () => {
        this.analyzing = false
        es.close()
      }
    },
    connectStreamWx(taskId) {
      const openid = uni.getStorageSync('openid') || ''
      const requestTask = wx.request({
        url: BASE_URL + '/api/naming/stream/' + taskId,
        method: 'GET',
        header: { 'X-Openid': openid },
        enableChunked: true,
        success: () => {
          this.analyzing = false
        },
        fail: () => {
          this.analyzing = false
        }
      })
      this.requestTask = requestTask
      requestTask.onChunkReceived((res) => {
        const buffer = new Uint8Array(res.data)
        const text = new TextDecoder('utf-8').decode(buffer)
        this.parseSseChunks(text)
      })
    },
    parseSseChunks(text) {
      const lines = text.split('\n')
      for (let i = 0; i < lines.length; i++) {
        const line = lines[i].trim()
        if (line.startsWith('data:')) {
          const data = line.substring(5).trim()
          if (data === '[DONE]') {
            this.analyzing = false
            this.clearConnection()
            return
          }
          if (data) {
            this.analysisContent += data
          }
        }
      }
    },
    async pollAnalysis() {
      try {
        const text = await request.get('/api/naming/stream/' + this.taskId)
        if (text.includes('[DONE]')) {
          this.analysisContent = text.replace('[DONE]', '')
          this.analyzing = false
          clearInterval(this.pollTimer)
          this.pollTimer = null
        } else {
          this.analysisContent = text
        }
      } catch (e) {
        console.error(e)
      }
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

.name-header {
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  border-radius: 24rpx;
  padding: 48rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #fff;
  margin-bottom: 30rpx;
}

.big-name {
  font-size: 72rpx;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.pinyin {
  font-size: 28rpx;
  opacity: 0.9;
  margin-bottom: 24rpx;
}

.score-circle {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 4rpx solid rgba(255,255,255,0.4);
}

.score-num {
  font-size: 48rpx;
  font-weight: bold;
}

.score-label {
  font-size: 22rpx;
  opacity: 0.9;
}

.section {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 20rpx;
}

.score-row {
  display: flex;
  justify-content: space-around;
}

.score-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.item-val {
  font-size: 36rpx;
  font-weight: bold;
  color: #FF6B6B;
}

.item-label {
  font-size: 24rpx;
  color: #888;
}

.paragraph {
  font-size: 28rpx;
  color: #555;
  line-height: 1.8;
}

.analyze-btn {
  background: #F8F9FA;
  border-radius: 16rpx;
  padding: 40rpx;
  text-align: center;
  color: #FF6B6B;
  font-size: 30rpx;
}

.analysis-content {
  background: #F8F9FA;
  border-radius: 16rpx;
  padding: 24rpx;
}

.typing {
  white-space: pre-wrap;
}

.typing-cursor {
  display: inline-block;
  width: 4rpx;
  height: 32rpx;
  background: #FF6B6B;
  margin-left: 8rpx;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
</style>
