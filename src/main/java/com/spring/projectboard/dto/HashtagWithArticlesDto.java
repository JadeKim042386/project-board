package com.spring.projectboard.dto;

import com.spring.projectboard.domain.Hashtag;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record HashtagWithArticlesDto(
        Long id,
        Set<ArticleDto> articles,
        String hashtagName,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime modifiedAt,
        String modifiedBy
) {
    public static HashtagWithArticlesDto of(Set<ArticleDto> articles, String hashtagName) {
        return new HashtagWithArticlesDto(null, articles, hashtagName, null, null, null, null);
    }
    public static HashtagWithArticlesDto of(Long id, Set<ArticleDto> articles, String hashtagName, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
        return new HashtagWithArticlesDto(id, articles, hashtagName, createdAt, createdBy, modifiedAt, modifiedBy);
    }

    public static HashtagWithArticlesDto from(Hashtag hashtag) {
        return new HashtagWithArticlesDto(
                hashtag.getId(),
                hashtag.getArticles().stream()
                        .map(ArticleDto::from)
                        .collect(Collectors.toUnmodifiableSet()),
                hashtag.getHashtagName(),
                hashtag.getCreatedAt(),
                hashtag.getCreatedBy(),
                hashtag.getModifiedAt(),
                hashtag.getModifiedBy()
        );
    }

    public Hashtag toEntity(){
        return Hashtag.of(hashtagName);
    }
}
