package com.scccy.service.jackyun.domain.jpa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 用户信息表
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "sys_user", schema = "scaffolding")
@Schema(name = "SysUserJpa", description = "用户信息表")
public class SysUserJpa implements Serializable {

    @Serial
    private static final long serialVersionUID = -68243693391948069L;

    @Id
    @Column(name = "user_id")
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    @Column(name = "dept_id")
    private Long deptId;
    /**
     * 用户账号
     */
    @Schema(description = "用户账号")
    @Column(name = "user_name")
    private String userName;
    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    @Column(name = "nick_name")
    private String nickName;
    /**
     * 用户类型（00系统用户）
     */
    @Schema(description = "用户类型（00系统用户）")
    @Column(name = "user_type")
    private String userType;
    /**
     * 用户邮箱
     */
    @Schema(description = "用户邮箱")
    @Column(name = "email")
    private String email;
    /**
     * 手机号码
     */
    @Schema(description = "手机号码")
    @Column(name = "phonenumber")
    private String phonenumber;
    /**
     * 用户性别（0男 1女 2未知）
     */
    @Schema(description = "用户性别（0男 1女 2未知）")
    @Column(name = "sex")
    private String sex;
    /**
     * 头像地址
     */
    @Schema(description = "头像地址")
    @Column(name = "avatar")
    private String avatar;
    /**
     * 密码
     */
    @Schema(description = "密码")
    @Column(name = "password")
    private String password;
    /**
     * 账号状态（0正常 1停用）
     */
    @Schema(description = "账号状态（0正常 1停用）")
    @Column(name = "status")
    private Integer status;
    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @Schema(description = "删除标志（0代表存在 2代表删除）")
    @Column(name = "del_flag")
    private Integer delFlag;
    /**
     * 最后登录IP
     */
    @Schema(description = "最后登录IP")
    @Column(name = "login_ip")
    private String loginIp;
    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间")
    @Column(name = "login_date")
    private Date loginDate;
    /**
     * 密码最后更新时间
     */
    @Schema(description = "密码最后更新时间")
    @Column(name = "pwd_update_date")
    private Date pwdUpdateDate;
    /**
     * 创建者
     */
    @Schema(description = "创建者")
    @Column(name = "create_by")
    private String createBy;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @Column(name = "create_time")
    private Date createTime;
    /**
     * 更新者
     */
    @Schema(description = "更新者")
    @Column(name = "update_by")
    private String updateBy;
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @Column(name = "update_time")
    private Date updateTime;
    /**
     * 备注
     */
    @Schema(description = "备注")
    @Column(name = "remark")
    private String remark;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        SysUserJpa that = (SysUserJpa) o;
        return userId != null && Objects.equals(userId, that.userId);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(userId);
    }

}
