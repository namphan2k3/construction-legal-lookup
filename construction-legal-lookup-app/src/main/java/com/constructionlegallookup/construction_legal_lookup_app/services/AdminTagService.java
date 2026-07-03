package com.constructionlegallookup.construction_legal_lookup_app.services;

import java.util.List;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.TagAdminRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.document.TagResponse;

public interface AdminTagService {
    List<TagResponse> getAllTags();
    TagResponse createTag(TagAdminRequest request);
    TagResponse updateTag(Long id, TagAdminRequest request);
    void deleteTag(Long id);
}
