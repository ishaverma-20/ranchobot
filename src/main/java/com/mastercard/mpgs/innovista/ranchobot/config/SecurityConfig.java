package com.mastercard.mpgs.innovista.ranchobot.config;

import com.mastercard.mpgs.innovista.ranchobot.util.AppConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // Define all publicly accessible URL paths
                                .requestMatchers(
                                        "/", "/login", "/index.html", "/login.html", "/style.css", "/script.js",
                                        "/api/auth/status", "/api/chat", "/api/rancho-chat"
                                ).permitAll()
                                // All other requests (including /api/notifications) must be authenticated
                                .anyRequest().authenticated()
                )
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/login")
                                .defaultSuccessUrl("/index.html", true)
                                .permitAll()
                )
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username(AppConstants.HARDCODED_USERNAME)
                .password(AppConstants.HARDCODED_PASSWORD)
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}