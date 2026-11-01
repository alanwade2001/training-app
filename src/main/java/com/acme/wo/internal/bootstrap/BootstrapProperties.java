package com.acme.wo.internal.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bootstrap")
public record BootstrapProperties(
        boolean enabled,
        String programmes,
        String workouts
) {
}
