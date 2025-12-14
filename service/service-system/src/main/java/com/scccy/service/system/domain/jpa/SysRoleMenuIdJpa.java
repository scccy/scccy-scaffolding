package com.scccy.service.system.domain.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * 角色和菜单关联表 复合主键类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Embeddable
public class SysRoleMenuIdJpa implements Serializable {

    @Serial
    private static final long serialVersionUID = -70877601997843026L;

    /**
     * 角色ID
     */
    @Size(max = 128)
    @NotNull
    @Column(name = "role_id", nullable = false, length = 128)
    private Long roleId;
    /**
     * 菜单ID
     */
    @Size(max = 128)
    @NotNull
    @Column(name = "menu_id", nullable = false, length = 128)
    private Long menuId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SysRoleMenuIdJpa entity = (SysRoleMenuIdJpa) o;
        if (!Objects.equals(this.roleId, entity.roleId)) return false;
        return Objects.equals(this.menuId, entity.menuId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                roleId,
                menuId
        );
    }
}
