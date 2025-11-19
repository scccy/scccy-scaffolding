# Repository Guidelines

## Project Structure & Module Organization
本项目是多模块 Maven 脚手架：`gateway/` 负责统一路由与鉴权，`service/service-auth/` 与 `service/service-system/` 提供认证、系统能力，`service/service-demo/` 及外部集成模块示范第三方接入；`common/` 汇聚日志、缓存、Knife4j、实体等通用组件，`core/core-flink/` 聚焦实时作业示例，`docker/` 与 `nacos_config_export/` 管理基础设施与配置快照，`docs/`、`templates/` 存放架构说明与脚本，`logs/` 仅用于本地输出。各模块 `src/test/java` 目录放置对应测试，包结构需与 `src/main/java` 保持一致。

## Build, Test, and Development Commands
- `mvn clean install`：全量编译与单测，生成聚合依赖；初次构建可带 `-Dmaven.test.skip` 快速校验依赖。
- `mvn spring-boot:run -pl gateway -am`：联动依赖启动网关，调试路由、灰度与 Knife4j 文档聚合。
- `mvn spring-boot:run -pl service/service-auth -am` / `-pl service/service-system -am`：启动核心服务并观察 Nacos 注册、JWT 签发流程。
- `mvn -pl common/common-modules -am -DskipTests package`：单独打包共用模块，供外部服务或 Demo 引用。
- `docker compose up -d`（位于相应 `docker/*` 目录）：按需拉起 Nacos、Redis、Kafka、SkyWalking 等支撑服务，确保端口未冲突。

## Coding Style & Naming Conventions
Java 21 + Spring Boot 3.5，统一使用 4 空格缩进，包命名遵循 `com.scccy.<module>`；类以领域含义命名（如 `*Controller`、`*ServiceImpl`），枚举与常量大写下划线。REST 路由保持 kebab-case（示例：`/api/user/login`），DTO 字段小驼峰。使用 Lombok 时保持字段 `@Getter/@Setter` 明确，MyBatis-Plus Mapper 统一置于 `mapper` 包并以 `Mapper` 收尾。提交前运行 `mvn -pl <module> fmt:format`（如配置了 formatter）并遵循 Checkstyle/Spotless 约定，必要时结合 `qodana.yaml` 触发 IDE 级静态扫描。

## Testing Guidelines
首选 JUnit 5 + Spring Boot Test，测试类与被测类同名 `*Test`，放在匹配包路径下。新增业务需附核心单测，并为网关过滤器或安全链提供 `@SpringBootTest` + MockMvc 覆盖；涉及 Redis、MySQL 的集成场景可借助 Testcontainers 复现真实依赖。建议以 `mvn test` 或 `mvn -pl service/service-auth -am test` 运行模块级校验，保持关键模块覆盖率 ≥80%，同时验证 MyBatis-Plus Mapper 的 SQL 片段并将关键断言写入 PR 描述。

## Commit & Pull Request Guidelines
沿用 Conventional Commits：`type(scope): 摘要`，如 `feat(common-base): 新增内部令牌管理功能`；多作用域建议拆分多条提交，若存在破坏性改动请追加 `BREAKING CHANGE:` 说明。PR 描述需包含：变更背景、主要改动点、测试方式（命令输出摘要）、关联 Issue/需求编号与回滚策略。若涉及界面或文档更新，请附关键截图或链接，并同步更新 `docs/` 与相关 Nacos 配置，便于评审聚焦。

## Security & Configuration Tips
敏感配置只存放在 `nacos_config_export/DEFAULT_GROUP/*.yaml` 或私有环境变量，不要写入 Git；提交前检查 `application-*.yml` 与 `docker/*.env` 中的密钥。联调局域网时可在 `common` 模块开启 `scccy.security.permit-all=true`，但务必在 PR 描述中说明并在上线前关闭，避免调试开关带入生产。Docker 目录内的 compose 文件默认暴露 8848/6379/9090 端口，可通过 `.env` 覆盖；如需远程排障，请在网关层配置限流、IP Allowlist，并记录操作时间窗口。
