package com.scccy.common.seata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Seata 客户端配置，映射官方 seata.client.* 属性。
 */
@Data
@ConfigurationProperties(prefix = "seata.client")
public class SeataClientProperties {

    /**
     * 是否启用全局事务能力。
     */
    private boolean enabled = true;

    /**
     * Seata 客户端应用 ID。
     */
    private String applicationId = "scccy-scaffolding";

    /**
     * 事务分组名称。
     */
    private String txServiceGroup = "default_tx_group";

    /**
     * 是否自动代理数据源。
     */
    private boolean enableAutoDataSourceProxy = true;

    /**
     * 数据源代理模式：AT/XA/NONE，默认 AT。
     */
    private String dataSourceProxyMode = "AT";

}
