package com.scccy.service.jackyun.domain.jpa;

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
 * 企微用户群关联表 复合主键类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Embeddable
public class WechatworkGroupIdJpa implements Serializable {

    @Serial
    private static final long serialVersionUID = 101872304007410684L;

    /**
     * 群id
     */
    @Size(max = 128)
    @NotNull
    @Column(name = "wechatwork_group_id", nullable = false, length = 128)
    private String wechatworkGroupId;
    /**
     * 客户企微id
     */
    @Size(max = 128)
    @NotNull
    @Column(name = "wechatwork_external_user_id", nullable = false, length = 128)
    private String wechatworkExternalUserId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        WechatworkGroupIdJpa entity = (WechatworkGroupIdJpa) o;
        if (!Objects.equals(this.wechatworkGroupId, entity.wechatworkGroupId)) return false;
        return Objects.equals(this.wechatworkExternalUserId, entity.wechatworkExternalUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                wechatworkGroupId,
                wechatworkExternalUserId
        );
    }
}
