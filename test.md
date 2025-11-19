用户发起 /api/user/login
DispatcherServlet 接到请求，交给 UserAuthController.login()。
AuthService 验证账号密码
AuthService.login() 调用 authenticate()，通过 Spring Security 校验用户名/密码。
调用 SystemUserClient.getByUserName() 远程查询用户信息；getUserAuthorities() 查询权限。
登录成功后准备生成 Token。
UserTokenGenerationService 生成用户 JWT
generateUserToken() 被调用，记录日志“生成用户 JWT Token”。
通过 SystemUserClient 再次获取用户信息、权限（与上一步存在重复，可后续优化）。
拿到 SysUserMp 和权限列表后，进入 generateJwtToken()。
获取内部服务客户端配置
findUserClient() 读取配置 scccy.internal-token.client-id（例如 internal-service-client），通过 RegisteredClientRepository 查询 oauth2_registered_client。
如果数据库存在该客户端，返回其配置（主要读取 TokenSettings 的过期时间等）；如果找不到，直接抛错拒绝登录；如果未配置，使用默认 2 小时 TTL。
生成 Token（优先 JwtEncoder）
构建 JWT Claims（issuer/subject/audience 等），把用户信息、权限写入 claims。
调用 jwtEncoder.encode() 生成 Token；如果 JwtEncoder Bean 不存在或执行失败，才会 fallback 到 JWK（JWKCacheManager）进行 RSA 手动签名。
生成的 Token 与过期时间写入 LoginResponse。
返回登录结果
AuthService.login() 日志显示“用户登录成功并生成 Token”；
控制器返回 200 OK，日志记录“用户登录成功”。