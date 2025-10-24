package com.scccy.common.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 读取管理链路追踪相关的配置
 */
@Data
@ConfigurationProperties(prefix = "management.tracing")
public class TracingProperties {
    private boolean enabled = true;
    private Sampling sampling = new Sampling();
    private Zipkin zipkin = new Zipkin();
    private Filter filter = new Filter();
    private Interceptor interceptor = new Interceptor();

    @Data
    public static class Sampling {
        private double probability = 0.1;
    }

    @Data
    public static class Zipkin {
        private Tracing tracing = new Tracing();

        @Data
        public static class Tracing {
            private String endpoint = "http://117.50.197.170:9411/api/v2/spans";
        }
    }

    @Data
    public static class Filter {
        private boolean enabled = true;
        private String headerName = "X-Trace-Id";
        private boolean responseHeader = true;
    }

    @Data
    public static class Interceptor {
        private boolean enabled = true;
        private String headerName = "X-Trace-Id";
    }
}
