package com.gitee.olinonee.web.service;

import com.gitee.olinonee.web.entity.SysUser;
import com.gitee.olinonee.web.utils.ResponseResult;

/**
 * 登录服务接口
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-25
 */
public interface ILoginService {
    /**
     * 用户登录
     *
     * @param user 用户信息
     * @return 统一封装的返回结果
     */
    ResponseResult<Object> login(SysUser user);

    /**
     * 退出登录
     *
     * @return 统一封装的返回结果
     */
    ResponseResult<Object> logout();
}
