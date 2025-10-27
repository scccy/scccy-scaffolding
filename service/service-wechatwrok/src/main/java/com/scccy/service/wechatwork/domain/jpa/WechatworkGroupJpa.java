package com.scccy.service.wechatwork.domain.jpa;

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
 * 企微用户群关联表
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "wechatwork_group", schema = "scaffolding")
@Schema(name = "WechatworkGroupJpa", description = "企微用户群关联表")
public class WechatworkGroupJpa implements Serializable {

    @Serial
    private static final long serialVersionUID = -70472021767107828L;

    /**
     * 复合主键
     */
    @EmbeddedId
    @Schema(description = "复合主键")
    private WechatworkGroupIdJpa id;

    /**
     * 企微id
     */
    @Schema(description = "企微id")
    @Column(name = "wechatwork_user_id")
    private String wechatworkUserId;
    /**
     * 客户标识
     */
    @Schema(description = "客户标识")
    @Column(name = "wechatwork_external_union_id")
    private String wechatworkExternalUnionId;
    /**
     * 状态 1 启用  0 禁用
     */
    @Schema(description = "状态 1 启用  0 禁用")
    @Column(name = "status")
    private Integer status;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @Column(name = "create_time")
    private Date createTime;
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @Column(name = "update_time")
    private Date updateTime;
    /**
     * 创建人
     */
    @Schema(description = "创建人")
    @Column(name = "create_by")
    private String createBy;
    /**
     * 更新人
     */
    @Schema(description = "更新人")
    @Column(name = "update_by")
    private String updateBy;
    /**
     * 1 未删除 0 已删除
     */
    @Schema(description = "1 未删除 0 已删除")
    @Column(name = "del_flag")
    private Integer delFlag;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        WechatworkGroupJpa that = (WechatworkGroupJpa) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

}
