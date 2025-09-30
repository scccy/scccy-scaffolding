package com.origin.banyu.coreflink.job.user;


import com.origin.banyu.coreflink.common.GenericJsonDeserializer;
import com.origin.banyu.coreflink.common.entity.UserRegistrationEvent;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * 简化的用户注册实时模拟程序 - Flink 2.1.0 版本
 * 从Kafka消费JSON数据，按10秒窗口聚合统计注册人数，直接打印结果
 */
public class SimpleRegistrationSimulator {

    public static void main(String[] args) throws Exception {
        // Kafka配置
//        String bootstrapServers = "117.50.197.170:9094";
//        String bootstrapServers = "localhost:9094";
        String bootstrapServers = "broker:9092";
        String topic = "user-registration";
        String groupId = "registration-simulator";

        // 创建Flink环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // 设置并行度
        env.setParallelism(1);

        // 创建Kafka源
        KafkaSource<UserRegistrationEvent> source = KafkaSource.<UserRegistrationEvent>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(topic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(GenericJsonDeserializer.of(UserRegistrationEvent.class))
                .build();

        // 从Kafka读取数据流
        DataStream<UserRegistrationEvent> eventStream = env.fromSource(
                source,
                WatermarkStrategy
                        .<UserRegistrationEvent>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                        .withTimestampAssigner((event, ts) -> {
                            // 确保时间戳不为 null，使用当前时间作为默认值
                            if (event.getTimestamp() > 0) {
                                return event.getTimestamp();
                            } else {
                                return System.currentTimeMillis();
                            }
                        }),
                "User Registration Source"
        );
        eventStream.print("开始测试");
        // 按10秒窗口聚合统计注册人数 - Flink 2.0.0 版本
        eventStream
                .keyBy(event -> "global") // 全局聚合
                .window(TumblingProcessingTimeWindows.of(Duration.ofSeconds(10)))
                .process(new ProcessWindowFunction<UserRegistrationEvent, String, String, TimeWindow>() {
                    @Override
                    public void process(String key, 
                                     ProcessWindowFunction<UserRegistrationEvent, String, String, TimeWindow>.Context context,
                                     Iterable<UserRegistrationEvent> events, 
                                     Collector<String> out) throws Exception {
                        
                        int count = 0;
                        StringBuilder details = new StringBuilder();
                        
                        for (UserRegistrationEvent event : events) {
                            if (event != null) {
                                count++;
                                String nickname = event.getNickname() != null ? event.getNickname() : "未知用户";
                                String registerTimeStr = event.getRegisterTime() != null ? event.getRegisterTime() : "未知时间";
                                details.append(String.format("  - %s (注册时间: %s)\n", nickname, registerTimeStr));
                            }
                        }
                        
                        String result = String.format(
                            "\n=== 10秒窗口注册统计 ===\n" +
                            "窗口时间: %s - %s\n" +
                            "注册人数: %d\n" +
                            "详细信息:\n%s" +
                            "=======================\n",
                            context.window().getStart(),
                            context.window().getEnd(),
                            count,
                            details
                        );
                        
                        out.collect(result);
                    }
                })
                .print("注册统计");

        // 执行作业
        env.execute("Simple Registration Simulator");
    }
}