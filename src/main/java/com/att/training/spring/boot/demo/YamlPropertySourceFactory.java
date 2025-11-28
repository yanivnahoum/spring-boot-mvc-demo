package com.att.training.spring.boot.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.util.Assert;

@Slf4j
public class YamlPropertySourceFactory implements PropertySourceFactory {
    @SneakyThrows
    @Override
    public PropertySource<?> createPropertySource(@Nullable String name, EncodedResource encodedResource) {
        var yamlLoader = new YamlPropertySourceLoader();
        var sourceName = getSourceName(name, encodedResource);
        log.trace("#createPropertySource - Attempting to load {}", sourceName);
        var propertySources = yamlLoader.load(sourceName, encodedResource.getResource());
        Assert.notEmpty(propertySources, "Yaml file [%s] must contain a single document".formatted(sourceName));
        if (propertySources.size() > 1) {
            log.warn("#createPropertySource - Yaml file [{}] loaded as property source contains multiple documents. Only the first one will be used.", sourceName);
        }
        return propertySources.getFirst();
    }

    private String getSourceName(@Nullable String name, EncodedResource resource) {
        if (name != null) return name;
        String filename = resource.getResource().getFilename();
        return filename != null ? filename : "";
    }
}
