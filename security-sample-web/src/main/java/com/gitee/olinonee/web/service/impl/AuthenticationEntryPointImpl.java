package com.gitee.olinonee.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.gitee.olinonee.web.utils.ResponseResult;
import com.gitee.olinonee.web.utils.WebUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *  SpringSecurity自定义认证失败处理实现类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-05
 */
@Service
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    /**
     * 启动身份验证方案
     *
     *
     * @param request       请求
     * @param response      响应
     * @param authException 认证异常
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        ResponseResult<Object> failureResult = ResponseResult.failure(HttpStatus.UNAUTHORIZED, "认证失败请重新登录");
        String failureStr = JSON.toJSONString(failureResult);
        WebUtil.renderString(response, failureStr);
    }
}
