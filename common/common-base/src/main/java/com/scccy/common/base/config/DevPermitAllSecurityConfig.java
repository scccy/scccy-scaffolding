package com.scccy.common.base.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 开发/单服务联调阶段的全局放行配置（Base）
 *
 * 适用场景：
 * - 引入了 spring-boot-starter-security，但没有配置 Resource Server（缺少 issuer-uri）
 * - 或希望在开发阶段快速联调，未通过网关走流量
 *
 * 激活条件：
 * - 配置项 scccy.security.permit-all=true 时生效（默认 true）
 * - 且当前应用不存在其他 SecurityFilterChain Bean（避免与业务自定义配置冲突）
 *
 * 生产环境请关闭本配置（将 scccy.security.permit-all 置为 false），
 * 并通过网关统一鉴权或在业务服务启用 Resource Server。
 */
@Configuration
@ConditionalOnClass(HttpSecurity.class)
@ConditionalOnMissingBean(SecurityFilterChain.class)
@ConditionalOnProperty(name = "scccy.security.permit-all", havingValue = "true")
public class DevPermitAllSecurityConfig {

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilterChain permitAllSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth
				.anyRequest().permitAll()
			);
		return http.build();
	}
}


