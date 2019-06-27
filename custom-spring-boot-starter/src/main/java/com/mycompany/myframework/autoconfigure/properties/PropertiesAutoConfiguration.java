package com.mycompany.myframework.autoconfigure.properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.mycompany.myframework.properties.config.MyFrameworkConfig;

@Configuration
@EnableConfigurationProperties({ MyFrameworkConfig.class })
public class PropertiesAutoConfiguration {
}
