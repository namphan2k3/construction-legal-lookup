package com.constructionlegallookup.construction_legal_lookup_app.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.bookmark.BookmarkCreationResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.bookmark.BookmarkResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.bookmark.BookmarkStatusResponse;
import com.constructionlegallookup.construction_legal_lookup_app.entities.Bookmark;
import com.constructionlegallookup.construction_legal_lookup_app.entities.Document;
import com.constructionlegallookup.construction_legal_lookup_app.entities.User;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.AppException;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.ErrorCode;
import com.constructionlegallookup.construction_legal_lookup_app.mappers.BookmarkMapper;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.BookmarkRepository;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.DocumentRepository;
import com.constructionlegallookup.construction_legal_lookup_app.services.BookmarkService;
import com.constructionlegallookup.construction_legal_lookup_app.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class BookmarkServiceImpl implements BookmarkService {

    BookmarkRepository bookmarkRepository;
    DocumentRepository documentRepository;
    BookmarkMapper bookmarkMapper;
    SecurityUtils securityUtils;

    @Override
    public Page<BookmarkResponse> getBookmarks(Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();
        Page<Bookmark> page = bookmarkRepository.findByUserId(currentUser.getId(), pageable);
        return page.map(bookmarkMapper::toBookmarkResponse);
    }

    @Override
    @Transactional
    public BookmarkCreationResponse createBookmark(Long documentId) {
        User currentUser = securityUtils.getCurrentUser();

        Document doc = documentRepository.findByIdAndDeletedAtIsNull(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy văn bản với id=" + documentId));

        if (bookmarkRepository.existsByUserIdAndDocumentId(currentUser.getId(), documentId)) {
            throw new AppException(ErrorCode.CONFLICT, "Văn bản đã có trong danh sách yêu thích");
        }

        Bookmark bookmark = Bookmark.builder()
                .user(currentUser)
                .document(doc)
                .build();

        bookmark = bookmarkRepository.save(bookmark);
        return bookmarkMapper.toBookmarkCreationResponse(bookmark);
    }

    @Override
    @Transactional
    public void deleteBookmark(Long documentId) {
        User currentUser = securityUtils.getCurrentUser();

        Bookmark bookmark = bookmarkRepository.findByUserIdAndDocumentId(currentUser.getId(), documentId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Văn bản chưa được lưu trong danh sách yêu thích"));

        bookmarkRepository.delete(bookmark);
    }

    @Override
    public BookmarkStatusResponse getBookmarkStatus(Long documentId) {
        User currentUser = securityUtils.getCurrentUser();
        boolean bookmarked = bookmarkRepository.existsByUserIdAndDocumentId(currentUser.getId(), documentId);

        return BookmarkStatusResponse.builder()
                .documentId(documentId)
                .bookmarked(bookmarked)
                .build();
    }
}
