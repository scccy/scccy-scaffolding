package com.scccy.common.modules.domain.mp.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户信息表(SysUser)实体类
 *
 * @author scccy
 * @since 2025-10-22 16:26:59
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_user" )
@Schema(name = "SysUserMp" , description = "用户信息表" )
public class SysUserMp extends Model<SysUserMp> implements Serializable {
    @Serial
    private static final long serialVersionUID = -27821874157644536L;

    /**
     * 用户ID
     * 自动生成雪花ID
     */
    @Schema(description = "用户ID（自动生成雪花ID）" )
    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    private Long userId;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID" )
    @TableField("dept_id" )
    private Long deptId;

    /**
     * 用户账号
     */
    @Schema(description = "用户账号" )
    @TableField("user_name" )
    private String userName;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称" )
    @TableField("nick_name" )
    private String nickName;

    /**
     * 用户类型（00系统用户）
     */
    @Schema(description = "用户类型（00系统用户）" )
    @TableField("user_type" )
    private String userType;

    /**
     * 用户邮箱
     */
    @Schema(description = "用户邮箱" )
    @TableField("email" )
    private String email;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码" )
    @TableField("phonenumber" )
    private String phonenumber;

    /**
     * 用户性别（0男 1女 2未知）
     */
    @Schema(description = "用户性别（0男 1女 2未知）" )
    @TableField("sex" )
    private String sex;

    /**
     * 头像地址
     */
    @Schema(description = "头像地址" )
    @TableField("avatar" )
    private String avatar;

    /**
     * 密码
     */
    @Schema(description = "密码" )
    @TableField("password" )
    private String password;

    /**
     * 账号状态（0正常 1停用）
     */
    @Schema(description = "账号状态（0正常 1停用）" )
    @TableField("status" )
    private Integer status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @Schema(description = "删除标志（0代表存在 2代表删除）" )
    @TableField("del_flag" )
    private Integer delFlag;

    /**
     * 最后登录IP
     */
    @Schema(description = "最后登录IP" )
    @TableField("login_ip" )
    private String loginIp;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间" )
    @TableField("login_date" )
    private Date loginDate;

    /**
     * 密码最后更新时间
     */
    @Schema(description = "密码最后更新时间" )
    @TableField("pwd_update_date" )
    private Date pwdUpdateDate;

    /**
     * 创建者
     */
    @Schema(description = "创建者" )
    @TableField("create_by" )
    private String createBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间" )
    @TableField("create_time" )
    private Date createTime;

    /**
     * 更新者
     */
    @Schema(description = "更新者" )
    @TableField("update_by" )
    private String updateBy;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间" )
    @TableField("update_time" )
    private Date updateTime;

    /**
     * 备注
     */
    @Schema(description = "备注" )
    @TableField("remark" )
    private String remark;


}
