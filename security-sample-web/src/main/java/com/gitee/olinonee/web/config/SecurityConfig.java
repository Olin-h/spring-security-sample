package com.gitee.olinonee.web.config;

import com.gitee.olinonee.web.filter.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

/**
 * SpringSecurity配置类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    public SecurityConfig(JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter, AccessDeniedHandler accessDeniedHandler, AuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtAuthenticationTokenFilter = jwtAuthenticationTokenFilter;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    /**
     * 使用的加密器 bean对象
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 关闭csrf
                .csrf().disable()
                // 不通过Session获取SecurityContext
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 对于登录接口 允许匿名访问
                .antMatchers("/user/login").anonymous()
                .antMatchers("/test/testCors").hasAuthority("system:test:list")
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated();

        // 把token校验过滤器添加到过滤器链中
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        // SpringSecurity统一异常拦截处理
        http.exceptionHandling()
                // 认证失败相关异常处理
                .authenticationEntryPoint(authenticationEntryPoint)
                // 授权失败相关异常处理
                .accessDeniedHandler(accessDeniedHandler);

        // 允许跨域
        http.cors().configurationSource(corsConfigurationSource());
        // http.cors();
    }

    /**
     * 认证管理器 bean对象
     *
     * @return AuthenticationManager
     * @throws Exception 异常
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration configuration = new CorsConfiguration();
        // 是否允许cookie
        configuration.setAllowCredentials(true);
        // 设置允许跨域请求的域名
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        // 设置允许的请求方式
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PUT"));
        // 设置允许的header属性
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        // 跨域允许时间为1小时
        configuration.setMaxAge(Duration.ofHours(1));
        // 设置允许跨域的路径
        source.registerCorsConfiguration("/**",configuration);
        return source;
    }
}
