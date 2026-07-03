package com.constructionlegallookup.construction_legal_lookup_app.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.bookmark.BookmarkCreationResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.bookmark.BookmarkResponse;
import com.constructionlegallookup.construction_legal_lookup_app.entities.Bookmark;

@Mapper(componentModel = "spring", uses = {DocumentMapper.class})
public interface BookmarkMapper {

    BookmarkResponse toBookmarkResponse(Bookmark bookmark);

    @Mapping(target = "documentId", source = "document.id")
    BookmarkCreationResponse toBookmarkCreationResponse(Bookmark bookmark);
}
