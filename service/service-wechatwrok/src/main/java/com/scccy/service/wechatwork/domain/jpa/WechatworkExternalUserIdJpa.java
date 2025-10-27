package com.scccy.service.wechatwork.domain.jpa;

import java.util.Date;

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
 * 好友关系 复合主键类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Embeddable
public class WechatworkExternalUserIdJpa implements Serializable {

    @Serial
    private static final long serialVersionUID = 403178720608676081L;

    /**
     * 企微id
     */
    @Size(max = 128)
    @NotNull
    @Column(name = "wechatwork_user_id", nullable = false, length = 128)
    private String wechatworkUserId;
    /**
     * 客户企微id
     */
    @Size(max = 128)
    @NotNull
    @Column(name = "wechatwork_external_userid", nullable = false, length = 128)
    private String wechatworkExternalUserid;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        WechatworkExternalUserIdJpa entity = (WechatworkExternalUserIdJpa) o;
        if (!Objects.equals(this.wechatworkUserId, entity.wechatworkUserId)) return false;
        return Objects.equals(this.wechatworkExternalUserid, entity.wechatworkExternalUserid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                wechatworkUserId,
                wechatworkExternalUserid
        );
    }
}
