package com.origin.banyu.coreflink.job.user;

import com.alibaba.fastjson2.JSON;
import com.origin.banyu.coreflink.common.entity.UserRegistrationEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Random;

/**
 * 用户注册数据生成器
 * 向Kafka发送模拟的用户注册数据
 */
public class RegistrationDataGenerator {

    private static final String[] NICKNAMES = {
        "张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十",
        "Alice", "Bob", "Charlie", "David", "Eva", "Frank", "Grace", "Henry",
        "小明", "小红", "小刚", "小丽", "阿强", "阿花", "老李", "老王"
    };

    private static final String[] GENDERS = {"MALE", "FEMALE", "UNKNOWN"};

    public static void main(String[] args) throws Exception {
        // Kafka配置
//        String bootstrapServers = "localhost:9094";
//        String bootstrapServers = "broker:9092";
        String bootstrapServers = "117.50.197.170:9094";
        String topic = "user-registration";

        // 创建Kafka生产者配置
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);

        // 创建生产者

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            Random random = new Random();
            System.out.println("开始生成用户注册数据...");
            System.out.println("Kafka地址: " + bootstrapServers);
            System.out.println("Topic: " + topic);
            System.out.println("按Ctrl+C停止生成\n");
            int messageCount = 0;
            while (true) {
                // 生成随机用户注册事件
                UserRegistrationEvent event = new UserRegistrationEvent();
                event.setUserId("user_" + System.currentTimeMillis() + "_" + random.nextInt(1000));
                event.setRegisterTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                event.setNickname(NICKNAMES[random.nextInt(NICKNAMES.length)]);
                event.setGender(GENDERS[random.nextInt(GENDERS.length)]);
                event.setTimestamp(System.currentTimeMillis());
                event.setSource("simulator");

                // 转换为JSON
                String jsonData = JSON.toJSONString(event);

                // 发送到Kafka
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, event.getUserId(), jsonData);
                producer.send(record);

                messageCount++;
                System.out.printf("发送第%d条数据: %s (昵称: %s, 性别: %s)\n",
                        messageCount, event.getUserId(), event.getNickname(), event.getGender());

                // 随机间隔1-3秒发送一条数据
                Thread.sleep(1000+random.nextInt(2000));
            }
        } catch (InterruptedException e) {
            System.out.println("\n数据生成器已停止");
        }
    }
}
