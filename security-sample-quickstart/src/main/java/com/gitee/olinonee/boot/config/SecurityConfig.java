package com.gitee.olinonee.boot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * SpringSecurity配置类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-07
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final LogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    public SecurityConfig(AuthenticationSuccessHandler authenticationSuccessHandler, AuthenticationFailureHandler authenticationFailureHandler, LogoutSuccessHandler logoutSuccessHandler) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.logoutSuccessHandler = logoutSuccessHandler;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 表单登录
                .formLogin()
                // 配置认证成功处理器
                .successHandler(authenticationSuccessHandler)
                // 配置认证失败处理器
                .failureHandler(authenticationFailureHandler)
                // 配置注销成功处理器
                .and()
                .logout()
                .logoutSuccessHandler(logoutSuccessHandler)
                .and()
                // 所有请求全部需要鉴权认证
                .authorizeRequests()
                .anyRequest()
                .authenticated();
    }
}
