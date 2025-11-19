package com.scccy.common.base.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
// import com.baomidou.mybatisplus.extension.plugins.pagination.optimize.JsqlParserCountOptimize;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

@Configuration
public class MyBatisPlusConfig {
    
    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); // 如果配置多个插件, 切记分页最后添加
        // 如果有多数据源可以不配具体类型, 否则都建议配上具体的 DbType
        System.out.println("============== 分页插件加载成功 ==============");
        return interceptor;
    }


    
    /**
     * 自动填充处理器
     */
    @Component
    public static class MetaObjectHandlerImpl implements MetaObjectHandler {
        
        /**
         * 获取当前用户名，如果无法获取则返回默认值 "system"
         */
        private String getCurrentUsername() {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
                    return authentication.getName();
                }
            } catch (Exception e) {
                // 忽略异常，返回默认值
            }
            return "system";
        }
        
        @Override
        public void insertFill(MetaObject metaObject) {
            String currentUser = getCurrentUsername();
            // 插入时自动填充
            // 处理 LocalDateTime 类型的字段
            this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, LocalDateTime.now());
            this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
            // 填充创建人和更新人
            this.strictInsertFill(metaObject, "createdBy", String.class, currentUser);
            this.strictInsertFill(metaObject, "updatedBy", String.class, currentUser);

        }
        
        @Override
        public void updateFill(MetaObject metaObject) {
            String currentUser = getCurrentUsername();
            // 更新时自动填充
            // 处理 LocalDateTime 类型的字段
            this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
            // 处理 Date 类型的字段
            this.strictUpdateFill(metaObject, "updatedTime", Date.class, new Date());
            // 填充更新人
            this.strictUpdateFill(metaObject, "updatedBy", String.class, currentUser);
            // 兼容不同的字段名（update_by/updated_by）
            this.strictUpdateFill(metaObject, "updateBy", String.class, currentUser);
        }
    }
}

