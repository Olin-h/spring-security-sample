package com.gitee.olinonee.web.controller;

import com.gitee.olinonee.web.utils.ResponseResult;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/echo")
    @PreAuthorize("hasAuthority('system:test:list')")
    public ResponseResult<String> echo() {
        return ResponseResult.data("Spring Security!");
    }

    @RequestMapping("/testCors")
    public ResponseResult<String> testCors() {
        return ResponseResult.success("测试接口调用成功！");
    }

    @RequestMapping("/testCustomizeAuthorize")
    @PreAuthorize("@ca.hasAuthority('system:test:list')")
    public ResponseResult<String> testCustomizeAuthorize() {
        return ResponseResult.success("CustomizeAuthorize！");
    }
}
