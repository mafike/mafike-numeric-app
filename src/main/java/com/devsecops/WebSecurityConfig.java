package com.devsecops;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // Disable CSRF protection (only if using REST APIs; enable otherwise)
            .csrf().disable()

            // Add security headers
            .headers()
                .contentSecurityPolicy("default-src 'self'; script-src 'self' 'unsafe-inline';")
                .and()
                .frameOptions().deny()
                .and()

            // Configure URL-based authorization
            .authorizeRequests()
                // Allow unauthenticated access to health and info endpoints
                .antMatchers("/actuator/health", "/actuator/info").permitAll()
                // Require authentication for all other requests
                .anyRequest().authenticated()
                .and()

            // Add basic authentication (for simplicity; replace with a more secure method if needed)
            .httpBasic();
    }
}
