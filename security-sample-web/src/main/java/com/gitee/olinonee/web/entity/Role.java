package com.gitee.olinonee.web.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 角色表(Role)实体类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-05
 */
@Data
@TableName(value = "sys_role")
@AllArgsConstructor
@NoArgsConstructor
public class Role implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 角色名称
     */
    private String name;
    /**
     * 角色key
     */
    private String roleKey;
    /**
     * 角色状态（0正常 1停用）
     */
    private String status;
    /**
     * 创建用户
     */
    private Long createBy;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改用户
     */
    private Long updateBy;
    /**
     * 修改时间
     */
    private Date updateTime;
    /**
     * 是否删除（0未删除 1已删除）
     */
    private Integer delFlag;
    /**
     * 备注
     */
    private String remark;
}
