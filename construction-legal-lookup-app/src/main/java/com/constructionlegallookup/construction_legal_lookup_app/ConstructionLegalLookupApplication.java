package com.constructionlegallookup.construction_legal_lookup_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ConstructionLegalLookupApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConstructionLegalLookupApplication.class, args);
    }
}
