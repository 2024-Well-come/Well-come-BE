package com.wellcome.WellcomeBE.global.p6spy;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "log.query")
@Getter
@Setter
public class QueryLogProperties {
    private long slowThreshold;
}
