package com.constructionlegallookup.construction_legal_lookup_app.controllers;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/public/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP");
    }
}
