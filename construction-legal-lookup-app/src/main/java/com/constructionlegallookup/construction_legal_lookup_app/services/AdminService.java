package com.constructionlegallookup.construction_legal_lookup_app.services;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin.AdminDashboardResponse;

public interface AdminService {
    AdminDashboardResponse getDashboard(String period);
}
