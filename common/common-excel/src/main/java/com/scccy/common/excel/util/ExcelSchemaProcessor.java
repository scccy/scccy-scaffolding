package com.scccy.common.excel.util;

import cn.idev.excel.annotation.ExcelProperty;
import com.scccy.common.excel.annotation.ExcelSchemaProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.reflect.Field;

/**
 * Excel Schema 处理器
 * <p>
 * 用于处理 @ExcelSchemaProperty 注解，
 * 自动从 @Schema 注解读取 description 并应用到 Excel 导出
 * </p>
 *
 * @author scccy
 */
public class ExcelSchemaProcessor {

    /**
     * 获取字段的 Excel 列名
     * 优先从 @ExcelProperty 读取，如果没有则从 @Schema 读取
     *
     * @param field 字段
     * @param clazz 字段所属的类（用于检查类级别的 @ExcelSchemaProperty）
     * @return 列名，如果都没有则返回字段名
     */
    public static String getExcelColumnName(Field field, Class<?> clazz) {
        // 先检查是否有 @ExcelProperty
        ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
        if (excelProperty != null && excelProperty.value() != null && excelProperty.value().length > 0) {
            return excelProperty.value()[0];
        }

        // 检查字段上是否有 @ExcelSchemaProperty
        if (field.isAnnotationPresent(ExcelSchemaProperty.class)) {
            Schema schema = field.getAnnotation(Schema.class);
            if (schema != null && schema.description() != null && !schema.description().isEmpty()) {
                return schema.description();
            }
        }

        // 检查类上是否有 @ExcelSchemaProperty，如果有，则从字段的 @Schema 读取
        if (clazz.isAnnotationPresent(ExcelSchemaProperty.class)) {
            Schema schema = field.getAnnotation(Schema.class);
            if (schema != null && schema.description() != null && !schema.description().isEmpty()) {
                return schema.description();
            }
        }

        // 默认返回字段名
        return field.getName();
    }
}

