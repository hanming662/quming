<template>
  <view class="container">
    <view class="header">
      <text class="title">🎯 AI智能取名</text>
      <text class="subtitle">融合易经五行、现代审美与音韵学，为宝宝取个好名字</text>
    </view>

    <view class="form-card">
      <view class="form-item">
        <text class="label">父姓</text>
        <input
          class="input"
          v-model="form.fatherSurname"
          placeholder="请输入父姓"
          maxlength="2"
        />
      </view>

      <view class="form-item">
        <text class="label">母姓（可选）</text>
        <input
          class="input"
          v-model="form.motherSurname"
          placeholder="可融入名字构思"
          maxlength="2"
        />
      </view>

      <view class="form-item">
        <text class="label">宝宝性别</text>
        <view class="gender-group">
          <view
            class="gender-item"
            :class="{ active: form.gender === 'boy' }"
            @click="form.gender = 'boy'"
          >
            <text class="icon">👦</text>
            <text>男宝</text>
          </view>
          <view
            class="gender-item"
            :class="{ active: form.gender === 'girl' }"
            @click="form.gender = 'girl'"
          >
            <text class="icon">👧</text>
            <text>女宝</text>
          </view>
        </view>
      </view>

      <view class="form-item">
        <text class="label">名字风格</text>
        <view class="style-group">
          <view
            class="style-item"
            :class="{ active: form.style === 'modern' }"
            @click="form.style = 'modern'"
          >现代</view>
          <view
            class="style-item"
            :class="{ active: form.style === 'classic' }"
            @click="form.style = 'classic'"
          >古典</view>
          <view
            class="style-item"
            :class="{ active: form.style === 'literary' }"
            @click="form.style = 'literary'"
          >文艺</view>
          <view
            class="style-item"
            :class="{ active: form.style === 'dignified' }"
            @click="form.style = 'dignified'"
          >稳重</view>
        </view>
      </view>

      <view class="form-item">
        <text class="label">您的期望（选填）</text>
        <textarea
          class="textarea"
          v-model="form.prompt"
          placeholder="如：希望名字带'水'旁，寓意聪慧灵动..."
          maxlength="100"
        />
      </view>

      <view class="form-item">
        <text class="label">生成数量</text>
        <slider
          class="slider"
          :value="form.count"
          :min="3"
          :max="10"
          @change="e => form.count = e.detail.value"
        />
        <text class="count-text">{{ form.count }} 个</text>
      </view>

      <button class="submit-btn" @click="submit">✨ 开始AI取名</button>
    </view>
  </view>
</template>

<script>
import request from '@/utils/request.js'

export default {
  data() {
    return {
      form: {
        fatherSurname: '',
        motherSurname: '',
        gender: 'boy',
        style: 'modern',
        prompt: '',
        count: 5
      }
    }
  },
  methods: {
    async submit() {
      if (!this.form.fatherSurname) {
        uni.showToast({ title: '请输入父姓', icon: 'none' })
        return
      }
      uni.showLoading({ title: 'AI思考中...' })
      try {
        const list = await request.post('/api/naming/generate', this.form)
        uni.setStorageSync('naming_result', list)
        uni.navigateTo({ url: '/pages/result/result' })
      } catch (e) {
        console.error(e)
      } finally {
        uni.hideLoading()
      }
    }
  }
}
</script>

<style scoped>
.container {
  padding: 30rpx;
  background: linear-gradient(135deg, #FFF5F5 0%, #FFF 50%, #F5F8FF 100%);
  min-height: 100vh;
}

.header {
  margin-bottom: 40rpx;
  text-align: center;
}

.title {
  font-size: 48rpx;
  font-weight: bold;
  background: linear-gradient(90deg, #FF6B6B, #FF8E53);
  -webkit-background-clip: text;
  color: transparent;
  display: block;
  margin-bottom: 16rpx;
}

.subtitle {
  font-size: 26rpx;
  color: #888;
}

.form-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.06);
}

.form-item {
  margin-bottom: 32rpx;
}

.label {
  font-size: 28rpx;
  color: #333;
  font-weight: 600;
  margin-bottom: 16rpx;
  display: block;
}

.input {
  height: 88rpx;
  background: #F8F9FA;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 30rpx;
  color: #333;
}

.textarea {
  height: 160rpx;
  background: #F8F9FA;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  font-size: 30rpx;
  color: #333;
  width: 100%;
  box-sizing: border-box;
}

.gender-group {
  display: flex;
  gap: 20rpx;
}

.gender-item {
  flex: 1;
  height: 120rpx;
  background: #F8F9FA;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  font-size: 28rpx;
  color: #666;
  transition: all 0.3s;
}

.gender-item.active {
  background: #FFF0F0;
  border: 2rpx solid #FF6B6B;
  color: #FF6B6B;
}

.icon {
  font-size: 48rpx;
}

.style-group {
  display: flex;
  gap: 16rpx;
  flex-wrap: wrap;
}

.style-item {
  padding: 16rpx 32rpx;
  background: #F8F9FA;
  border-radius: 32rpx;
  font-size: 26rpx;
  color: #666;
  transition: all 0.3s;
}

.style-item.active {
  background: #FF6B6B;
  color: #fff;
}

.slider {
  width: 100%;
}

.count-text {
  text-align: center;
  color: #FF6B6B;
  font-weight: 600;
  font-size: 28rpx;
}

.submit-btn {
  margin-top: 40rpx;
  height: 96rpx;
  line-height: 96rpx;
  background: linear-gradient(90deg, #FF6B6B, #FF8E53);
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: 48rpx;
  border: none;
}

.submit-btn::after {
  border: none;
}
</style>
