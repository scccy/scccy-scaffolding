package com.scccy.service.demo.controller;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.CacheUpdate;
import com.scccy.common.modules.annotation.Anonymous;
import com.scccy.common.modules.dto.ResultData;
import com.scccy.common.redis.cache.DefaultCacheArea;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存测试 Controller
 * <p>
 * 用于演示 JetCache 缓存注解的使用方式：
 * <ul>
 *     <li>@Cached: 缓存方法返回值</li>
 *     <li>@CacheUpdate: 更新缓存</li>
 *     <li>@CacheInvalidate: 删除缓存</li>
 * </ul>
 * <p>
 * 缓存类型说明：
 * <ul>
 *     <li>CacheType.LOCAL: 仅本地缓存（Caffeine）</li>
 *     <li>CacheType.REMOTE: 仅远程缓存（Redis）</li>
 *     <li>CacheType.BOTH: 两级缓存（本地 + 远程）</li>
 * </ul>
 *
 * @author scccy
 */
@Slf4j
@Tag(name = "缓存测试", description = "JetCache 缓存注解使用示例")
@RequestMapping("/demo/cache")
@RestController
public class CacheDemoController {

    private static final String CACHE_PREFIX = "demo:cache:";

    /**
     * 测试1：使用 @Cached 注解缓存方法返回值（默认缓存区域）
     * <p>
     * 使用 CacheType.BOTH 表示使用两级缓存（本地 + 远程）
     * 缓存键为：demo:cache:default:{id}
     */
    @Anonymous
    @Cached(name = CACHE_PREFIX + "default", key = "#id", cacheType = CacheType.BOTH, expire = 300)
    @Operation(summary = "获取数据（默认缓存）", description = "使用 @Cached 注解缓存方法返回值，默认缓存区域，过期时间 300 秒")
    @GetMapping("/default/{id}")
    public ResultData<CacheData> getDefaultCache(
            @Parameter(description = "数据ID") @PathVariable String id) {
        log.info("执行数据库查询，ID: {}", id);
        
        // 模拟数据库查询
        CacheData data = new CacheData();
        data.setId(id);
        data.setName("默认缓存数据");
        data.setValue("这是使用默认缓存区域的数据");
        data.setTimestamp(LocalDateTime.now());
        
        return ResultData.ok(data);
    }

    /**
     * 测试2：使用长时间缓存区域
     * <p>
     * 使用 longTime 缓存区域，适合缓存时效要求不高的数据
     */
    @Anonymous
    @Cached(name = CACHE_PREFIX + DefaultCacheArea.LONG_TIME_AREA, key = "#id", 
            cacheType = CacheType.BOTH, expire = 3600)
    @Operation(summary = "获取数据（长时间缓存）", description = "使用 longTime 缓存区域，过期时间 3600 秒")
    @GetMapping("/long-time/{id}")
    public ResultData<CacheData> getLongTimeCache(
            @Parameter(description = "数据ID") @PathVariable String id) {
        log.info("执行数据库查询（长时间缓存），ID: {}", id);
        
        CacheData data = new CacheData();
        data.setId(id);
        data.setName("长时间缓存数据");
        data.setValue("这是使用长时间缓存区域的数据");
        data.setTimestamp(LocalDateTime.now());
        
        return ResultData.ok(data);
    }

    /**
     * 测试3：使用短时间缓存区域
     * <p>
     * 使用 shortTime 缓存区域，适合缓存时效要求较高的数据
     */
    @Anonymous
    @Cached(name = CACHE_PREFIX + DefaultCacheArea.SHORT_TIME_AREA, key = "#id", 
            cacheType = CacheType.BOTH, expire = 60)
    @Operation(summary = "获取数据（短时间缓存）", description = "使用 shortTime 缓存区域，过期时间 60 秒")
    @GetMapping("/short-time/{id}")
    public ResultData<CacheData> getShortTimeCache(
            @Parameter(description = "数据ID") @PathVariable String id) {
        log.info("执行数据库查询（短时间缓存），ID: {}", id);
        
        CacheData data = new CacheData();
        data.setId(id);
        data.setName("短时间缓存数据");
        data.setValue("这是使用短时间缓存区域的数据");
        data.setTimestamp(LocalDateTime.now());
        
        return ResultData.ok(data);
    }

    /**
     * 测试4：仅使用本地缓存
     * <p>
     * 使用 CacheType.LOCAL，仅缓存到本地（Caffeine），不缓存到 Redis
     */
    @Anonymous
    @Cached(name = CACHE_PREFIX + "local", key = "#id", cacheType = CacheType.LOCAL, expire = 300)
    @Operation(summary = "获取数据（仅本地缓存）", description = "使用 CacheType.LOCAL，仅缓存到本地")
    @GetMapping("/local/{id}")
    public ResultData<CacheData> getLocalCache(
            @Parameter(description = "数据ID") @PathVariable String id) {
        log.info("执行数据库查询（仅本地缓存），ID: {}", id);
        
        CacheData data = new CacheData();
        data.setId(id);
        data.setName("本地缓存数据");
        data.setValue("这是仅使用本地缓存的数据");
        data.setTimestamp(LocalDateTime.now());
        
        return ResultData.ok(data);
    }

    /**
     * 测试5：仅使用远程缓存
     * <p>
     * 使用 CacheType.REMOTE，仅缓存到 Redis，不缓存到本地
     */
    @Anonymous
    @Cached(name = CACHE_PREFIX + "remote", key = "#id", cacheType = CacheType.REMOTE, expire = 300)
    @Operation(summary = "获取数据（仅远程缓存）", description = "使用 CacheType.REMOTE，仅缓存到 Redis")
    @GetMapping("/remote/{id}")
    public ResultData<CacheData> getRemoteCache(
            @Parameter(description = "数据ID") @PathVariable String id) {
        log.info("执行数据库查询（仅远程缓存），ID: {}", id);
        
        CacheData data = new CacheData();
        data.setId(id);
        data.setName("远程缓存数据");
        data.setValue("这是仅使用远程缓存的数据");
        data.setTimestamp(LocalDateTime.now());
        
        return ResultData.ok(data);
    }

    /**
     * 测试6：更新缓存
     * <p>
     * 使用 @CacheUpdate 注解更新缓存值
     * 注意：@CacheUpdate 的 value 参数指向要更新的值，通常是方法参数或返回值
     */
    @Anonymous
    @CacheUpdate(name = CACHE_PREFIX + "default", key = "#id", value = "#data")
    @Operation(summary = "更新数据并刷新缓存", description = "使用 @CacheUpdate 注解更新缓存")
    @PutMapping("/update/{id}")
    public ResultData<CacheData> updateCache(
            @Parameter(description = "数据ID") @PathVariable String id,
            @RequestBody CacheData data) {
        log.info("更新数据并刷新缓存，ID: {}", id);
        
        data.setId(id);
        data.setName("更新后的数据");
        data.setTimestamp(LocalDateTime.now());
        
        // 模拟数据库更新
        // ...
        
        return ResultData.ok(data);
    }

    /**
     * 测试7：删除缓存
     * <p>
     * 使用 @CacheInvalidate 注解删除缓存
     */
    @Anonymous
    @CacheInvalidate(name = CACHE_PREFIX + "default", key = "#id")
    @Operation(summary = "删除数据并清除缓存", description = "使用 @CacheInvalidate 注解删除缓存")
    @DeleteMapping("/delete/{id}")
    public ResultData<Map<String, Object>> deleteCache(
            @Parameter(description = "数据ID") @PathVariable String id) {
        log.info("删除数据并清除缓存，ID: {}", id);
        
        // 模拟数据库删除
        // ...
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("message", "数据已删除，缓存已清除");
        result.put("timestamp", LocalDateTime.now());
        
        return ResultData.ok(result);
    }

    /**
     * 测试8：组合使用（查询 + 更新 + 删除）
     * <p>
     * 演示在一个方法中同时使用多个缓存注解
     */
    @Anonymous
    @Cached(name = CACHE_PREFIX + "default", key = "#id", cacheType = CacheType.BOTH, expire = 300)
    @Operation(summary = "获取数据（组合示例）", description = "演示缓存注解的组合使用")
    @GetMapping("/combined/{id}")
    public ResultData<CacheData> getCombinedCache(
            @Parameter(description = "数据ID") @PathVariable String id) {
        log.info("执行数据库查询（组合示例），ID: {}", id);
        
        CacheData data = new CacheData();
        data.setId(id);
        data.setName("组合缓存数据");
        data.setValue("这是演示组合使用的缓存数据");
        data.setTimestamp(LocalDateTime.now());
        
        return ResultData.ok(data);
    }

    /**
     * 测试9：缓存统计信息
     * <p>
     * 返回缓存使用说明和统计信息
     */
    @Anonymous
    @Operation(summary = "缓存统计信息", description = "返回缓存使用说明和统计信息")
    @GetMapping("/stats")
    public ResultData<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheTypes", Map.of(
            "LOCAL", "仅本地缓存（Caffeine）",
            "REMOTE", "仅远程缓存（Redis）",
            "BOTH", "两级缓存（本地 + 远程）"
        ));
        stats.put("cacheAreas", Map.of(
            "default", "默认缓存区域",
            DefaultCacheArea.LONG_TIME_AREA, "长时间缓存区域",
            DefaultCacheArea.SHORT_TIME_AREA, "短时间缓存区域"
        ));
        stats.put("annotations", Map.of(
            "@Cached", "缓存方法返回值",
            "@CacheUpdate", "更新缓存值",
            "@CacheInvalidate", "删除缓存"
        ));
        stats.put("timestamp", LocalDateTime.now());
        
        return ResultData.ok(stats);
    }

    /**
     * 缓存数据模型
     */
    @Data
    public static class CacheData {
        private String id;
        private String name;
        private String value;
        private LocalDateTime timestamp;
    }
}

