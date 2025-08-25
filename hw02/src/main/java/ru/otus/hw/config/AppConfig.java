package ru.otus.hw.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan("ru.otus.hw")
@PropertySource("classpath:application.properties")
public class AppConfig {
}
