# Common Excel 模块

基于 FastExcel 的 Excel 导入导出工具模块，提供了便捷的 Excel 操作功能，支持自动从 `@Schema` 注解读取列名，避免重复定义。

## 功能特性

- ✅ **Excel 导出**：支持将数据列表导出为 Excel 文件
- ✅ **Excel 导入**：支持从 Excel 文件读取数据并批量处理
- ✅ **类级别注解**：支持在类上使用 `@ExcelSchemaProperty`，标识需要处理的类
- ✅ **兼容 FastExcel**：完全兼容 FastExcel 的 `@ExcelProperty` 注解

## ⚠️ 重要提示

**由于 Java 限制，无法在运行时动态添加注解。FastExcel 只识别 `@ExcelProperty` 注解。**

如果使用 `@ExcelSchemaProperty`，需要在字段上**同时添加 `@ExcelProperty` 注解**。

`@ExcelSchemaProperty` 主要用于：
1. **标记作用**：标识需要处理的字段

## 快速开始

### 1. 添加依赖

该模块已包含在项目中，无需额外配置。

### 2. 创建导出实体类

**重要：由于 FastExcel 只识别 `@ExcelProperty` 注解，需要在字段上同时添加 `@ExcelProperty`。**

```java
package com.scccy.example;

import cn.idev.excel.annotation.ExcelProperty;
import com.scccy.common.excel.annotation.ExcelSchemaProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@ExcelSchemaProperty  // 在类上使用，标识需要处理的类
public class UserExport {
    
    @Schema(description = "用户ID")
    @ExcelProperty("用户ID")  // 需要手动添加，值从 @Schema 复制
    private Long userId;

    @Schema(description = "用户名")
    @ExcelProperty("用户名")  // 需要手动添加，值从 @Schema 复制
    private String userName;

    @Schema(description = "用户年龄")
    @ExcelProperty("用户年龄")  // 需要手动添加，值从 @Schema 复制
    private Integer age;

    @Schema(description = "邮箱地址")
    @ExcelProperty("邮箱地址")  // 需要手动添加，值从 @Schema 复制
    private String email;
}
```


### 3. 导出 Excel

```java
package com.scccy.example.controller;

import com.scccy.common.excel.untils.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class UserController {

    @GetMapping("/export")
    public void exportUsers(HttpServletResponse response) throws IOException {
        // 获取数据
        List<UserExport> users = userService.getUsers();

        // 导出 Excel
        ExcelUtil.export(response, "用户列表", users, UserExport.class);
    }
}
```

### 4. 导入 Excel

```java
package com.scccy.example.controller;

import com.scccy.common.excel.untils.ExcelUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class UserController {

    @PostMapping("/import")
    public String importUsers(@RequestParam("file") MultipartFile file) {
        try {
            ExcelUtil.importData(file, UserExport.class, users -> {
                // 批量处理导入的数据
                userService.saveBatch(users);
            });
            return "导入成功";
        } catch (Exception e) {
            return "导入失败: " + e.getMessage();
        }
    }
}
```

## @ExcelSchemaProperty 注解

### 功能说明

`@ExcelSchemaProperty` 注解用于标识需要从 `@Schema` 注解读取 `description` 的字段。

**⚠️ 重要：由于 Java 限制，无法在运行时动态添加注解。FastExcel 只识别 `@ExcelProperty` 注解。**

因此，`@ExcelSchemaProperty` 主要用于：
1. **标记作用**：标识需要处理的字段
2. **代码生成建议**：通过工具类获取建议的 `@ExcelProperty` 代码
3. **编译时检查**：编译时注解处理器提供检查和提示

### 使用方式

#### 方式1：类级别使用（推荐）

在类上使用 `@ExcelSchemaProperty`，标识该类需要处理。字段上需要同时添加 `@ExcelProperty` 注解。

```java
import cn.idev.excel.annotation.ExcelProperty;
import com.scccy.common.excel.annotation.ExcelSchemaProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@ExcelSchemaProperty  // 标识需要处理的类
public class UserExport {
    @Schema(description = "用户ID")
    @ExcelProperty("用户ID")  // 需要手动添加，值从 @Schema 复制
    private Long userId;
    
    @Schema(description = "用户名")
    @ExcelProperty("用户名")  // 需要手动添加，值从 @Schema 复制
    private String userName;
}
```

#### 方式2：字段级别使用

在字段上使用 `@ExcelSchemaProperty`，标识该字段需要处理。

```java
@Data
public class UserExport {
    @Schema(description = "用户ID")
    @ExcelSchemaProperty  // 标识需要处理的字段
    @ExcelProperty("用户ID")  // 需要手动添加
    private Long userId;
}
```

### 注意事项

- **必须在字段上同时添加 `@ExcelProperty` 注解**，否则 FastExcel 无法识别
- 字段上必须有 `@Schema(description = "...")` 注解
- `@ExcelProperty` 的值应该与 `@Schema` 的 `description` 保持一致

## API 文档

### ExcelUtil.export()

导出 Excel 文件。

**方法签名：**

```java
public static <T> void export(
        HttpServletResponse response,
        String fileName,
        List<T> data,
        Class<T> clazz
) throws IOException
```

**参数说明：**

- `response`: HTTP 响应对象
- `fileName`: 导出的文件名（不带扩展名，会自动添加 .xlsx）
- `data`: 要导出的数据列表
- `clazz`: 导出实体类类型

**示例：**

```java
List<UserExport> users = Arrays.asList(
        new UserExport(1L, "张三", 25, "zhangsan@example.com"),
        new UserExport(2L, "李四", 30, "lisi@example.com")
);
ExcelUtil.

export(response, "用户列表",users, UserExport .class);
```

### ExcelUtil.importData()

导入 Excel 文件。

**方法签名：**

```java
public static <T> void importData(
        MultipartFile file,
        Class<T> clazz,
        Consumer<List<T>> batchConsumer
) throws Exception
```

**参数说明：**

- `file`: 上传的 Excel 文件
- `clazz`: 目标实体类
- `batchConsumer`: 批量处理函数（读取完数据后的处理逻辑，如保存到数据库）

**示例：**

```java
ExcelUtil.importData(file, UserExport .class, users ->{
        // 批量保存到数据库
        userService.

saveBatch(users);
    log.

info("成功导入 {} 条数据",users.size());
        });
```

## 完整示例

### 导出示例

```java
package com.scccy.example.controller;

import com.scccy.common.excel.untils.ExcelUtil;
import com.scccy.example.domain.UserExport;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class ExportController {

    @Autowired
    private UserService userService;

    /**
     * 导出用户列表
     */
    @GetMapping("/api/users/export")
    public void exportUsers(HttpServletResponse response) throws IOException {
        // 查询数据
        List<UserExport> users = userService.findAll();

        // 导出 Excel
        ExcelUtil.export(response, "用户列表_" + System.currentTimeMillis(), users, UserExport.class);
    }
}
```

### 导入示例

```java
package com.scccy.example.controller;

import com.scccy.common.excel.untils.ExcelUtil;
import com.scccy.example.domain.UserExport;
import com.scccy.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ImportController {

    @Autowired
    private UserService userService;

    /**
     * 导入用户数据
     */
    @PostMapping("/api/users/import")
    public String importUsers(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return "文件不能为空";
            }

            ExcelUtil.importData(file, UserExport.class, users -> {
                // 批量保存
                userService.saveBatch(users);
            });

            return "导入成功";
        } catch (Exception e) {
            return "导入失败: " + e.getMessage();
        }
    }
}
```

### 实体类示例

```java
package com.scccy.example.domain;

import com.scccy.common.excel.annotation.ExcelSchemaProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ExcelSchemaProperty  // 类级别使用，所有字段自动从 @Schema 读取列名
public class UserExport {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "用户年龄")
    private Integer age;

    @Schema(description = "邮箱地址")
    private String email;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
```

## 技术实现

- **基础框架**：FastExcel (cn.idev.excel)
- **注解处理**：通过反射读取 `@Schema` 和 `@ExcelSchemaProperty` 注解

## 注意事项

1. **文件格式**：支持 .xlsx 格式（Excel 2007+）
2. **编码**：导出文件名使用 UTF-8 编码
3. **性能**：导入时使用批量处理，建议在 `batchConsumer` 中批量保存数据以提高性能
4. **错误处理**：导入时如果数据格式不正确，会抛出异常，建议做好异常处理

## 版本信息

- 基于 FastExcel 1.3.0
- 支持 Java 21+
- 支持 Spring Boot 3.x

## 许可证

本项目遵循项目主许可证。
