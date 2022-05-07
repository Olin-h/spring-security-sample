package com.gitee.olinonee.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.gitee.olinonee.web.utils.ResponseResult;
import com.gitee.olinonee.web.utils.WebUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SpringSecurity自定义授权失败访问拒绝处理器实现类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-05
 */
@Service
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    /**
     * 处理拒绝访问失败。
     *
     * @param request               请求
     * @param response              响应
     * @param accessDeniedException 访问拒绝异常
     * @throws IOException      io异常
     * @throws ServletException servlet异常
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ResponseResult<Object> failureResult = ResponseResult.failure(HttpStatus.FORBIDDEN, "权限不足");
        String failureStr = JSON.toJSONString(failureResult);
        WebUtil.renderString(response, failureStr);
    }
}
