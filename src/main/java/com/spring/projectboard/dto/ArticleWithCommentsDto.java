package com.spring.projectboard.dto;

import com.spring.projectboard.domain.Article;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public record ArticleWithCommentsDto(
        Long id,
        String title,
        String content,
        Set<HashtagDto> hashtagDtos,
        UserAccountDto userAccountDto,
        Set<ArticleCommentDto> articleCommentDtos,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime modifiedAt,
        String modifiedBy

) {
    public static ArticleWithCommentsDto of(Long id, String title, String content, Set<HashtagDto> hashtagDtos, UserAccountDto userAccountDto, Set<ArticleCommentDto> articleCommentDtos, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
        return new ArticleWithCommentsDto(id, title, content, hashtagDtos, userAccountDto, articleCommentDtos, createdAt, createdBy, modifiedAt, modifiedBy);
    }

    public static ArticleWithCommentsDto from(Article article) {
        return new ArticleWithCommentsDto(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getHashtags().stream()
                        .map(HashtagDto::from)
                        .collect(Collectors.toCollection(LinkedHashSet::new)),
                UserAccountDto.from(article.getUserAccount()),
                article.getArticleComments().stream()
                        .map(ArticleCommentDto::from)
                        .collect(Collectors.toCollection(LinkedHashSet::new)),
                article.getCreatedAt(),
                article.getCreatedBy(),
                article.getModifiedAt(),
                article.getModifiedBy()
        );
    }
}
