<script>
export default {
  onLaunch() {
    // Mock openid for dev
    const openid = uni.getStorageSync('openid')
    if (!openid) {
      const mockOpenid = 'user_' + Date.now()
      uni.setStorageSync('openid', mockOpenid)
    }
    console.log('App Launch')
  },
  onShow() {
    console.log('App Show')
  },
  onHide() {
    console.log('App Hide')
  }
}
</script>

<style>
/* global styles */
</style>
