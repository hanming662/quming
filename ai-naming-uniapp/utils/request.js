export const BASE_URL = 'http://localhost:8080'

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
