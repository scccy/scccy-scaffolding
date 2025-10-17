# Common Log 模块

## 概述

本模块提供统一的日志和链路追踪功能，使用 **Micrometer Tracing** 替代已废弃的 Spring Cloud Sleuth。

## 主要功能

- **日志管理**: 基于 Log4j2 的高性能日志系统
- **链路追踪**: 使用 Micrometer Tracing 进行分布式链路追踪（自动配置）
- **异步日志**: 使用 Disruptor 提供高性能异步日志队列
- **Zipkin 集成**: 支持将追踪数据发送到 Zipkin 服务器（自动配置）

## 依赖说明

### 核心依赖

- `spring-boot-starter-log4j2`: Log4j2 日志框架
- `micrometer-tracing`: Micrometer 链路追踪核心
- `micrometer-tracing-bridge-brave`: Brave 追踪桥接器
- `zipkin-reporter-brave`: Zipkin 数据上报
- `disruptor`: 高性能异步队列

### 版本兼容性

- Spring Boot: 3.2.5+
- Java: 21+
- 不再支持 Spring Cloud Sleuth

## 配置说明

### 1. 自动配置（推荐）

**无需任何配置文件！** 直接引用 `common-log` 模块即可自动配置 Micrometer Tracing。

默认配置：
- 采样率：10%
- Zipkin 端点：http://117.50.197.170:9411/api/v2/spans
- 自动启用链路追踪

### 自动配置原理

`common-log` 模块通过以下方式实现自动配置：

1. **依赖管理**：包含所有必要的 Micrometer Tracing 依赖
2. **自动配置类**：`TracingAutoConfiguration` 自动设置系统属性
3. **Spring Boot 集成**：利用 Spring Boot 的 Micrometer Tracing 自动配置机制
4. **日志集成**：Log4j2 配置自动包含 traceId 和 spanId

### 2. 日志格式配置

```yaml
logging:
  pattern:
    level: "%X{spring.application.name:-},%X{traceId:-},%X{spanId:-} %5p"
```

### 3. 完整配置示例

参考 `src/main/resources/application-tracing.yml`

## 使用方式

### 1. 在业务模块中引用

```xml
<dependency>
    <groupId>com.scccy</groupId>
    <artifactId>common-log</artifactId>
</dependency>
```

### 2. 自定义配置（可选）

如果需要自定义配置，可以通过以下方式覆盖默认值：

**方式一：系统属性**

```bash
java -Dmanagement.tracing.sampling.probability=0.05 \
     -Dmanagement.zipkin.tracing.endpoint=http://your-zipkin:9411/api/v2/spans \
     -jar your-app.jar
```

**方式二：环境变量**

```bash
export MANAGEMENT_TRACING_SAMPLING_PROBABILITY=0.05
export MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://your-zipkin:9411/api/v2/spans
```

**方式三：配置文件**

在微服务的 `application.yml` 中：

```yaml
management:
  tracing:
    sampling:
      probability: 0.05  # 自定义采样率
  zipkin:
    tracing:
      endpoint: http://your-zipkin:9411/api/v2/spans  # 自定义Zipkin地址

spring:
  application:
    name: your-service-name
```

### 3. 日志使用

```java
@Slf4j
@RestController
public class TestController {
    
    @GetMapping("/test")
    public String test() {
        log.info("处理请求"); // 自动包含 traceId 和 spanId
        return "success";
    }
}
```

## 迁移指南

### 从 Spring Cloud Sleuth 迁移

1. **移除旧依赖**:
   ```xml
   <!-- 移除 -->
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-sleuth</artifactId>
   </dependency>
   ```

2. **更新配置**:
   ```yaml
   # 旧配置
   spring:
     sleuth:
       sampler:
         probability: 0.1
     zipkin:
       base-url: http://localhost:9411
   
   # 新配置
   management:
     tracing:
       sampling:
         probability: 0.1
     zipkin:
       tracing:
         endpoint: http://localhost:9411/api/v2/spans
   ```

3. **更新代码**:
   ```java
   // 旧方式
   @Autowired
   private Tracer tracer;
   
   // 新方式 - 使用 Micrometer Tracing
   @Autowired
   private Tracer tracer; // 接口相同，但实现不同
   ```

## 注意事项

1. **采样率设置**: 生产环境建议设置较低的采样率 (0.01-0.1)
2. **Zipkin 服务**: 确保 Zipkin 服务器正常运行
3. **性能影响**: 链路追踪会有轻微的性能开销
4. **日志格式**: 确保日志格式包含 traceId 和 spanId 用于问题排查

## 故障排查

### 常见问题

1. **追踪数据未发送到 Zipkin**
   - 检查 Zipkin 服务是否启动
   - 验证 endpoint 配置是否正确
   - 查看网络连接是否正常

2. **日志中缺少 traceId**
   - 检查日志格式配置
   - 确认 tracing 功能已启用

3. **性能问题**
   - 降低采样率
   - 检查 Disruptor 配置
   - 监控内存使用情况
