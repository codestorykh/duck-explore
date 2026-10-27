package com.duck.explore.idempotency.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "enterprise.idempotency")
public class IdempotencyProperties {

    private boolean enabled = true;

    private String prefix = "idemp";

    private String headerName = "Idempotency-Key";

}