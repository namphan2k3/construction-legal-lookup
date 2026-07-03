package com.constructionlegallookup.construction_legal_lookup_app.services.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin.TagAdminRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.document.TagResponse;
import com.constructionlegallookup.construction_legal_lookup_app.entities.Tag;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.AppException;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.ErrorCode;
import com.constructionlegallookup.construction_legal_lookup_app.mappers.TagMapper;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.TagRepository;
import com.constructionlegallookup.construction_legal_lookup_app.services.AdminTagService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class AdminTagServiceImpl implements AdminTagService {

    TagRepository tagRepository;
    TagMapper tagMapper;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream().map(tagMapper::toTagResponse).toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TagResponse createTag(TagAdminRequest request) {
        Tag tag = tagMapper.toTag(request);
        Tag saved = tagRepository.save(tag);
        return tagMapper.toTagResponse(saved);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TagResponse updateTag(Long id, TagAdminRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TAG_NOT_FOUND));
        tagMapper.updateTag(tag, request);
        Tag updated = tagRepository.save(tag);
        return tagMapper.toTagResponse(updated);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }
}
