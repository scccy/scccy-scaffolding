package com.scccy.common.redis.cache.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * 基于 fastjson2 的 JetCache Value Decoder，实现 UTF-8 JSON 反序列化。
 */
public class Fastjson2ValueDecoder implements Function<byte[], Object> {

    private static final JSONReader.Feature[] READ_FEATURES = new JSONReader.Feature[]{
        JSONReader.Feature.SupportClassForName,
        JSONReader.Feature.FieldBased
    };


    @Override
    public Object apply(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        String json = new String(bytes, StandardCharsets.UTF_8);
        return JSON.parseObject(json, Object.class, READ_FEATURES);
    }
}
