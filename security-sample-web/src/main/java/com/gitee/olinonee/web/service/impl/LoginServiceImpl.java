package com.gitee.olinonee.web.service.impl;

import com.gitee.olinonee.web.entity.LoginUser;
import com.gitee.olinonee.web.entity.SysUser;
import com.gitee.olinonee.web.service.ILoginService;
import com.gitee.olinonee.web.utils.JwtUtil;
import com.gitee.olinonee.web.utils.RedisCache;
import com.gitee.olinonee.web.utils.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 登录服务接口实现类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-25
 */
@Service
public class LoginServiceImpl implements ILoginService {
    private final AuthenticationManager authenticationManager;
    private final RedisCache redisCache;

    @Autowired
    public LoginServiceImpl(AuthenticationManager authenticationManager, RedisCache redisCache) {
        this.authenticationManager = authenticationManager;
        this.redisCache = redisCache;
    }

    /**
     * 用户登录
     *
     * @param user 用户信息
     * @return 统一封装的返回结果
     */
    @Override
    public ResponseResult<Object> login(SysUser user) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        if (Objects.isNull(authenticate)) {
            throw new RuntimeException("用户名或密码错误");
        }
        // 使用userid生成token
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        String userId = loginUser.getUser().getId().toString();
        String jwt = JwtUtil.createJWT(userId);
        // authenticate存入redis
        redisCache.setCacheObject("login:" + userId, loginUser);
        // 把token响应给前端
        Map<String, String> map = new HashMap<>();
        map.put("token", jwt);
        return ResponseResult.data(200, "登陆成功", map);
    }

    /**
     * 退出登录
     *
     * @return 统一封装的返回结果
     */
    @Override
    public ResponseResult<Object> logout() {
        // 获取SecurityContextHolder中的用户id
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 这里为什么不需要判空？
        // 是因为登录之前已经做了认证处理、非法token的处理以及redis键过期之后的处理，可以查看对应JwtAuthenticationTokenFilter类处理逻辑
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();
        // 删除redis中对应的键
        redisCache.deleteObject("login:" + userId);
        return ResponseResult.success("退出成功!");
    }
}
