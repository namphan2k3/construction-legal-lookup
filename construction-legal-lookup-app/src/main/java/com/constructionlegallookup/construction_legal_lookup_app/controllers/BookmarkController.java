package com.constructionlegallookup.construction_legal_lookup_app.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.bookmark.BookmarkCreationResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.bookmark.BookmarkResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.bookmark.BookmarkStatusResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.common.ApiResponse;
import com.constructionlegallookup.construction_legal_lookup_app.services.BookmarkService;
import com.constructionlegallookup.construction_legal_lookup_app.utils.LocalizationUtils;
import com.constructionlegallookup.construction_legal_lookup_app.utils.MessageKeys;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final LocalizationUtils localizationUtils;

    @GetMapping
    public ApiResponse<Page<BookmarkResponse>> getBookmarks(@PageableDefault(size = 20, sort = "createdAt,desc") Pageable pageable) {
        Page<BookmarkResponse> bookmarks = bookmarkService.getBookmarks(pageable);
        return ApiResponse.<Page<BookmarkResponse>>builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.BOOKMARK_LIST_SUCCESS))
                .data(bookmarks)
                .build();
    }

    @PostMapping("/{documentId}")
    public ApiResponse<BookmarkCreationResponse> createBookmark(@PathVariable Long documentId) {
        BookmarkCreationResponse response = bookmarkService.createBookmark(documentId);
        return ApiResponse.<BookmarkCreationResponse>builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.BOOKMARK_CREATE_SUCCESS))
                .data(response)
                .build();
    }

    @DeleteMapping("/{documentId}")
    public ApiResponse<Void> deleteBookmark(@PathVariable Long documentId) {
        bookmarkService.deleteBookmark(documentId);
        return ApiResponse.<Void>builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.BOOKMARK_DELETE_SUCCESS))
                .build();
    }

    @GetMapping("/{documentId}/status")
    public ApiResponse<BookmarkStatusResponse> getBookmarkStatus(@PathVariable Long documentId) {
        BookmarkStatusResponse status = bookmarkService.getBookmarkStatus(documentId);
        return ApiResponse.<BookmarkStatusResponse>builder()
                .data(status)
                .build();
    }
}
