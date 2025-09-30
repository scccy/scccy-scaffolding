# Redis 外网访问配置指南

## 配置说明

为了支持Redis外网访问和密码认证，已对`redis.config`文件进行了以下修改：

### 1. 网络绑定配置
```bash
# 注释掉本地绑定，允许所有网络接口访问
# bind 127.0.0.1
```

### 2. 保护模式配置
```bash
# 关闭保护模式，允许外网连接
protected-mode no
```

### 3. 密码认证配置
```bash
# 设置Redis访问密码
requirepass your_redis_password_here
```

## 安全建议

### 1. 设置强密码
请将`your_redis_password_here`替换为强密码，建议：
- 至少16位字符
- 包含大小写字母、数字和特殊字符
- 避免使用常见密码

### 2. 防火墙配置
确保服务器防火墙允许Redis端口（默认6379）的访问：
```bash
# 开放Redis端口
sudo ufw allow 6379
# 或者只允许特定IP访问
sudo ufw allow from 192.168.1.0/24 to any port 6379
```

### 3. 网络安全
- 考虑使用VPN或内网访问
- 定期更换密码
- 监控访问日志

## 客户端连接示例

### 1. Redis CLI连接
```bash
# 使用密码连接
redis-cli -h your_server_ip -p 6379 -a your_redis_password_here

# 或者先连接再认证
redis-cli -h your_server_ip -p 6379
AUTH your_redis_password_here
```

### 2. Java客户端连接
```java
// 使用Jedis
Jedis jedis = new Jedis("your_server_ip", 6379);
jedis.auth("your_redis_password_here");

// 使用Lettuce
RedisClient client = RedisClient.create("redis://:your_redis_password_here@your_server_ip:6379");
```

### 3. Spring Boot配置
```yaml
spring:
  redis:
    host: your_server_ip
    port: 6379
    password: your_redis_password_here
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

## 重启Redis服务

配置修改后需要重启Redis服务：

```bash
# 使用systemctl重启
sudo systemctl restart redis

# 或者使用service命令
sudo service redis restart

# 检查服务状态
sudo systemctl status redis
```

## 验证配置

### 1. 检查Redis是否监听所有接口
```bash
netstat -tlnp | grep 6379
# 应该显示 0.0.0.0:6379 而不是 127.0.0.1:6379
```

### 2. 测试外网连接
```bash
# 从其他机器测试连接
telnet your_server_ip 6379
```

### 3. 测试密码认证
```bash
redis-cli -h your_server_ip -p 6379
# 尝试执行命令，应该提示需要认证
# 输入 AUTH your_redis_password_here
# 再次执行命令应该成功
```

## 注意事项

1. **安全风险**：开放外网访问会增加安全风险，请确保：
   - 使用强密码
   - 配置防火墙
   - 定期更新密码
   - 监控访问日志

2. **性能考虑**：外网访问可能影响性能，建议：
   - 使用连接池
   - 配置合适的超时时间
   - 监控连接数

3. **备份策略**：确保Redis数据定期备份

## 故障排除

### 1. 连接被拒绝
- 检查防火墙设置
- 确认Redis服务正在运行
- 验证端口配置

### 2. 认证失败
- 检查密码是否正确
- 确认requirepass配置已生效
- 重启Redis服务

### 3. 性能问题
- 检查网络延迟
- 优化连接池配置
- 监控Redis性能指标
