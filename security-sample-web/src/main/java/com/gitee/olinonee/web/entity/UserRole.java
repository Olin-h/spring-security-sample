package com.gitee.olinonee.web.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户角色表(UserRole)实体对象
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-05
 */
@Data
@TableName(value = "sys_user_role")
public class UserRole implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private long userId;
    /**
     * 角色id
     */
    private long roleId;
}
