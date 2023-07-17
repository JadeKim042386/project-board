package com.spring.projectboard.dto.response;

import com.spring.projectboard.dto.ArticleCommentDto;
import com.spring.projectboard.dto.ArticleWithCommentsDto;
import com.spring.projectboard.dto.HashtagDto;
import com.spring.projectboard.dto.UserAccountDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DTO - 댓글을 포함한 게시글 응답 테스트")
class ArticleWithCommentResponseTest {

    @DisplayName("자식 댓글이 없는 ArticleWithCommentsDto -> ArticleWithCommentResponse 으로 변환할 때 댓글을 시간 내림차순 + ID 오름차순으로 정렬")
    @Test
    void withoutChildCommentsOrderByCreatedAtAndId() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Set<ArticleCommentDto> articleCommentDtos = Set.of(
                createArticleCommentDto(1L, null, now),
                createArticleCommentDto(2L, null, now.plusDays(1L)),
                createArticleCommentDto(3L, null, now.plusDays(3L)),
                createArticleCommentDto(4L, null, now),
                createArticleCommentDto(5L, null, now.plusDays(5L)),
                createArticleCommentDto(6L, null, now.plusDays(4L)),
                createArticleCommentDto(7L, null, now.plusDays(2L)),
                createArticleCommentDto(8L, null, now.plusDays(7L))
        );
        ArticleWithCommentsDto input = createArticleWithCommentDto(articleCommentDtos);
        // When
        ArticleWithCommentResponse actual = ArticleWithCommentResponse.from(input);
        // Then
        assertThat(actual.articleCommentResponses())
                .containsExactly(
                        createArticleCommentResponse(8L, null, now.plusDays(7L)),
                        createArticleCommentResponse(5L, null, now.plusDays(5L)),
                        createArticleCommentResponse(6L, null, now.plusDays(4L)),
                        createArticleCommentResponse(3L, null, now.plusDays(3L)),
                        createArticleCommentResponse(7L, null, now.plusDays(2L)),
                        createArticleCommentResponse(2L, null, now.plusDays(1L)),
                        createArticleCommentResponse(1L, null, now),
                        createArticleCommentResponse(4L, null, now)
                );
    }

    @DisplayName("ArticleWithCommentsDto -> ArticleWithCommentResponse 으로 변환할 때 댓글 부모 자식 관계를 규칙에 맞게 정렬")
    @Test
    void OrderByParentAndChildComments() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Set<ArticleCommentDto> articleCommentDtos = Set.of(
                createArticleCommentDto(1L, null, now),
                createArticleCommentDto(2L, 1L, now.plusDays(1L)),
                createArticleCommentDto(3L, 1L, now.plusDays(3L)),
                createArticleCommentDto(4L, 1L, now),
                createArticleCommentDto(5L, null, now.plusDays(5L)),
                createArticleCommentDto(6L, null, now.plusDays(4L)),
                createArticleCommentDto(7L, 6L, now.plusDays(2L)),
                createArticleCommentDto(8L, 6L, now.plusDays(7L))
        );
        ArticleWithCommentsDto input = createArticleWithCommentDto(articleCommentDtos);
        // When
        ArticleWithCommentResponse actual = ArticleWithCommentResponse.from(input);
        // Then
        assertThat(actual.articleCommentResponses())
                .containsExactly(
                        createArticleCommentResponse(5L, null, now.plusDays(5L)),
                        createArticleCommentResponse(6L, null, now.plusDays(4L)),
                        createArticleCommentResponse(1L, null, now)
                )
                .flatExtracting(ArticleCommentResponse::childComments)
                .containsExactly(
                        createArticleCommentResponse(7L, 6L, now.plusDays(2L)),
                        createArticleCommentResponse(8L, 6L, now.plusDays(7L)),
                        createArticleCommentResponse(4L, 1L, now),
                        createArticleCommentResponse(2L, 1L, now.plusDays(1L)),
                        createArticleCommentResponse(3L, 1L, now.plusDays(3L))
                );
    }

    @DisplayName("ArticleWithCommentsDto -> ArticleWithCommentResponse 으로 변환할 때 부모 자식 관계 깊이(depth)는 제한이 없다.")
    @Test
    void OrganizesParentAndChildCommentsWithoutDepthLimit() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Set<ArticleCommentDto> articleCommentDtos = Set.of(
                createArticleCommentDto(1L, null, now),
                createArticleCommentDto(2L, 1L, now.plusDays(1L)),
                createArticleCommentDto(3L, 2L, now.plusDays(2L)),
                createArticleCommentDto(4L, 3L, now.plusDays(3L)),
                createArticleCommentDto(5L, 4L, now.plusDays(4L)),
                createArticleCommentDto(6L, 5L, now.plusDays(5L)),
                createArticleCommentDto(7L, 6L, now.plusDays(6L)),
                createArticleCommentDto(8L, 7L, now.plusDays(7L))
        );
        ArticleWithCommentsDto input = createArticleWithCommentDto(articleCommentDtos);

        // When
        ArticleWithCommentResponse actual = ArticleWithCommentResponse.from(input);

        // Then
        Iterator<ArticleCommentResponse> iterator = actual.articleCommentResponses().iterator();
        long i = 1L;
        while (iterator.hasNext()) {
            ArticleCommentResponse articleCommentResponse = iterator.next();
            assertThat(articleCommentResponse)
                    .hasFieldOrPropertyWithValue("id", i)
                    .hasFieldOrPropertyWithValue("parentCommentId", i == 1L ? null : i - 1L)
                    .hasFieldOrPropertyWithValue("createdAt", now.plusDays(i - 1L));

            iterator = articleCommentResponse.childComments().iterator();
            i++;
        }
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "joo",
                "password",
                "joo@gmail.com",
                "Joo",
                "This is memo",
                LocalDateTime.now(),
                "joo",
                LocalDateTime.now(),
                "joo"
        );
    }

    private ArticleCommentDto createArticleCommentDto(Long id, Long parentCommentId, LocalDateTime createdAt) {
        return ArticleCommentDto.of(
                id,
                1L,
                createUserAccountDto(),
                parentCommentId,
                "test comment " + id,
                createdAt,
                "joo",
                createdAt,
                "joo"
        );
    }

    private ArticleWithCommentsDto createArticleWithCommentDto(Set<ArticleCommentDto> articleCommentDtos) {
        return ArticleWithCommentsDto.of(
                1L,
                "title",
                "content",
                Set.of(HashtagDto.of("java")),
                createUserAccountDto(),
                articleCommentDtos,
                LocalDateTime.now(),
                "joo",
                LocalDateTime.now(),
                "joo"
        );
    }

    private ArticleCommentResponse createArticleCommentResponse(Long id, Long parentCommentId, LocalDateTime createdAt) {
        return ArticleCommentResponse.of(
                id,
                "test comment " + id,
                createdAt,
                "joo@gmail.com",
                "Joo",
                "joo",
                parentCommentId
        );
    }
}