package com.spring.projectboard.dto;

import com.spring.projectboard.domain.UserAccount;

import java.time.LocalDateTime;
import java.util.Set;

public record ArticleWithCommentsDto(
        Long id,
        String title,
        String content,
        String hashtag,
        UserAccountDto userAccountDto,
        Set<ArticleCommentDto> articleCommentDtos,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime modifiedAt,
        String modifiedBy

) {
    public static ArticleWithCommentsDto of(Long id, String title, String content, String hashtag, UserAccountDto userAccountDto, Set<ArticleCommentDto> articleCommentDtos, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
        return new ArticleWithCommentsDto(id, title, content, hashtag, userAccountDto, articleCommentDtos, createdAt, createdBy, modifiedAt, modifiedBy);
    }
}
