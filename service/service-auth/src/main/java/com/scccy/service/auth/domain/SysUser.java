package com.scccy.service.auth.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.scccy.common.modules.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户信息表
 */
@Schema(description = "用户信息表")
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_user")
public class SysUser extends BaseEntity implements Serializable {
    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 部门ID
     */
    @TableField(value = "dept_id")
    @Schema(description = "部门ID")
    private Long deptId;

    /**
     * 用户账号
     */
    @TableField(value = "user_name")
    @Schema(description = "用户账号")
    private String userName;

    /**
     * 用户昵称
     */
    @TableField(value = "nick_name")
    @Schema(description = "用户昵称")
    private String nickName;

    /**
     * 用户类型（00系统用户）
     */
    @TableField(value = "user_type")
    @Schema(description = "用户类型（00系统用户）")
    private String userType;

    /**
     * 用户邮箱
     */
    @TableField(value = "email")
    @Schema(description = "用户邮箱")
    private String email;

    /**
     * 手机号码
     */
    @TableField(value = "phonenumber")
    @Schema(description = "手机号码")
    private String phonenumber;

    /**
     * 用户性别（0男 1女 2未知）
     */
    @TableField(value = "sex")
    @Schema(description = "用户性别（0男 1女 2未知）")
    private String sex;

    /**
     * 头像地址
     */
    @TableField(value = "avatar")
    @Schema(description = "头像地址")
    private String avatar;

    /**
     * 密码
     */
    @TableField(value = "`password`")
    @Schema(description = "密码")
    private String password;

    /**
     * 账号状态（0正常 1停用）
     */
    @TableField(value = "`status`")
    @Schema(description = "账号状态（0正常 1停用）")
    private Integer status;


    /**
     * 最后登录IP
     */
    @TableField(value = "login_ip")
    @Schema(description = "最后登录IP")
    private String loginIp;

    /**
     * 最后登录时间
     */
    @TableField(value = "login_date")
    @Schema(description = "最后登录时间")
    private Date loginDate;

    /**
     * 密码最后更新时间
     */
    @TableField(value = "pwd_update_date")
    @Schema(description = "密码最后更新时间")
    private Date pwdUpdateDate;


    /**
     * 备注
     */
    @TableField(value = "remark")
    @Schema(description = "备注")
    private String remark;
}