import request from '@/utils/request'

// 登录方法
export function login (sysUser) {
  return request({
    url: '/user/login',
    headers: {
      isToken: false
    },
    method: 'post',

    data: sysUser
  })
}

// 测试接口
export function testCors () {
  return request({
    url: '/test/testCors',
    method: 'post'
  })
}
