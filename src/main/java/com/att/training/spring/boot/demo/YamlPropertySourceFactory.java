package com.att.training.spring.boot.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

@Slf4j
public class YamlPropertySourceFactory implements PropertySourceFactory {

    @SneakyThrows
    @NonNull
    @Override
    public PropertySource<?> createPropertySource(String name, @NonNull EncodedResource encodedResource) {
        var yamlLoader = new YamlPropertySourceLoader();
        var sourceName = getSourceName(name, encodedResource);
        log.trace("#createPropertySource - Attempting to load {}", sourceName);
        var propertySources = yamlLoader.load(sourceName, encodedResource.getResource());
        Assert.notEmpty(propertySources, String.format("Yaml file [%s] must contain a single document", sourceName));
        if (propertySources.size() > 1) {
            log.warn("#createPropertySource - Yaml file [{}] loaded as property source contains multiple documents. Only the first one will be used.", sourceName);
        }
        return propertySources.get(0);
    }

    @NonNull
    private String getSourceName(String name, @NonNull EncodedResource resource) {
        if (name != null) return name;
        String filename = resource.getResource().getFilename();
        return filename != null ? filename : "";
    }
}
