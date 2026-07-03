package com.constructionlegallookup.construction_legal_lookup_app.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.bookmark.BookmarkCreationResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.bookmark.BookmarkResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.bookmark.BookmarkStatusResponse;

public interface BookmarkService {
    Page<BookmarkResponse> getBookmarks(Pageable pageable);
    BookmarkCreationResponse createBookmark(Long documentId);
    void deleteBookmark(Long documentId);
    BookmarkStatusResponse getBookmarkStatus(Long documentId);
}
