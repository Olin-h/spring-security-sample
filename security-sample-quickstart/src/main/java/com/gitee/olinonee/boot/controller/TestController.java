package com.gitee.olinonee.boot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SpringSecurity测试接口
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-22
 */
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 调用该接口会自动跳转到SpringSecurity认证页面，需要输入用户名和密码
     * <p>用户名默认为：user；密码：项目启动会自动生成
     *
     * @return 输出测试信息
     */
    @GetMapping("/echo")
    public String echo() {
        return "Hello, SpringSecurity!";
    }
}
