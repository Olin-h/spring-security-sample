package com.gitee.olinonee.boot.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 认证成功处理器（用于认证成功之后的逻辑）
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-07
 */
@Component
@Slf4j
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * 当用户成功通过身份验证时调用。
     *
     * @param request        请求
     * @param response       响应
     * @param authentication 认证
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.info(">>>> 认证成功了！");
    }
}
