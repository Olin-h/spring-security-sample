package com.gitee.olinonee.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gitee.olinonee.web.entity.SysUser;

import java.util.List;

/**
 * 用户mapper接口
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 通过用户id查询对应的权限菜单
     *
     * @param id 用户id
     * @return 权限菜单集合
     */
    List<String> selectPermsByUserId(Long id);
}
