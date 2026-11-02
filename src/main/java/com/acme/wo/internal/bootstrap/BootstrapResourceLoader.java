package com.acme.wo.internal.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class BootstrapResourceLoader {

    private static final String YAML_PATTERN = "*.{yaml,yml}";

    private final PathMatchingResourcePatternResolver resolver =
            new PathMatchingResourcePatternResolver();

    public List<Resource> resources(String location) {
        var pattern = Path.of(location, YAML_PATTERN).normalize().toString();
        try {
            var resources = Arrays.stream(
                            resolver.getResources(pattern)
                    )
                    .filter(Resource::isReadable)
                    .sorted(
                            Comparator.comparing(
                                    Resource::getFilename,
                                    Comparator.nullsLast(
                                            String::compareTo
                                    )
                            )
                    )
                    .toList();

            resources.forEach(r -> log.info("{}", r));

            return resources;

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Unable to load bootstrap resources from "
                            + pattern,
                    ex
            );
        }
    }
}
