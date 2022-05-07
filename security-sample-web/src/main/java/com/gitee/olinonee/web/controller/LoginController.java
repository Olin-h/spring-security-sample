package com.gitee.olinonee.web.controller;

import com.gitee.olinonee.web.entity.SysUser;
import com.gitee.olinonee.web.service.ILoginService;
import com.gitee.olinonee.web.utils.ResponseResult;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录控制器
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-25
 */
@RestController
@AllArgsConstructor
public class LoginController {

    private ILoginService loginService;

    /**
     * 用户登录接口
     */
    @PostMapping("/user/login")
    public ResponseResult<Object> login(@RequestBody SysUser user){
        return loginService.login(user);
    }

    /**
     * 退出登录
     */
    @GetMapping("/user/logout")
    public ResponseResult<Object> logout() {
        return loginService.logout();
    }
}
