# 用户注册实时模拟程序

这是一个简化的Flink实时流处理程序，用于从Kafka消费用户注册数据，按10秒窗口聚合统计注册人数。

## 功能特性

- 从Kafka (117.50.197.170:9094) 消费JSON格式的用户注册数据
- 按10秒滚动窗口聚合统计注册人数
- 直接打印统计结果到控制台
- 包含数据生成器用于测试

## 项目结构

```
src/main/java/com/origin/banyu/coreflink/job/user/
├── SimpleRegistrationSimulator.java    # 主程序：实时聚合统计
├── RegistrationDataGenerator.java      # 数据生成器：发送测试数据
├── UserRegistrationAnalysisJobMain.java # 原始复杂版本
└── UserRegistrationAnalysisJobSubmitMain.java # 原始复杂版本
```

## 数据格式

### 输入数据 (JSON)
```json
{
  "userId": "user_1703123456789_123",
  "registerTime": "2023-12-21T10:30:45",
  "nickname": "张三",
  "gender": "MALE",
  "timestamp": 1703123456789,
  "source": "simulator"
}
```

### 输出结果
```
=== 10秒窗口注册统计 ===
窗口时间: 1703123450000 - 1703123460000
注册人数: 3
详细信息:
  - 张三 (注册时间: 2023-12-21 10:30:45)
  - 李四 (注册时间: 2023-12-21 10:30:47)
  - 王五 (注册时间: 2023-12-21 10:30:52)
=======================
```

## 使用方法

### 1. 编译项目
```bash
mvn clean package
```

### 2. 运行数据生成器（可选）
如果需要测试数据，先运行数据生成器：
```bash
java -cp target/core-flink-job-user-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
     com.origin.banyu.coreflink.job.user.RegistrationDataGenerator
```

### 3. 运行实时模拟程序
```bash
java -cp target/core-flink-job-user-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
     com.origin.banyu.coreflink.job.user.SimpleRegistrationSimulator
```

或者直接运行jar包：
```bash
java -jar target/core-flink-job-user-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

## 配置说明

### Kafka配置
- **Bootstrap Servers**: 117.50.197.170:9094
- **Topic**: user-registration
- **Group ID**: registration-simulator
- **起始偏移量**: latest（从最新消息开始消费）

### Flink配置
- **并行度**: 1
- **窗口类型**: 滚动窗口 (TumblingProcessingTimeWindows)
- **窗口大小**: 10秒
- **水印策略**: 5秒延迟容忍

## 技术栈

- **Apache Flink**: 2.0.0
- **Apache Kafka**: 3.6.0
- **FastJSON2**: JSON序列化/反序列化
- **Java**: 21

## 注意事项

1. 确保Kafka服务正在运行且可访问
2. 确保Topic `user-registration` 已创建
3. 程序会从最新消息开始消费，如需处理历史数据，请修改代码中的 `OffsetsInitializer.latest()` 为 `OffsetsInitializer.earliest()`
4. 数据生成器会持续发送数据，按Ctrl+C停止

## 与原始版本的区别

原始版本 (`UserRegistrationAnalysisJobMain`) 包含：
- 复杂的配置加载
- Redis写入功能
- 1分钟窗口聚合
- 性别统计

简化版本 (`SimpleRegistrationSimulator`) 特点：
- 硬编码配置，简单直接
- 直接打印结果，无外部依赖
- 10秒窗口聚合
- 专注核心功能：注册人数统计
