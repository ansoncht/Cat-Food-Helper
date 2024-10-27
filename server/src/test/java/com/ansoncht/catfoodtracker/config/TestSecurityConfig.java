package com.ansoncht.catfoodtracker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

public class TestSecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(TestSecurityConfig.class);

    public TestSecurityConfig() {
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        try {
            http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests((auth) -> auth.anyRequest().permitAll());

            return http.build();
        } catch (Exception e) {
            logger.error("Could not create Security Filter Chain", e);

            throw e;
        }
    }

}
