package com.att.training.spring.boot.demo;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.lang.NonNull;

import java.util.Properties;

public class YamlPropertySourceFactory implements PropertySourceFactory {

    @NonNull
    @Override
    public PropertySource<?> createPropertySource(String name, @NonNull EncodedResource resource) {
        Properties yamlProperties = loadYamlProperties(resource);
        String sourceName = getSourceName(name, resource);
        return new PropertiesPropertySource(sourceName, yamlProperties);
    }

    private Properties loadYamlProperties(EncodedResource resource) {
        var factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource.getResource());
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @NonNull
    private String getSourceName(String name, @NonNull EncodedResource resource) {
        if (name != null) return name;
        String filename = resource.getResource().getFilename();
        return filename != null ? filename : "";
    }
}
