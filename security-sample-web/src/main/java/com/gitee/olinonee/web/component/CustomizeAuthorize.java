package com.gitee.olinonee.web.component;

import com.gitee.olinonee.web.entity.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义权限校验方法
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-06
 */
@Component(value = "ca")
public class CustomizeAuthorize {

    public boolean hasAuthority(String authority){
        // 获取当前用户的权限
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> permissions = loginUser.getPermissionList();
        // 判断用户权限集合中是否存在authority
        return permissions.contains(authority);
    }
}
