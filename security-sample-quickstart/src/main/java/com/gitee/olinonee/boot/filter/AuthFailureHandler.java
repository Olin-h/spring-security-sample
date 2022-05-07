package com.gitee.olinonee.boot.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 认证失败处理器（用于认证失败之后的逻辑）
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-07
 */
@Component
@Slf4j
public class AuthFailureHandler implements AuthenticationFailureHandler {
    /**
     * 当身份验证尝试失败时调用
     *
     * @param request   请求
     * @param response  响应
     * @param exception 异常
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        log.error(">>>> 认证失败了！");
    }
}
