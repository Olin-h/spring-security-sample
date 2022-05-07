package com.gitee.olinonee.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gitee.olinonee.web.entity.LoginUser;
import com.gitee.olinonee.web.entity.SysUser;
import com.gitee.olinonee.web.mapper.SysUserMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 用户详情服务接口实现类 （主要是实现SpringSecurity对应的接口）
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private SysUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查询用户信息
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUserName, username);
        SysUser user = userMapper.selectOne(wrapper);

        // 如果查询不到数据就通过抛出异常来给出提示
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        // 测试写法
        // List<String> permissionList = Collections.singletonList("test");
        // 根据用户查询权限信息 添加到LoginUser中
        List<String> permissionList = userMapper.selectPermsByUserId(user.getId());

        // 封装成UserDetails对象返回
        return new LoginUser(user, permissionList);
    }
}
