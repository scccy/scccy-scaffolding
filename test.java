package com.scccy.example;

import com.scccy.common.excel.annotation.ExcelSchemaProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试类 - 演示如何使用 @ExcelSchemaProperty 自动从 @Schema 读取 description
 * <p>
 * 使用方式：
 * 1. 在类上使用 @ExcelSchemaProperty（推荐），该类中所有带有 @Schema(description) 的字段
 *    会自动使用 description 作为 Excel 列名
 * 2. 或者在字段上使用 @ExcelSchemaProperty
 * </p>
 * <p>
 * 导出示例：
 * <pre>
 * List&lt;Test&gt; data = new ArrayList&lt;&gt;();
 * // ... 添加数据
 * ExcelUtil.export(response, "测试数据", data, Test.class);
 * </pre>
 * </p>
 */
@Data
@ExcelSchemaProperty
public class Test {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "用户年龄")
    private Integer age;

    @Schema(description = "邮箱地址")
    private String email;
}

