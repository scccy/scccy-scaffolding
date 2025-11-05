package com.scccy.common.excel.annotation;

import java.lang.annotation.*;

/**
 * Excel 属性注解，用于标识需要从 @Schema 读取 description 的字段
 * <p>
 * <strong>重要提示：</strong>
 * 由于 Java 限制，无法在运行时动态添加注解。FastExcel 只识别 @ExcelProperty 注解。
 * 如果使用 @ExcelSchemaProperty，需要在字段上同时添加 @ExcelProperty 注解。
 * </p>
 * <p>
 * 使用方式：
 * <ul>
 *     <li>方式1：在类上使用 @ExcelSchemaProperty，该类中所有带有 @Schema(description) 的字段
 *     需要在字段上同时添加 @ExcelProperty(description) 注解</li>
 *     <li>方式2：在字段上使用 @ExcelSchemaProperty，该字段需要同时添加 @ExcelProperty 注解</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例（推荐）：
 * <pre>
 * &#64;ExcelSchemaProperty
 * public class UserExport {
 *     &#64;Schema(description = "用户ID")
 *     &#64;ExcelProperty("用户ID")  // 需要手动添加
 *     private Long userId;
 *`     `
 *     &#64;Schema(description = "用户名")
 *     &#64;ExcelProperty("用户名")  // 需要手动添加
 *     private String userName;
 * }
 * </pre>
 * </p>
 *
 * @author scccy
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelSchemaProperty {
}

