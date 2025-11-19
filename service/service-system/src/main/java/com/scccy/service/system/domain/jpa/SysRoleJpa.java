package com.scccy.service.system.domain.jpa;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 角色信息表
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "sys_role", schema = "scaffolding")
@Schema(name = "SysRoleJpa", description = "角色信息表")
public class SysRoleJpa implements Serializable {

    @Serial
    private static final long serialVersionUID = -51866802930150444L;

    @Id
    @Column(name = "role_id")
    @Schema(description = "角色ID")
    private Long roleId;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    @Column(name = "role_name")
    private String roleName;
    /**
     * 角色权限字符串
     */
    @Schema(description = "角色权限字符串")
    @Column(name = "role_key")
    private String roleKey;
    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序")
    @Column(name = "role_sort")
    private Integer roleSort;
    /**
     * 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）
     */
    @Schema(description = "数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）")
    @Column(name = "data_scope")
    private String dataScope;
    /**
     * 菜单树选择项是否关联显示
     */
    @Schema(description = "菜单树选择项是否关联显示")
    @Column(name = "menu_check_strictly")
    private Integer menuCheckStrictly;
    /**
     * 部门树选择项是否关联显示
     */
    @Schema(description = "部门树选择项是否关联显示")
    @Column(name = "dept_check_strictly")
    private Integer deptCheckStrictly;
    /**
     * 角色状态（0正常 1停用）
     */
    @Schema(description = "角色状态（0正常 1停用）")
    @Column(name = "status")
    private String status;
    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @Schema(description = "删除标志（0代表存在 2代表删除）")
    @Column(name = "del_flag")
    private String delFlag;
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
        SysRoleJpa that = (SysRoleJpa) o;
        return roleId != null && Objects.equals(roleId, that.roleId);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(roleId);
    }

}
