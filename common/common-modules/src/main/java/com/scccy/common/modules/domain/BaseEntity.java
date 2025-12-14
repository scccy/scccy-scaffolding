package com.scccy.common.modules.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.scccy.common.modules.utils.EntityModelConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

/**
 * 基础实体类，包含公共字段
 * <p>
 * 继承 MyBatis Plus 的 Model，支持 ActiveRecord 模式
 * 同时提供公共字段（创建时间、更新时间、创建人、更新人等）
 * <p>
 * 使用方式：
 * 1. Service 层模式（推荐）：通过 Service 层操作，如 service.save(entity)
 * 2. ActiveRecord 模式（可选）：直接调用实体方法，如 entity.save()
 * <p>
 * 示例：
 * <pre>
 * public class User extends BaseEntity&lt;User, UserVo&gt; {
 *     // 继承 BaseEntity 后，可以使用：
 *     // user.save() - ActiveRecord 模式
 *     // 或 service.save(user) - Service 层模式
 * }
 * </pre>
 *
 * @param <T> 实体类型（自引用，用于 ActiveRecord 模式）
 * @param <V> VO 类型（用于 toVo 方法）
 * @author origin
 * @since 2025-07-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseEntity<T extends BaseEntity<T, V>, V> extends Model<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 创建人ID
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT_UPDATE)
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime createdTime;

    /**
     * 更新人ID
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;

    public V toVo(Class<V> clazz) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        return (V)(EntityModelConverter.getInstance().convert(this, clazz));
    }
} 