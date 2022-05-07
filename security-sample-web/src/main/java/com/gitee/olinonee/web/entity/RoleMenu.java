package com.gitee.olinonee.web.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色菜单关联表(RoleMenu)实体对象
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-05
 */
@Data
@TableName(value = "sys_role_menu")
public class RoleMenu implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 角色id
     */
    private long roleId;
    /**
     * 菜单id
     */
    private long menuId;
}
