package com.spring.projectboard.service;

import com.spring.projectboard.domain.Article;
import com.spring.projectboard.domain.SearchType;
import com.spring.projectboard.domain.UserAccount;
import com.spring.projectboard.dto.ArticleDto;
import com.spring.projectboard.dto.ArticleWithCommentsDto;
import com.spring.projectboard.dto.UserAccountDto;
import com.spring.projectboard.repository.ArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@DisplayName("비지니스 로직 - 게시글")
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @InjectMocks private ArticleService sut; //system under test
    @Mock private ArticleRepository articleRepository;

    @DisplayName("검색없이 게시글 페이지 검색")
    @Test
    void noSearchArticles() {
        // Given
        Pageable pageable = Pageable.ofSize(20); //페이지 크기가 20인 Pageable 생성
        given(articleRepository.findAll(pageable)).willReturn(Page.empty()); // 빈 페이지를 반환하도록 설정
        // When
        Page<ArticleDto> articles = sut.searchArticles(null, null, pageable);
        // Then
        assertThat(articles).isEmpty();
        then(articleRepository).should().findAll(pageable); //findAll 검증
    }

    @DisplayName("검색어로 게시글 페이지 검색")
    @Test
    void searchArticles() {
        // Given
        SearchType searchType = SearchType.TITLE;
        String searchKeyword = "title";
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByTitleContaining(searchKeyword, pageable)).willReturn(Page.empty());

        // When
        Page<ArticleDto> articles = sut.searchArticles(searchType, searchKeyword, pageable);

        // Then
        assertThat(articles).isEmpty();
        then(articleRepository).should().findByTitleContaining(searchKeyword, pageable);
    }

    @DisplayName("articleId로 게시글 조회")
    @Test
    void searchArticle() {
        // Given
        Long articleId = 1L;
        Article article = createArticle();
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

        // When
        ArticleWithCommentsDto dto = sut.getArticle(articleId);

        // Then
        assertThat(dto)
                .hasFieldOrPropertyWithValue("title", article.getTitle())
                .hasFieldOrPropertyWithValue("content", article.getContent())
                .hasFieldOrPropertyWithValue("hashtag", article.getHashtag());
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("[예외] articleId로 게시글 조회")
    @Test
    void exSearchArticle() {
        // Given
        Long articleId = 0L;
        given(articleRepository.findById(articleId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sut.getArticle(articleId)).isInstanceOf(EntityNotFoundException.class);
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("게시글 저장")
    @Test
    void saveArticle() {
        // Given
        ArticleDto articleDto = createArticleDto();
        given(articleRepository.save(any(Article.class))).willReturn(createArticle());
        // When
        sut.saveArticle(articleDto);
        // Then
        then(articleRepository).should().save(any(Article.class));
    }

    @DisplayName("존재하는 게시글 수정")
    @Test
    void updateArticle() {
        // Given
        ArticleDto articleDto = createArticleDto("new title", "new content", "#Springboot");
        given(articleRepository.getReferenceById(articleDto.id())).willReturn(createArticle());
        // When
        sut.updateArticle(articleDto);
        // Then
        then(articleRepository).should().getReferenceById(articleDto.id());
    }

    @DisplayName("[예외] 존재하지않는 게시글 수정")
    @Test
    void updateNotExistArticle() {
        // Given
        ArticleDto articleDto = createArticleDto("new title", "new content", "#Springboot");
        given(articleRepository.getReferenceById(articleDto.id())).willThrow(EntityNotFoundException.class);
        // When
        sut.updateArticle(articleDto);
        // Then
        then(articleRepository).should().getReferenceById(articleDto.id());
    }

    @DisplayName("게시글 삭제")
    @Test
    void deleteArticle() {
        // Given
        Long articleId = 1L;
        willDoNothing().given(articleRepository).deleteById(articleId);
        // When
        sut.deleteArticle(1L);
        // Then
        then(articleRepository).should().deleteById(articleId);
    }

    private Article createArticle() {
        return Article.of(
                createUserAccount(),
                "title",
                "content",
                "#java"
        );
    }

    private UserAccount createUserAccount() {
        return UserAccount.of(
                "uno",
                "password",
                "uno@email.com",
                "Uno",
                null
        );
    }

    private ArticleDto createArticleDto() {
        return createArticleDto("title", "content", "#java");
    }

    private ArticleDto createArticleDto(String title, String content, String hashtag) {
        return ArticleDto.of(
                1L,
                createUserAccountDto(),
                title,
                content,
                hashtag,
                LocalDateTime.now(),
                "Jno",
                LocalDateTime.now(),
                "Jno");
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                1L,
                "joo",
                "password",
                "joo@mail.com",
                "Jno",
                "This is memo",
                LocalDateTime.now(),
                "jno",
                LocalDateTime.now(),
                "jno"
        );
    }
}
