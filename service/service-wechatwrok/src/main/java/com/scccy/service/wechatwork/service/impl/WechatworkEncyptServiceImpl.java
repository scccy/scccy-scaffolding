package com.scccy.service.wechatwork.service.impl;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.scccy.service.wechatwork.config.WechatworkProperties;
import com.scccy.service.wechatwork.service.WechatworkEncyptService;
import com.scccy.service.wechatwork.service.WechatworkService;
import com.tencent.wework.Finance;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class WechatworkEncyptServiceImpl implements WechatworkEncyptService {

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Resource
    WechatworkService wechatworkServiceImpl;

    private static final String header = "hasGroupChat:";
    private static JSONObject returnJsonDetail = new JSONObject();
    private static JSONObject returnJson = new JSONObject();
    private static final Pattern TIME_RANGE_PATTERN = Pattern.compile("(\\d{2}:\\d{2})-(\\d{2}:\\d{2})");
    private static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final WechatworkProperties wechatworkProperties;



    @Override
    public Boolean getHasRoomChat(String unionId) {
        Object value = redisTemplate.opsForValue().get(header + unionId);

        if (value == null) {
            return false; // key 不存在
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            return false;
        }
    }

    @Override
    public void getAllData()  {

        String priKey =
                loadPrivateKeyPEM(wechatworkProperties.getPriKey());
        // 初始化参数
        int type = 1;
        Integer seq = Optional.ofNullable((Integer) redisTemplate.opsForValue().get("wechatwork:seq"))
                .orElse(0);

        int limit = 200;
        String proxy = "";
        String passwd = "";
        int timeout = 10;
        try {
            while (true) {
                // 构造参数数组
                String[] array = {
                        String.valueOf(type),
                        String.valueOf(seq),
                        String.valueOf(limit),
                        proxy,
                        passwd,
                        String.valueOf(timeout)
                };
//                System.out.println(Arrays.toString(array));

                // 调用接口
                get(array);
                JSONArray chatdata = returnJson.getJSONArray("chatdata");

                if (chatdata == null || chatdata.isEmpty()) {
                    break;
                }

                // 收集结果
                for (int i = 0; i < chatdata.size(); i++) {
                    String encryptRandomKey = chatdata.getJSONObject(i).getString("encrypt_random_key");
                    String encryptChatMsg = chatdata.getJSONObject(i).getString("encrypt_chat_msg");

                    encryptRandomKey = decryptRandomKey(encryptRandomKey, priKey);
                    // todo: 需要异步解密
                    get(new String[]{"3", encryptRandomKey, encryptChatMsg});
                    Long msgtime = returnJsonDetail.getLong("msgtime");
                    if (returnJsonDetail.getString("roomid") != null)
//                是群聊
                    {
                        if (returnJsonDetail != null
                                && returnJsonDetail.getJSONObject("text") != null
                                && returnJsonDetail.getJSONObject("text").getString("content") != null
                        ) {
                            String roomid = returnJsonDetail.getString("roomid");
                            String text = returnJsonDetail.getJSONObject("text").getString("content");

                            // 内容为空直接看下一条，避免NPE
                            if (text == null) {
                                seq += 1;
                                redisTemplate.opsForValue().set("wechatwork:seq", seq);
                                continue;
                            }
                            // 情况1: 运营发布活动时间
                            // 仅当匹配到时间范围时才计算开始/结束时间
                            Matcher matcher = TIME_RANGE_PATTERN.matcher(text);
                            if (matcher != null && matcher.find()) {
                                // 检查消息时间是否为今天，避免处理历史消息
                                if (msgtime == null) {
                                    seq += 1;
                                    redisTemplate.opsForValue().set("wechatwork:seq", seq);
                                    continue;
                                }

                                // 将msgtime转换为LocalDate进行比较
                                LocalDateTime msgDateTime = LocalDateTime.ofInstant(
                                        Instant.ofEpochMilli(msgtime),
                                        ZoneId.systemDefault()
                                );
                                LocalDate msgDate = msgDateTime.toLocalDate();
                                LocalDate today = LocalDate.now();

                                // 如果不是今天的消息，跳过处理
                                if (!msgDate.equals(today)) {
                                    seq += 1;
                                    redisTemplate.opsForValue().set("wechatwork:seq", seq);
                                    continue;
                                }
                                String startStr = matcher.group(1);
                                String endStr = matcher.group(2);

                                if (startStr != null && endStr != null) {
                                    LocalTime startTime = LocalTime.parse(startStr, FORMATTER);
                                    LocalTime endTimeObj = LocalTime.parse(endStr, FORMATTER);

                                    LocalDateTime startDateTime = LocalDateTime.of(today, startTime);
                                    LocalDateTime endDateTime = LocalDateTime.of(today, endTimeObj);

                                    Long starTime = startDateTime
                                            .atZone(ZoneId.systemDefault())
                                            .toInstant().toEpochMilli();
                                    Long endTime = endDateTime
                                            .atZone(ZoneId.systemDefault())
                                            .toInstant().toEpochMilli();

                                    // 计算Redis过期时间（到活动结束时间）
                                    long currentTime = System.currentTimeMillis();
                                    long expireSeconds = (endTime - currentTime) / 1000;
                                    if (expireSeconds > 0) {
                                        // 存储到Redis，过期时间就是活动结束时间
                                        redisTemplate.opsForValue().set("wechatwork:" + roomid + ":" + "starTime", starTime, expireSeconds, TimeUnit.SECONDS);
                                        redisTemplate.opsForValue().set("wechatwork:" + roomid + ":" + "endTime", endTime, expireSeconds, TimeUnit.SECONDS);
                                    }

                                    // 时间信息已存储到Redis，无需保存到内存
                                } else {
                                    // 分组为空，跳过
                                }
                                seq += 1;
                                redisTemplate.opsForValue().set("wechatwork:seq", seq);
                                continue;
                            } else {
                                // 情况2: 正常客户发言
                                String currentRoomId = returnJsonDetail.getString("roomid");

                                // 检查Redis中是否有活动时间设置（判断是否是运营发言）
                                Long existingStartTime = (Long) redisTemplate.opsForValue().get("wechatwork:" + currentRoomId + ":" + "starTime");
                                if (existingStartTime == null) {
                                    // 运营未发言，直接跳过处理
                                    seq += 1;
                                    redisTemplate.opsForValue().set("wechatwork:seq", seq);
                                    continue;
                                }

                                // 处理客户发言 - 从Redis获取活动时间
                                Long starTime = (Long) redisTemplate.opsForValue().get("wechatwork:" + currentRoomId + ":" + "starTime");
                                Long endTime = (Long) redisTemplate.opsForValue().get("wechatwork:" + currentRoomId + ":" + "endTime");

                                // 判断是否在活动时间范围内
                                if (starTime != null && endTime != null && msgtime != null && starTime <= msgtime && msgtime <= endTime) {
                                    String from = returnJsonDetail.getString("from");
                                    if (from != null && from.matches("^(wo|wm).*")) {
                                        String UnionId = wechatworkServiceImpl.getExteralContactUnionId(from);
                                        LocalDateTime now = LocalDateTime.now();
                                        LocalDateTime endOfDay = now.with(LocalTime.MAX);
                                        long seconds = Duration.between(now, endOfDay).getSeconds();
                                        redisTemplate.opsForValue().set(header + UnionId, true, seconds, TimeUnit.SECONDS);
                                    }
                                }

                                // 客户发言处理完成，更新seq
                                seq += 1;
                                redisTemplate.opsForValue().set("wechatwork:seq", seq);
                            }


                        } else {
                            // 非文本消息，seq + 1
                            seq += 1;
                            redisTemplate.opsForValue().set("wechatwork:seq", seq);
                        }
                    } else
//                不是群聊
                    {
                        seq += 1;
                        redisTemplate.opsForValue().set("wechatwork:seq", seq);

                    }
                }
                // 退出循环
                // 判断是否需要继续拉取
                if (chatdata.size() < limit) {
                    break; // 不满一页，说明到末尾了
                }

                // 翻页逻辑：更新 seq 或 limit
                seq += limit;
                redisTemplate.opsForValue().set("wechatwork:seq", seq);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void get(String[] args) throws Exception {
        long sdk = Finance.NewSdk();
//        System.out.println(Finance.Init(sdk, Constants.WECHATWORK_CORPID, Constants.WECHATWORK_CHATSECRET));


        long ret = 0;
        try {


            switch (args[0]) {
                case "1":
                    int seq = Integer.parseInt(args[1]);
                    int limit = Integer.parseInt(args[2]);
                    long slice = Finance.NewSlice();
                    ret = Finance.GetChatData(sdk, seq, limit, args[3], args[4], Integer.parseInt(args[5]), slice);
                    if (ret != 0) {
//                        System.out.println("getchatdata ret " + ret);
                        break;
                    }
//                    System.out.println("getchatdata :" + Finance.GetContentFromSlice(slice));
                    returnJson = JSON.parseObject(Finance.GetContentFromSlice(slice));
                    Finance.FreeSlice(slice);
                    break;
                case "2":

                    String indexbuf = "";
                    while (true) {
                        long media_data = Finance.NewMediaData();
                        ret = Finance.GetMediaData(sdk, indexbuf, args[1], args[2], args[3], Integer.parseInt(args[4]), media_data);
//                        System.out.println("getmediadata ret:" + ret);
                        if (ret != 0) {
                            break;
                        }
//                        System.out.printf("getmediadata outindex len:%d, data_len:%d, is_finis:%d\n", Finance.GetIndexLen(media_data), Finance.GetDataLen(media_data), Finance.IsMediaDataFinish(media_data));
                        try {
                            FileOutputStream outputStream = new FileOutputStream(new File("/home/qspace/upload/media_data"), true);
                            outputStream.write(Finance.GetData(media_data));
                            outputStream.close();
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                        if (Finance.IsMediaDataFinish(media_data) == 1) {
                            // need free media_data
                            Finance.FreeMediaData(media_data);
                            break;
                        } else {
                            indexbuf = Finance.GetOutIndexBuf(media_data);

                            // need free media_data
                            Finance.FreeMediaData(media_data);
                        }
                    }
                    break;
                case "3":

                    long msg = Finance.NewSlice();
                    ret = Finance.DecryptData(sdk, args[1], args[2], msg);
                    if (ret != 0) {
//                        System.out.println("getchatdata ret " + ret);
                        break;
                    }
//                    System.out.println("decrypt ret:" + ret + " msg:" + Finance.GetContentFromSlice(msg));
                    System.out.println(Finance.GetContentFromSlice(msg));
                    returnJsonDetail = JSON.parseObject(Finance.GetContentFromSlice(msg));
                    Finance.FreeSlice(msg);
                    break;
                default:
                    System.out.println("wrong args " + args[0]);
                    break;
            }
            Finance.DestroySdk(sdk);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 使用私钥解密数据
     *
     * @param encryptRandomKey Base64 编码后的加密数据
     * @param privateKeyStr    Base64 编码的私钥字符串（PKCS8 格式）
     * @return 解密后的明文
     */
    public String decryptRandomKey(String encryptRandomKey, String privateKeyStr) throws Exception {
        // 1. Base64 decode 私钥
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        // 2. Base64 decode encrypt_random_key
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptRandomKey);

        // 3. 用 RSA/ECB/PKCS1Padding 解密
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes, "UTF-8");
    }

    public static String loadPrivateKeyPEM(String pem) {
        return pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", ""); // 去掉换行和空格
    }

}

