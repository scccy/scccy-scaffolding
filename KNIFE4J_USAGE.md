# Knife4j 使用指南

## 概述

本项目已成功集成 Knife4j 4.4.0，用于生成和展示 API 文档。Knife4j 是基于 OpenAPI 3 规范的增强版 Swagger UI。

## 配置信息

### 依赖配置
- **版本**: 4.4.0
- **依赖**: `knife4j-openapi3-jakarta-spring-boot-starter`
- **兼容性**: Spring Boot 3.2.5 + Jakarta EE

### 配置文件位置
- **配置类**: `service/service-base/src/main/java/com/scccy/service/base/config/Knife4jConfig.java`
- **自动配置**: 已添加到 `org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 使用方法

### 1. 在 Controller 中使用注解

```java
@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Operation(summary = "获取用户列表", description = "分页获取用户列表")
    @GetMapping("/list")
    public ResultData<Page<User>> getUserList(
            @Parameter(description = "页码", example = "1") 
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小", example = "10") 
            @RequestParam(defaultValue = "10") int size) {
        // 业务逻辑
        return ResultData.success(userService.getUserList(page, size));
    }

    @Operation(summary = "创建用户", description = "创建新用户")
    @PostMapping("/create")
    public ResultData<User> createUser(@RequestBody @Valid CreateUserRequest request) {
        // 业务逻辑
        return ResultData.success(userService.createUser(request));
    }
}
```

### 2. 常用注解说明

- `@Tag`: 用于标记 Controller 类，定义 API 分组
- `@Operation`: 用于标记方法，定义接口信息
- `@Parameter`: 用于标记参数，定义参数说明
- `@Schema`: 用于标记实体类字段，定义字段说明

### 3. 访问文档

启动应用后，可以通过以下地址访问 API 文档：

- **Knife4j UI**: `http://localhost:8080/doc.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **OpenAPI YAML**: `http://localhost:8080/v3/api-docs.yaml`

## 示例代码

项目中已包含示例 Controller：
- **文件**: `service/service-base/src/main/java/com/scccy/scaffolding/base/controller/ApiDocumentationController.java`
- **功能**: 演示 Knife4j 注解的基本用法

## 注意事项

1. **Spring Boot 3.x 兼容性**: 使用 Jakarta EE 规范，确保所有注解和依赖都是 Jakarta 版本
2. **自动配置**: 配置类已自动注册，无需手动配置
3. **版本管理**: 版本号在根 POM 的 `dependencyManagement` 中统一管理
4. **安全考虑**: 生产环境建议配置访问权限或禁用文档接口

## 扩展功能

### 自定义配置
可以在 `Knife4jConfig` 中添加更多配置：

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
            .info(new Info()
                    .title("API文档标题")
                    .description("API文档描述")
                    .version("1.0.0"))
            .addServersItem(new Server().url("http://localhost:8080").description("开发环境"))
            .addServersItem(new Server().url("https://api.example.com").description("生产环境"));
}
```

### 安全配置
可以添加 JWT 认证支持：

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
            .info(new Info().title("API文档").version("1.0.0"))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                    .addSecuritySchemes("Bearer Authentication", 
                            new SecurityScheme()
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("bearer")
                                    .bearerFormat("JWT")));
}
```
