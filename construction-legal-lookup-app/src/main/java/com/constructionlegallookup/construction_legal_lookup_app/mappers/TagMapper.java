package com.constructionlegallookup.construction_legal_lookup_app.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.TagAdminRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.document.TagResponse;
import com.constructionlegallookup.construction_legal_lookup_app.entities.Tag;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagResponse toTagResponse(Tag tag);
    Tag toTag(TagAdminRequest request);
    void updateTag(@MappingTarget Tag tag, TagAdminRequest request);
}
