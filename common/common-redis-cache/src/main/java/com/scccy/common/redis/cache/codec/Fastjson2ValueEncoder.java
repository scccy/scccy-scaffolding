package com.scccy.common.redis.cache.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * 基于 fastjson2 的 JetCache Value Encoder，实现 UTF-8 JSON 序列化。
 */
public class Fastjson2ValueEncoder implements Function<Object, byte[]> {

    private static final JSONWriter.Feature[] WRITE_FEATURES = new JSONWriter.Feature[]{
        JSONWriter.Feature.WriteClassName,
        JSONWriter.Feature.WriteEnumsUsingName,
        JSONWriter.Feature.WriteNulls
    };


    @Override
    public byte[] apply(Object value) {
        if (value == null) {
            return new byte[0];
        }
        String json = JSON.toJSONString(value, WRITE_FEATURES);
        return json.getBytes(StandardCharsets.UTF_8);
    }
}
