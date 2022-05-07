package com.gitee.olinonee.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.web.DefaultSecurityFilterChain;

import javax.servlet.Filter;
import java.util.List;

/**
 * 启动器
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-22
 */
@SpringBootApplication
public class SecuritySampleQuickStartApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(SecuritySampleQuickStartApplication.class, args);
        // 这里主要查看DefaultSecurityFilterChain对应的过滤器链
        DefaultSecurityFilterChain chain = run.getBean(DefaultSecurityFilterChain.class);
        // 查询过滤器链拥有的过滤器
        List<Filter> filters = chain.getFilters();
        filters.forEach(System.out::println);
    }
}
