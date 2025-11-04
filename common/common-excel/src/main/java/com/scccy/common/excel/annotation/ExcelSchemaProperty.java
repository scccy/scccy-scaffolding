package com.scccy.common.excel.annotation;

import java.lang.annotation.*;

/**
 * Excel 属性注解，自动从 @Schema 读取 description
 * 此注解可以替代 @ExcelProperty，会自动从同字段上的 @Schema 注解读取 description
 * 作为 Excel 列名，避免重复定义。
 * 使用方式：

 *     方式1：在类上使用 @ExcelSchemaProperty，该类中所有带有 @Schema(description) 的字段
 *     会自动使用 description 作为 Excel 列名</li>
 *     方式2：在字段上使用 @ExcelSchemaProperty，该字段会自动从 @Schema 读取 description</li>

 * 使用示例（类级别）：
 * @ ExcelSchemaProperty
 * public class UserExport {
 *     Schema(description = "用户ID")
 *     private Long userId;
 *     Schema(description = "用户名")
 *     private String userName;
 * }
 * 使用示例（字段级别）：
 * public class UserExport {
 *     Schema(description = "用户名")
 *     @ ExcelSchemaProperty
 *     private String userName;
 * }

 *
 * @author scccy
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelSchemaProperty {
}

