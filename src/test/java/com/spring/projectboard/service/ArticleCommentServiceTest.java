package com.spring.projectboard.service;

import com.spring.projectboard.domain.Article;
import com.spring.projectboard.domain.ArticleComment;
import com.spring.projectboard.domain.UserAccount;
import com.spring.projectboard.dto.ArticleCommentDto;
import com.spring.projectboard.dto.UserAccountDto;
import com.spring.projectboard.repository.ArticleCommentRepository;
import com.spring.projectboard.repository.ArticleRepository;
import com.spring.projectboard.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@DisplayName("비지니스 로직 - 댓글")
@ExtendWith(MockitoExtension.class)
class ArticleCommentServiceTest {
    @InjectMocks private ArticleCommentService sut;
    @Mock private ArticleRepository articleRepository;
    @Mock private ArticleCommentRepository articleCommentRepository;
    @Mock private UserAccountRepository userAccountRepository;

    @DisplayName("게시글 ID로 댓글 조회")
    @Test
    void getCommentsWithArticleId() {
        // Given
        Long article_id = 1L;
        ArticleComment comment = createComment("content");
        given(articleCommentRepository.findByArticleId(article_id)).willReturn(List.of(comment));
        // When
        List<ArticleCommentDto> comments = sut.getComments(article_id);
        // Then
        assertThat(comments)
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("content", comment.getContent());
        then(articleCommentRepository).should().findByArticleId(article_id);
    }

    @DisplayName("댓글 저장")
    @Test
    void saveComment() {
        // Given
        ArticleCommentDto dto = createCommentDto("content");
        ArticleComment articleComment = createComment("content");
        given(articleRepository.getReferenceById(dto.articleId())).willReturn(createArticle());
        given(articleCommentRepository.save(any(ArticleComment.class))).willReturn(articleComment);
        given(userAccountRepository.getReferenceById(dto.userAccountDto().userId())).willReturn(createUserAccount());
        // When
        Long savedCommentId = sut.saveComment(dto);
        // Then
        assertThat(savedCommentId).isEqualTo(articleComment.getId());
        then(articleRepository).should().getReferenceById(dto.articleId());
        then(articleCommentRepository).should().save(any(ArticleComment.class));
        then(userAccountRepository).should().getReferenceById(dto.userAccountDto().userId());
    }

    @DisplayName("[예외] 존재하지 않는 게시글에 댓글 저장")
    @Test
    void saveCommentNotExistArticle() {
        // Given
        ArticleCommentDto dto = createCommentDto("content");
        given(articleRepository.getReferenceById(dto.articleId())).willThrow(EntityNotFoundException.class);
        // When
        sut.saveComment(dto);
        // Then
        then(articleRepository).should().getReferenceById(dto.articleId());
        then(articleCommentRepository).shouldHaveNoInteractions();
        then(userAccountRepository).shouldHaveNoInteractions();
    }

    @DisplayName("댓글 수정")
    @Test
    void updateComment() {
        // Given
        ArticleComment comment = createComment("content");
        ArticleCommentDto dto = createCommentDto("new content");
        given(articleCommentRepository.getReferenceById(dto.id())).willReturn(comment);
        // When
        sut.updateComment(dto);
        // Then
        assertThat(comment.getContent()).isEqualTo("new content");
        then(articleCommentRepository).should().getReferenceById(dto.id());
    }

    @DisplayName("[예외] 존재하지 않는 댓글 수정")
    @Test
    void updateNotExistComment() {
        // Given
        ArticleCommentDto dto = createCommentDto("new content");
        given(articleCommentRepository.getReferenceById(dto.id())).willThrow(EntityNotFoundException.class);
        // When
        sut.updateComment(dto);
        // Then
        then(articleCommentRepository).should().getReferenceById(dto.id());
    }

    @DisplayName("댓글 삭제")
    @Test
    void deleteComment() {
        // Given
        Long comment_id = 1L;
        String userId = "joo";
        willDoNothing().given(articleCommentRepository).deleteByIdAndUserAccount_UserId(comment_id, userId);
        // When
        sut.deleteComment(comment_id, userId);
        // Then
        then(articleCommentRepository).should().deleteByIdAndUserAccount_UserId(comment_id, userId);
    }

    private ArticleCommentDto createCommentDto(String content) {
        return ArticleCommentDto.of(
                1L,
                1L,
                createUserAccountDto(),
                null,
                content,
                LocalDateTime.now(),
                "joo",
                LocalDateTime.now(),
                "joo"
        );
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "joo",
                "pw",
                "joo@mail.com",
                "joo",
                "momo",
                LocalDateTime.now(),
                "joo",
                LocalDateTime.now(),
                "joo"
        );
    }

    private ArticleComment createComment(String content) {
        return ArticleComment.of(
                createUserAccount(),
                Article.of(createUserAccount(), "title", "content"),
                content
        );
    }
    private UserAccount createUserAccount() {
        return UserAccount.of(
                "joo",
                "pw",
                "joo@gmail.com",
                "joo",
                "memo"
        );
    }

    private Article createArticle() {
        return Article.of(
                createUserAccount(),
                "title",
                "content"
        );
    }
}
