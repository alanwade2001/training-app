package com.acme.wo.internal.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Data
public class ClassPathResourceLoader {

    public List<Resource> load(String path) {
        ClassLoader cl = this.getClass().getClassLoader();
        var resolver = new PathMatchingResourcePatternResolver(cl);
        try {
            var resources = resolver.getResources(path);
            return Arrays.asList(resources);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
