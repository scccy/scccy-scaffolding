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
 * 角色和部门关联表 复合主键类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Embeddable
public class SysRoleDeptIdJpa implements Serializable {

    @Serial
    private static final long serialVersionUID = -55897046409645246L;

    /**
     * 角色ID
     */
    @Size(max = 128)
    @NotNull
    @Column(name = "role_id", nullable = false, length = 128)
    private Long roleId;
    /**
     * 部门ID
     */
    @Size(max = 128)
    @NotNull
    @Column(name = "dept_id", nullable = false, length = 128)
    private Long deptId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SysRoleDeptIdJpa entity = (SysRoleDeptIdJpa) o;
        if (!Objects.equals(this.roleId, entity.roleId)) return false;
        return Objects.equals(this.deptId, entity.deptId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                roleId,
                deptId
        );
    }
}
