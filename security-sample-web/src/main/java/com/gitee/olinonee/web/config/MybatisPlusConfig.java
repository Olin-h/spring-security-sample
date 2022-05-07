package com.gitee.olinonee.web.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis-plus配置类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Configuration
@MapperScan("com.gitee.olinonee.web.mapper")
public class MybatisPlusConfig {

}
