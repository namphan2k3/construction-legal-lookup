package com.constructionlegallookup.construction_legal_lookup_app.controllers;

import org.springframework.web.bind.annotation.*;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin.AdminDashboardResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.common.ApiResponse;
import com.constructionlegallookup.construction_legal_lookup_app.services.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse> getDashboard(@RequestParam(defaultValue = "7d") String period) {
        AdminDashboardResponse dashboard = adminService.getDashboard(period);
        return ApiResponse.<AdminDashboardResponse>builder()
                .data(dashboard)
                .build();
    }
}
