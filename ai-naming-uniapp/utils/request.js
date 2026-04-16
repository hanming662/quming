// 请根据实际部署环境修改 BASE_URL
// #ifdef H5
export const BASE_URL = 'http://localhost:8080'
// #endif
// #ifndef H5
// 微信小程序 / App 请使用真实后端域名（必须 HTTPS 且备案）
export const BASE_URL = 'http://localhost:8082'
// #endif

function request(options) {
  return new Promise((resolve, reject) => {
    const openid = uni.getStorageSync('openid') || ''
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'X-Openid': openid,
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const data = res.data
          if (data.code === 200) {
            resolve(data.data)
          } else {
            uni.showToast({ title: data.message || '请求失败', icon: 'none' })
            reject(data)
          }
        } else {
          uni.showToast({ title: '网络错误', icon: 'none' })
          reject(res)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '请求失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

request.get = (url, data) => request({ url, method: 'GET', data })
request.post = (url, data) => request({ url, method: 'POST', data })

export default request
