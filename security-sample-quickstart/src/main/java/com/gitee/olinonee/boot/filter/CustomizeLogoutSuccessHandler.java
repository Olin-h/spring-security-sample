package com.gitee.olinonee.boot.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义退出成功的处理器
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-07
 */
@Component
@Slf4j
public class CustomizeLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.info(">>>> 注销成功！");
    }
}
