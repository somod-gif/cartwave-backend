package com.cartwave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication(scanBasePackages = "com.cartwave")
@EnableJpaRepositories(basePackages = "com.cartwave")
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
@EnableMethodSecurity
@EnableWebSecurity
public class CartwaveBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartwaveBackendApplication.class, args);
    }

}

