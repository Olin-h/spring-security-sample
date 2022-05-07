package com.gitee.olinonee.web;

import com.alibaba.fastjson.JSON;
import com.gitee.olinonee.web.entity.Menu;
import com.gitee.olinonee.web.entity.Role;
import com.gitee.olinonee.web.entity.SysUser;
import com.gitee.olinonee.web.mapper.MenuMapper;
import com.gitee.olinonee.web.mapper.RoleMapper;
import com.gitee.olinonee.web.mapper.SysUserMapper;
import com.gitee.olinonee.web.utils.ResponseResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * mybatis-plus测试类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@SpringBootTest
public class MybatisPlusTests {

    @Resource
    private SysUserMapper userMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private MenuMapper menuMapper;

    @Test
    public void testSysUserMapper(){
        List<SysUser> users = userMapper.selectList(null);
        System.out.println(users);
    }

    /**
     * 测试SpringSecurity自带的BCrypt加密器
     * <Strong>注意：</Strong>
     * <p>使用相同明文进行BCrypt加密，每次产生的秘钥都不同，但是验证能进行成功匹配。
     * <a href="https://www.cnblogs.com/soft-engineer/p/15724281.html">BCrypt加密原理</a>
     */
    @Test
    public void testBCryptPassword() {
        this.encodeBCryptPassword();
    }

    @Test
    public void testInsertSysUser() {
        SysUser user = new SysUser();
        user.setUserName("dev");
        user.setNickName("欧尼酱");
        user.setPassword(this.encodeBCryptPassword());
        user.setStatus("0");
        user.setPhoneNumber("110-120-119");
        user.setEmail("dev@qq.com");
        user.setSex("0");
        user.setCreateTime(new Date());
        user.setDelFlag(0);
        int result = userMapper.insert(user);
        System.out.println("插入影响的行数：" + result);
    }

    @Test
    public void testInsertRole() {
        Role role = new Role();
        role.setName("CEO");
        role.setRoleKey("ceo");
        role.setStatus("0");
        role.setCreateTime(new Date());
        role.setDelFlag(0);
        int result = roleMapper.insert(role);
        System.out.println("插入影响的行数：" + result);
    }

    @Test
    public void testInsertMenu() {
        Menu menu = new Menu();
        menu.setMenuName("测试管理");
        menu.setPath("test");
        menu.setComponent("system/test/index");
        menu.setVisible("0");
        menu.setStatus("0");
        menu.setPerms("system:test:list");
        menu.setCreateTime(new Date());
        menu.setDelFlag(0);
        int result = menuMapper.insert(menu);
        System.out.println("插入影响的行数：" + result);
    }

    @Test
    public void testSelectPermsByUserId() {
        List<String> perms = userMapper.selectPermsByUserId(1522097140371689474L);
        perms.forEach(System.out::println);
    }

    @Test
    public void testFailureMsg() {
        ResponseResult<Object> failureResult = ResponseResult.failure(HttpStatus.FORBIDDEN, "权限不足");
        String failureStr = JSON.toJSONString(failureResult);
        System.out.println("失败的json信息为：" + failureStr);
    }

    private String encodeBCryptPassword() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode("123456");
        System.out.println("BCryptPasswordEncoder进行加密的秘钥:" + encode);
        return encode;
    }
}
