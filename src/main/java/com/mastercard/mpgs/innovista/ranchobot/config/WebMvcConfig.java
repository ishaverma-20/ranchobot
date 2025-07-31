package com.mastercard.mpgs.innovista.ranchobot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Maps URL paths directly to static HTML files to avoid view resolution conflicts.
        registry.addViewController("/login").setViewName("forward:/login.html");
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}